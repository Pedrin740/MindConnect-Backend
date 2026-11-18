
package br.com.mindconnect.service;

import br.com.mindconnect.dao.AuditoriaGeograficaDAO;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.geografia.RelatorioComparacaoCnesDto;
import br.com.mindconnect.geografia.ResultadoComparacaoCnesDto;
import br.com.mindconnect.geografia.ServicoGeograficoDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ComparacaoCnesService {

    private static final String ARQUIVO_CNES =
            "cnes/estabelecimentos-355030.csv";

    private static final String IBGE_CAPITAL = "355030";

    private static final double TOLERANCIA_COORDENADAS_METROS = 150.0;

    /*
     * Coordenada genérica identificada no CSV oficial.
     *
     * Foram encontrados 512 registros com coordenadas
     * a até 5 metros deste ponto.
     *
     * Essas coordenadas não serão consideradas referências
     * geográficas confiáveis para aprovação.
     */
    private static final BigDecimal LATITUDE_GENERICA =
            new BigDecimal("-23.548");

    private static final BigDecimal LONGITUDE_GENERICA =
            new BigDecimal("-46.636");

    private static final double TOLERANCIA_COORDENADA_GENERICA_METROS =
            5.0;

    private final AuditoriaGeograficaDAO dao;

    public ComparacaoCnesService(AuditoriaGeograficaDAO dao) {
        this.dao = dao;
    }

    public RelatorioComparacaoCnesDto comparar() {

        Map<String, RegistroCnes> registrosCnes = lerCsv();

        List<ServicoGeograficoDto> servicos =
                dao.listarServicosDaCapitalParaComparacaoCnes();

        List<ResultadoComparacaoCnesDto> resultados =
                new ArrayList<>();

        Set<String> codigosEncontrados = new HashSet<>();

        int compativeis = 0;
        int divergentes = 0;
        int incompletos = 0;
        int semCoordenadasCnes = 0;

        for (ServicoGeograficoDto servico : servicos) {

            String codigo = normalizarCodigo(servico.codigoCnes());

            if (codigo.isBlank()) {
                continue;
            }

            RegistroCnes referencia = registrosCnes.get(codigo);

            if (referencia == null) {
                continue;
            }

            codigosEncontrados.add(codigo);

            ResultadoComparacaoCnesDto resultado =
                    compararRegistro(servico, referencia);

            resultados.add(resultado);

            switch (resultado.classificacao()) {
                case "COMPATIVEL" -> compativeis++;
                case "INCOMPLETO" -> incompletos++;
                default -> divergentes++;
            }

            if (referencia.latitude() == null
                    || referencia.longitude() == null) {
                semCoordenadasCnes++;
            }
        }

        int registrosCsvSemCorrespondencia =
                (int) registrosCnes.keySet().stream()
                        .filter(codigo -> !codigosEncontrados.contains(codigo))
                        .count();

        return new RelatorioComparacaoCnesDto(
                IBGE_CAPITAL,
                "São Paulo",
                registrosCnes.size(),
                servicos.size(),
                resultados.size(),
                servicos.size() - resultados.size(),
                registrosCsvSemCorrespondencia,
                compativeis,
                divergentes,
                incompletos,
                semCoordenadasCnes,
                resultados
        );
    }

    private Map<String, RegistroCnes> lerCsv() {

        Map<String, RegistroCnes> registros = new HashMap<>();

        ClassPathResource arquivo =
                new ClassPathResource(ARQUIVO_CNES);

        if (!arquivo.exists()) {
            throw new RequisicaoInvalidaException(
                    "Arquivo oficial do CNES não encontrado em resources/cnes."
            );
        }

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                arquivo.getInputStream(),
                                StandardCharsets.ISO_8859_1
                        )
                )
        ) {

            String cabecalho = reader.readLine();

            if (cabecalho == null || cabecalho.isBlank()) {
                throw new RequisicaoInvalidaException(
                        "O arquivo CSV está vazio."
                );
            }

            List<String> colunas = parseLinhaCsv(cabecalho);

            Map<String, Integer> indices = new HashMap<>();

            for (int i = 0; i < colunas.size(); i++) {

                String coluna = colunas.get(i)
                        .replace("\uFEFF", "")
                        .trim();

                indices.put(coluna, i);
            }

            validarCabecalhos(indices);

            String linha;

            while ((linha = reader.readLine()) != null) {

                if (linha.isBlank()) {
                    continue;
                }

                List<String> campos = parseLinhaCsv(linha);

                LinhaCnes registroLinha = new LinhaCnes(
                        campos,
                        indices
                );

                String ibge = somenteDigitos(
                        valor(registroLinha, "IBGE")
                );

                String uf = valor(registroLinha, "UF")
                        .toUpperCase(Locale.ROOT);

                if (!IBGE_CAPITAL.equals(ibge) || !"SP".equals(uf)) {
                    continue;
                }

                String codigo = normalizarCodigo(
                        valor(registroLinha, "CNES")
                );

                if (codigo.isBlank()) {
                    continue;
                }

                String latitudeTexto =
                        valor(registroLinha, "LATITUDE");

                String longitudeTexto =
                        valor(registroLinha, "LONGITUDE");

                BigDecimal latitude = decimalCoordenada(
                        latitudeTexto,
                        -90,
                        90
                );

                BigDecimal longitude = decimalCoordenada(
                        longitudeTexto,
                        -180,
                        180
                );

                /*
                 * Impede que coordenadas genéricas sejam utilizadas
                 * como referência oficial para corrigir o mapa.
                 *
                 * Ambas as coordenadas são descartadas juntas.
                 */
                if (coordenadasGenericas(latitude, longitude)) {
                    latitude = null;
                    longitude = null;
                }

                RegistroCnes registro = new RegistroCnes(
                        codigo,
                        valor(registroLinha, "NOME FANTASIA"),
                        valor(registroLinha, "LOGRADOURO"),
                        valor(registroLinha, "NUMERO"),
                        valor(registroLinha, "BAIRRO"),
                        valor(registroLinha, "CEP"),
                        latitude,
                        longitude
                );

                if (registros.putIfAbsent(codigo, registro) != null) {
                    throw new RequisicaoInvalidaException(
                            "O CSV contém código CNES duplicado: " + codigo
                    );
                }
            }

            if (registros.isEmpty()) {
                throw new RequisicaoInvalidaException(
                        "Nenhum estabelecimento da capital foi encontrado no CSV."
                );
            }

            return registros;

        } catch (IOException e) {
            throw new RequisicaoInvalidaException(
                    "Não foi possível ler o arquivo oficial do CNES."
            );
        }
    }

    private void validarCabecalhos(
            Map<String, Integer> cabecalhos
    ) {

        String[] obrigatorios = {
                "IBGE",
                "UF",
                "CNES",
                "NOME FANTASIA",
                "LOGRADOURO",
                "NUMERO",
                "BAIRRO",
                "CEP",
                "LATITUDE",
                "LONGITUDE"
        };

        for (String coluna : obrigatorios) {

            if (!cabecalhos.containsKey(coluna)) {
                throw new RequisicaoInvalidaException(
                        "Coluna obrigatória ausente no CSV: " + coluna
                );
            }
        }
    }

    private ResultadoComparacaoCnesDto compararRegistro(
            ServicoGeograficoDto servico,
            RegistroCnes cnes
    ) {

        List<String> divergencias = new ArrayList<>();

        if (!normalizarTexto(servico.endereco())
                .equals(normalizarTexto(cnes.enderecoCompleto()))) {
            divergencias.add("ENDERECO");
        }

        if (!normalizarTexto(servico.bairro())
                .equals(normalizarTexto(cnes.bairro()))) {
            divergencias.add("BAIRRO");
        }

        if (!somenteDigitos(servico.cep())
                .equals(somenteDigitos(cnes.cep()))) {
            divergencias.add("CEP");
        }

        BigDecimal distancia = null;

        boolean bancoTemCoordenadas =
                servico.latitudeAtual() != null
                        && servico.longitudeAtual() != null;

        boolean cnesTemCoordenadas =
                cnes.latitude() != null
                        && cnes.longitude() != null;

        if (bancoTemCoordenadas && cnesTemCoordenadas) {

            double metros = distanciaMetros(
                    servico.latitudeAtual().doubleValue(),
                    servico.longitudeAtual().doubleValue(),
                    cnes.latitude().doubleValue(),
                    cnes.longitude().doubleValue()
            );

            distancia = BigDecimal.valueOf(metros)
                    .setScale(2, RoundingMode.HALF_UP);

            if (metros > TOLERANCIA_COORDENADAS_METROS) {
                divergencias.add("COORDENADAS");
            }

        } else {
            divergencias.add("COORDENADAS_INDISPONIVEIS");
        }

        boolean somenteFaltaCoordenada =
                divergencias.size() == 1
                        && divergencias.contains("COORDENADAS_INDISPONIVEIS");

        String classificacao = divergencias.isEmpty()
                ? "COMPATIVEL"
                : somenteFaltaCoordenada
                  ? "INCOMPLETO"
                  : "DIVERGENTE";

        String descricao = divergencias.isEmpty()
                ? "Os dados comparáveis estão compatíveis com o CNES."
                : String.join(", ", divergencias);

        return new ResultadoComparacaoCnesDto(
                servico.idServico(),
                cnes.codigo(),
                servico.nome(),
                cnes.nome(),
                servico.endereco(),
                cnes.enderecoCompleto(),
                servico.bairro(),
                cnes.bairro(),
                servico.cep(),
                cnes.cep(),
                servico.latitudeAtual(),
                servico.longitudeAtual(),
                cnes.latitude(),
                cnes.longitude(),
                distancia,
                classificacao,
                descricao
        );
    }

    /*
     * Verifica se o par de coordenadas corresponde ao ponto genérico
     * identificado no arquivo CNES.
     *
     * A distância é utilizada para absorver pequenas variações
     * de casas decimais, sem descartar outras localizações.
     */
    private boolean coordenadasGenericas(
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        if (latitude == null || longitude == null) {
            return false;
        }

        double distancia = distanciaMetros(
                latitude.doubleValue(),
                longitude.doubleValue(),
                LATITUDE_GENERICA.doubleValue(),
                LONGITUDE_GENERICA.doubleValue()
        );

        return distancia <= TOLERANCIA_COORDENADA_GENERICA_METROS;
    }

    private String valor(
            LinhaCnes linha,
            String coluna
    ) {

        Integer indice = linha.indices().get(coluna);

        if (indice == null || indice >= linha.campos().size()) {
            return "";
        }

        String resultado = linha.campos().get(indice);

        return resultado == null ? "" : resultado.trim();
    }

    private BigDecimal decimalCoordenada(
            String valor,
            int minimo,
            int maximo
    ) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {

            BigDecimal numero = new BigDecimal(
                    valor.trim().replace(',', '.')
            );

            if (numero.compareTo(BigDecimal.valueOf(minimo)) < 0
                    || numero.compareTo(BigDecimal.valueOf(maximo)) > 0) {
                return null;
            }

            return numero;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizarCodigo(String codigo) {
        return somenteDigitos(codigo);
    }

    private String somenteDigitos(String valor) {

        return valor == null
                ? ""
                : valor.replaceAll("\\D", "");
    }

    private String normalizarTexto(String valor) {

        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(
                        valor,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private double distanciaMetros(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        double raioTerra = 6_371_000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return raioTerra * 2
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<String> parseLinhaCsv(String linha) {

        List<String> campos = new ArrayList<>();

        StringBuilder campo = new StringBuilder();

        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {

            char caractere = linha.charAt(i);

            if (caractere == '"') {

                if (dentroDeAspas
                        && i + 1 < linha.length()
                        && linha.charAt(i + 1) == '"') {

                    campo.append('"');
                    i++;

                } else {
                    dentroDeAspas = !dentroDeAspas;
                }

            } else if (caractere == ';' && !dentroDeAspas) {

                campos.add(campo.toString());
                campo.setLength(0);

            } else {
                campo.append(caractere);
            }
        }

        if (dentroDeAspas) {
            throw new RequisicaoInvalidaException(
                    "O CSV contém um campo com aspas não fechadas."
            );
        }

        campos.add(campo.toString());

        return campos;
    }

    private record LinhaCnes(
            List<String> campos,
            Map<String, Integer> indices
    ) {
    }

    private record RegistroCnes(
            String codigo,
            String nome,
            String logradouro,
            String numero,
            String bairro,
            String cep,
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        private String enderecoCompleto() {

            return (
                    logradouro
                            + (numero.isBlank() ? "" : ", " + numero)
            ).trim();
        }
    }
}
