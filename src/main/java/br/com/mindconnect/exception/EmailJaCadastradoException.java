package br.com.mindconnect.exception;

// Regra de negócio: o e-mail do usuário precisa ser único (HTTP 409)
public class EmailJaCadastradoException extends RegraNegocioException {

    public EmailJaCadastradoException(String email) {
        super("Já existe um usuário cadastrado com o e-mail " + email);
    }
}
