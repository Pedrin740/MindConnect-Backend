package br.com.mindconnect.exception;

// Registro procurado não existe (HTTP 404)
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
