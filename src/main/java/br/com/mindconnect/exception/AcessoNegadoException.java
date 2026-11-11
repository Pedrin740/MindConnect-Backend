package br.com.mindconnect.exception;

// Usuário autenticado, mas sem permissão para a operação ou para os dados de outra pessoa (HTTP 403)
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
