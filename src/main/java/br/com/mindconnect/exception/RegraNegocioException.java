package br.com.mindconnect.exception;

// Regra de negócio violada ou remoção de registro que possui filhos (HTTP 409)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
