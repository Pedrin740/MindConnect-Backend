package br.com.mindconnect.service;

import br.com.mindconnect.dao.AuditoriaGeograficaDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.geografia.AprovacaoCoordenadasRequest;
import br.com.mindconnect.geografia.GeoapifyClient;
import br.com.mindconnect.geografia.ServicoGeograficoDto;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class AprovacaoCoordenadasService {

    private static final double CONFIANCA_MINIMA = 0.75;
    private static final double DISTANCIA_MINIMA_CORRECAO = 500.0;
    private static final double TOLERANCIA_APROVACAO = 50.0;

    private final AuditoriaGeograficaDAO dao;
    private final GeoapifyClient geoapifyClient;

    public AprovacaoCoordenadasService(
            AuditoriaGeograficaDAO dao,
            GeoapifyClient geoapifyClient
    ) {
        this.dao = dao;
        this.geoapifyClient = geoapifyClient;
    }

    public void aprovar(Long idServico, AprovacaoCoordenadasRequest request) {
        if (idServico == null || idServico <= 0) {
            throw new RegraNegocioException("O identificador do serviço é inválido.");
        }

        validarCoordenadas(request.latitude(), request.longitude());

        ServicoGeograficoDto servico = dao.buscarPorId(idServico);

        if (servico == null) {
            throw new RecursoNaoEncontradoException("Serviço de acolhimento não encontrado.");
        }

        validarSituacaoAtual(servico);

        Candidato candidato = buscarCandidatoValidado(servico);

        double distanciaCandidato = distanciaMetros(
                request.latitude().doubleValue(),
                request.longitude().doubleValue(),
                candidato.latitude().doubleValue(),
                candidato.longitude().doubleValue()
        );

        if (distanciaCandidato > TOLERANCIA_APROVACAO) {
            throw new RegraNegocioException(
                    "As coordenadas enviadas não correspondem à referência geográfica validada. "
                            + "Diferença encontrada: "
                            + Math.round(distanciaCandidato)
                            + " metros."
            );
        }

        boolean atualizado = dao.atualizarCoordenadasSeInalteradas(
                idServico,
                servico.latitudeAtual(),
                servico.longitudeAtual(),
                request.latitude(),
                request.longitude()
        );

        if (!atualizado) {
            throw new RegraNegocioException(
                    "As coordenadas do serviço foram alteradas durante a aprovação. "
                            + "Execute novamente a auditoria antes de tentar outra atualização."
            );
        }
    }

    private void validarSituacaoAtual(ServicoGeograficoDto servico) {
        BigDecimal latitude = servico.latitudeAtual();
        BigDecimal longitude = servico.longitudeAtual();

        if ((latitude == null) != (longitude == null)) {
            throw new RegraNegocioException(
                    "O registro possui apenas uma coordenada preenchida. "
                            + "É necessária uma conferência manual antes da atualização."
            );
        }

        if (latitude == null) {
            return;
        }

        validarCoordenadas(latitude, longitude);

        // Não permite sobrescrever coordenadas já compatíveis ou ambíguas.
        // A correção automática pelo endpoint fica restrita a divergências
        // superiores ou iguais a 500 metros.
    }

    private Candidato buscarCandidatoValidado(ServicoGeograficoDto servico) {
        String endereco = montarEndereco(servico);

        try {
            JsonNode resposta = geoapifyClient.geocodificar(endereco);
            JsonNode features = resposta.path("features");

            if (!features.isArray() || features.isEmpty()) {
                throw new RegraNegocioException(
                        "A Geoapify não encontrou uma referência para o endereço."
                );
            }

            Candidato melhor = null;

            for (JsonNode feature : features) {
                JsonNode propriedades = feature.path("properties");

                if (!"br".equalsIgnoreCase(
                        propriedades.path("country_code").asText()
                )) {
                    continue;
                }

                String municipio = texto(propriedades, "city");
                String cep = texto(propriedades, "postcode");

                if (!municipioCompativel(servico.municipio(), municipio)) {
                    continue;
                }

                if (cepPreenchido(servico.cep())
                        && cepPreenchido(cep)
                        && !somenteDigitos(servico.cep())
                        .equals(somenteDigitos(cep))) {
                    continue;
                }

                BigDecimal latitude = GeoapifyClient.decimal(propriedades, "lat");
                BigDecimal longitude = GeoapifyClient.decimal(propriedades, "lon");

                if (latitude == null || longitude == null) {
                    continue;
                }

                try {
                    validarCoordenadas(latitude, longitude);
                } catch (RegraNegocioException e) {
                    continue;
                }

                double confianca = propriedades.path("rank")
                        .path("confidence")
                        .asDouble(0.0);

                if (confianca < CONFIANCA_MINIMA) {
                    continue;
                }

                Candidato candidato = new Candidato(
                        latitude,
                        longitude,
                        confianca
                );

                if (melhor == null || candidato.confianca() > melhor.confianca()) {
                    melhor = candidato;
                }
            }

            if (melhor == null) {
                throw new RegraNegocioException(
                        "Nenhum resultado da Geoapify passou pelas validações de país, "
                                + "município, CEP, coordenadas e confiança."
                );
            }

            return melhor;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegraNegocioException(
                    "A consulta geográfica foi interrompida."
            );

        } catch (IOException e) {
            throw new RegraNegocioException(
                    "Não foi possível validar novamente o endereço na Geoapify."
            );
        }
    }

    private String montarEndereco(ServicoGeograficoDto servico) {
        StringBuilder endereco = new StringBuilder();

        adicionar(endereco, servico.endereco());
        adicionar(endereco, servico.bairro());
        adicionar(endereco, servico.municipio());
        adicionar(endereco, "SP");
        adicionar(endereco, servico.cep());
        adicionar(endereco, "Brasil");

        if (endereco.isEmpty()) {
            throw new RegraNegocioException(
                    "O serviço não possui endereço suficiente para validação."
            );
        }

        return endereco.toString();
    }

    private void adicionar(StringBuilder endereco, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }

        if (!endereco.isEmpty()) {
            endereco.append(", ");
        }

        endereco.append(valor.trim());
    }

    private void validarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null
                || latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0
                || longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {

            throw new RegraNegocioException(
                    "As coordenadas informadas são inválidas."
            );
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

    private double distanciaMetros(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        double raioTerra = 6_371_000.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return raioTerra * 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );
    }

    private record Candidato(
            BigDecimal latitude,
            BigDecimal longitude,
            double confianca
    ) {
    }
}
