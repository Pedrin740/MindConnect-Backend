package br.com.mindconnect.service;

import br.com.mindconnect.geografia.GeoapifyClient;
import br.com.mindconnect.geografia.GeocodificacaoResponse;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class GeocodificacaoService {

    private final GeoapifyClient geoapifyClient;

    public GeocodificacaoService(GeoapifyClient geoapifyClient) {
        this.geoapifyClient = geoapifyClient;
    }

    public GeocodificacaoResponse geocodificar(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            throw new IllegalArgumentException("O endereço é obrigatório.");
        }

        if (endereco.length() > 500) {
            throw new IllegalArgumentException("O endereço deve ter no máximo 500 caracteres.");
        }

        try {
            JsonNode resposta = geoapifyClient.geocodificar(endereco.trim());
            JsonNode features = resposta.path("features");

            if (!features.isArray() || features.isEmpty()) {
                return null;
            }

            for (JsonNode feature : features) {
                JsonNode propriedades = feature.path("properties");

                if (!"br".equalsIgnoreCase(propriedades.path("country_code").asText())) {
                    continue;
                }

                BigDecimal latitude = GeoapifyClient.decimal(propriedades, "lat");
                BigDecimal longitude = GeoapifyClient.decimal(propriedades, "lon");

                if (latitude == null || longitude == null) {
                    continue;
                }

                double confianca = propriedades.path("rank")
                        .path("confidence")
                        .asDouble(0.0);

                if (confianca < 0.50) {
                    continue;
                }

                return new GeocodificacaoResponse(
                        latitude,
                        longitude,
                        texto(propriedades, "formatted"),
                        texto(propriedades, "city"),
                        texto(propriedades, "postcode"),
                        confianca
                );
            }

            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A consulta geográfica foi interrompida.", e);

        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível consultar a Geoapify.", e);
        }
    }

    private String texto(JsonNode node, String campo) {
        JsonNode valor = node.path(campo);

        return valor.isMissingNode() || valor.isNull()
                ? null
                : valor.asText();
    }
}