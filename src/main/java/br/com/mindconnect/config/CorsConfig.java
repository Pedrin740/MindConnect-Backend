package br.com.mindconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

// Libera o CORS para o front-end conseguir chamar a API.
//
// Variável de ambiente opcional CORS_ORIGINS (endereços separados por vírgula):
//   CORS_ORIGINS=https://meu-front.vercel.app,http://localhost:5173
// Com ela definida, SOMENTE esses endereços são aceitos.
// Sem ela, qualquer origem é aceita (a API não usa cookies nem sessão).
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origens = carregarOrigens();

        registry.addMapping("/**")
                .allowedOriginPatterns(origens.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .maxAge(3600);
    }

    private List<String> carregarOrigens() {
        List<String> origens = new ArrayList<>();
        String configuradas = System.getenv("CORS_ORIGINS");

        if (configuradas != null) {
            for (String origem : configuradas.split(",")) {
                String limpa = origem.trim();

                // remove a barra final (o navegador envia a origem sem ela)
                while (limpa.endsWith("/")) {
                    limpa = limpa.substring(0, limpa.length() - 1);
                }
                if (!limpa.isEmpty()) {
                    origens.add(limpa);
                }
            }
        }

        if (origens.isEmpty()) {
            origens.add("*");
        }
        return origens;
    }
}
