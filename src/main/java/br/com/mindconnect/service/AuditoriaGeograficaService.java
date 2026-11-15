package br.com.mindconnect.service;

import br.com.mindconnect.dao.AuditoriaGeograficaDAO;
import br.com.mindconnect.geografia.GeoapifyClient;
import br.com.mindconnect.geografia.RelatorioAuditoriaGeograficaDto;
import br.com.mindconnect.geografia.ResultadoAuditoriaGeograficaDto;
import br.com.mindconnect.geografia.ServicoGeograficoDto;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AuditoriaGeograficaService {

    private static final double DISTANCIA_COMPATIVEL_METROS = 150.0;
    private static final double DISTANCIA_DIVERGENTE_METROS = 500.0;
    private static final double CONFIANCA_MINIMA = 0.75;

    private final AuditoriaGeograficaDAO dao;
    private final GeoapifyClient geoapifyClient;

    public AuditoriaGeograficaService(AuditoriaGeograficaDAO dao, GeoapifyClient geoapifyClient) {
        this.dao = dao;
        this.geoapifyClient = geoapifyClient;
    }

    public RelatorioAuditoriaGeograficaDto auditar(int pagina, int limite) {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página não pode ser negativa.");
        }

        if (limite < 1 || limite > 50) {
            throw new IllegalArgumentException("O limite deve estar entre 1 e 50 serviços por execução.");
        }

        List<ServicoGeograficoDto> servicos = dao.listarParaAuditoria(pagina, limite);
        List<ResultadoAuditoriaGeograficaDto> resultados = new ArrayList<>();

        for (ServicoGeograficoDto servico : servicos) {
            resultados.add(auditarServico(servico));
        }

        return new RelatorioAuditoriaGeograficaDto(
                pagina,
                limite,
                resultados.size(),
                contar(resultados, "COMPATIVEL"),
                contar(resultados, "DIVERGENTE"),
                contar(resultados, "SEM_COORDENADAS"),
                contar(resultados, "RESULTADO_AMBIGUO"),
                contar(resultados, "NAO_ENCONTRADO"),
                contar(resultados, "ERRO_CONSULTA"),
                resultados
        );
    }

    private ResultadoAuditoriaGeograficaDto auditarServico(ServicoGeograficoDto servico) {
        String enderecoConsultado = montarEndereco(servico);

        if (enderecoConsultado.isBlank()) {
            return resultadoBase(
                    servico,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "SEM_ENDERECO",
                    "O registro não possui endereço suficiente para geocodificação."
            );
        }

        try {
            JsonNode resposta = geoapifyClient.geocodificar(enderecoConsultado);
            JsonNode features = resposta.path("features");

            if (!features.isArray() || features.isEmpty()) {
                return resultadoBase(
                        servico,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "NAO_ENCONTRADO",
                        "A Geoapify não retornou resultados para o endereço consultado."
                );
            }

            JsonNode melhor = features.get(0).path("properties");

            BigDecimal latitude = GeoapifyClient.decimal(melhor, "lat");
            BigDecimal longitude = GeoapifyClient.decimal(melhor, "lon");

            String enderecoEncontrado = texto(melhor, "formatted");
            String municipioEncontrado = texto(melhor, "city");
            String cepEncontrado = texto(melhor, "postcode");

            double confianca = melhor.path("rank").path("confidence").asDouble(0.0);

            if (latitude == null || longitude == null
                    || !"br".equalsIgnoreCase(texto(melhor, "country_code"))) {

                return resultadoBase(
                        servico,
                        enderecoEncontrado,
                        municipioEncontrado,
                        cepEncontrado,
                        latitude,
                        longitude,
                        "RESULTADO_AMBIGUO",
                        "O resultado não possui coordenadas válidas ou não pertence ao Brasil."
                );
            }

            if (!municipioCompativel(servico.municipio(), municipioEncontrado)) {
                return resultadoBase(
                        servico,
                        enderecoEncontrado,
                        municipioEncontrado,
                        cepEncontrado,
                        latitude,
                        longitude,
                        "RESULTADO_AMBIGUO",
                        "O município retornado não corresponde ao município cadastrado."
                );
            }

            if (cepPreenchido(servico.cep()) && cepPreenchido(cepEncontrado)
                    && !somenteDigitos(servico.cep()).equals(somenteDigitos(cepEncontrado))) {

                return resultadoBase(
                        servico,
                        enderecoEncontrado,
                        municipioEncontrado,
                        cepEncontrado,
                        latitude,
                        longitude,
                        "RESULTADO_AMBIGUO",
                        "O CEP retornado é diferente do CEP cadastrado."
                );
            }

            if (confianca < CONFIANCA_MINIMA) {
                return resultadoBase(
                        servico,
                        enderecoEncontrado,
                        municipioEncontrado,
                        cepEncontrado,
                        latitude,
                        longitude,
                        "RESULTADO_AMBIGUO",
                        "A confiança informada pela geocodificação é baixa."
                );
            }

            if (servico.latitudeAtual() == null || servico.longitudeAtual() == null) {
                return resultadoBase(
                        servico,
                        enderecoEncontrado,
                        municipioEncontrado,
                        cepEncontrado,
                        latitude,
                        longitude,
                        "SEM_COORDENADAS",
                        "Foi encontrada uma referência geográfica, mas o registro não possui coordenadas atuais."
                );
            }

            double distancia = distanciaMetros(
                    servico.latitudeAtual().doubleValue(),
                    servico.longitudeAtual().doubleValue(),
                    latitude.doubleValue(),
                    longitude.doubleValue()
            );

            String classificacao;
            String justificativa;

            if (distancia <= DISTANCIA_COMPATIVEL_METROS) {
                classificacao = "COMPATIVEL";
                justificativa = "As coordenadas atuais estão a até 150 metros da referência encontrada.";

            } else if (distancia >= DISTANCIA_DIVERGENTE_METROS) {
                classificacao = "DIVERGENTE";
                justificativa = "As coordenadas atuais estão a pelo menos 500 metros da referência encontrada. A correção ainda exige validação humana.";

            } else {
                classificacao = "RESULTADO_AMBIGUO";
                justificativa = "A diferença está entre 150 e 500 metros; é necessária conferência.";
            }

            return new ResultadoAuditoriaGeograficaDto(
                    servico.idServico(),
                    servico.nome(),
                    enderecoConsultado,
                    enderecoEncontrado,
                    municipioEncontrado,
                    cepEncontrado,
                    servico.latitudeAtual(),
                    servico.longitudeAtual(),
                    latitude,
                    longitude,
                    BigDecimal.valueOf(distancia).setScale(2, RoundingMode.HALF_UP),
                    confianca,
                    classificacao,
                    justificativa
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return resultadoBase(
                    servico,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "ERRO_CONSULTA",
                    "A consulta foi interrompida."
            );

        } catch (IOException | RuntimeException e) {
            return resultadoBase(
                    servico,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "ERRO_CONSULTA",
                    "Falha ao consultar a Geoapify: " + e.getMessage()
            );
        }
    }

    private ResultadoAuditoriaGeograficaDto resultadoBase(
            ServicoGeograficoDto servico,
            String endereco,
            String municipio,
            String cep,
            BigDecimal latitude,
            BigDecimal longitude,
            String classificacao,
            String justificativa
    ) {
        return new ResultadoAuditoriaGeograficaDto(
                servico.idServico(),
                servico.nome(),
                montarEndereco(servico),
                endereco,
                municipio,
                cep,
                servico.latitudeAtual(),
                servico.longitudeAtual(),
                latitude,
                longitude,
                null,
                null,
                classificacao,
                justificativa
        );
    }

    private String montarEndereco(ServicoGeograficoDto servico) {
        List<String> partes = new ArrayList<>();

        adicionar(partes, servico.endereco());
        adicionar(partes, servico.bairro());
        adicionar(partes, servico.municipio());
        adicionar(partes, "SP");
        adicionar(partes, servico.cep());
        adicionar(partes, "Brasil");

        return String.join(", ", partes);
    }

    private void adicionar(List<String> partes, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(valor.trim());
        }
    }

    private boolean municipioCompativel(String cadastrado, String encontrado) {
        if (cadastrado == null || cadastrado.isBlank()
                || encontrado == null || encontrado.isBlank()) {
            return false;
        }

        return normalizar(cadastrado).equals(normalizar(encontrado));
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .trim();
    }

    private boolean cepPreenchido(String cep) {
        return cep != null && !cep.isBlank();
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String texto(JsonNode node, String campo) {
        JsonNode valor = node.path(campo);

        return valor.isMissingNode() || valor.isNull()
                ? null
                : valor.asText();
    }

    private long contar(List<ResultadoAuditoriaGeograficaDto> resultados, String classificacao) {
        return resultados.stream()
                .filter(r -> classificacao.equals(r.classificacao()))
                .count();
    }

    private double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double raioTerra = 6_371_000.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return raioTerra * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}