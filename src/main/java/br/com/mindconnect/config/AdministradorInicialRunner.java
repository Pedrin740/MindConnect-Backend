package br.com.mindconnect.config;

import br.com.mindconnect.service.AdministradorInicialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Na inicialização, se as variáveis ADMIN_EMAIL e ADMIN_SENHA existirem, garante esse administrador.
// Qualquer falha (ex.: banco indisponível) só gera um aviso no log e não derruba a aplicação.
@Component
public class AdministradorInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicialRunner.class);

    private final AdministradorInicialService service;

    public AdministradorInicialRunner(AdministradorInicialService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = System.getenv("ADMIN_EMAIL");
        String senha = System.getenv("ADMIN_SENHA");

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return;
        }
        if (!email.contains("@") || senha.length() < 6) {
            log.warn("ADMIN_EMAIL/ADMIN_SENHA ignoradas: e-mail inválido ou senha com menos de 6 caracteres.");
            return;
        }

        try {
            service.garantir(email, senha);
            log.info("Administrador inicial garantido para o e-mail configurado em ADMIN_EMAIL.");
        } catch (RuntimeException e) {
            log.warn("Não foi possível garantir o administrador inicial: {}", e.getMessage());
        }
    }
}
