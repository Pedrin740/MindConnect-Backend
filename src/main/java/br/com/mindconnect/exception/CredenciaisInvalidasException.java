package br.com.mindconnect.exception;

// E-mail ou senha incorretos no login (HTTP 401)
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos");
    }
}
