package br.com.mindconnect.geografia;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class GeoapifyClient {

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeoapifyClient(@Value("${geoapify.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode geocodificar(String endereco) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("A variável GEOAPIFY_API_KEY não foi configurada.");
        }

        String texto = URLEncoder.encode(endereco, StandardCharsets.UTF_8);
        String chave = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String url = "https://api.geoapify.com/v1/geocode/search?text=" + texto
                + "&filter=countrycode:br&lang=pt&limit=5&apiKey=" + chave;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Geoapify respondeu com HTTP " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    public static BigDecimal decimal(JsonNode node, String campo) {
        JsonNode valor = node.path(campo);
        return valor.isNumber() ? valor.decimalValue() : null;
    }
}