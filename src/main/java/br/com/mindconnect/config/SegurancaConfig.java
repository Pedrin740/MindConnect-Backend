package br.com.mindconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Liga o interceptor de autorização em todas as rotas da API
@Configuration
public class SegurancaConfig implements WebMvcConfigurer {

    private final AutorizacaoInterceptor autorizacaoInterceptor;

    public SegurancaConfig(AutorizacaoInterceptor autorizacaoInterceptor) {
        this.autorizacaoInterceptor = autorizacaoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacaoInterceptor).addPathPatterns("/api/**");
    }
}
