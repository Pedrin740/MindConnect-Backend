package br.com.mindconnect.exception;

// Dado enviado é inválido para o sistema, por exemplo referência a um id que não existe (HTTP 400)
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
