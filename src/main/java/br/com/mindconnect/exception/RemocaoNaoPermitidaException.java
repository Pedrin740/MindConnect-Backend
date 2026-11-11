package br.com.mindconnect.exception;

// Tentativa de remover um registro que ainda possui registros filhos (HTTP 409)
public class RemocaoNaoPermitidaException extends RegraNegocioException {

    public RemocaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
