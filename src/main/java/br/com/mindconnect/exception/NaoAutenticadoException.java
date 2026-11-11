package br.com.mindconnect.exception;

// Rota protegida chamada sem token ou com token inválido/expirado (HTTP 401)
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException() {
        super("Autenticação necessária. Faça login e envie o header Authorization: Bearer <token>.");
    }
}
