package br.com.mindconnect.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Marca em cada rota quem pode chamá-la. Quem confere é o AutorizacaoInterceptor.
// Rota sem esta anotação é tratada como exclusiva de administradores (seguro por padrão).
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Acesso {
    Nivel value();
}
