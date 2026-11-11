package br.com.mindconnect.exception;

// Falha ao acessar o banco. A mensagem NÃO vai para o cliente (HTTP 500 genérico);
// o detalhe técnico (causa) fica apenas no log do servidor.
public class ErroBancoDadosException extends RuntimeException {

    public ErroBancoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
