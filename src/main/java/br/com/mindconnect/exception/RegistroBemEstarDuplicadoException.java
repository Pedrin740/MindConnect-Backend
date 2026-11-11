package br.com.mindconnect.exception;

import java.time.LocalDate;

// Regra de negócio: apenas um registro de bem-estar por usuário por dia (HTTP 409)
public class RegistroBemEstarDuplicadoException extends RegraNegocioException {

    public RegistroBemEstarDuplicadoException(LocalDate data) {
        super("O usuário já possui um registro de bem-estar no dia " + data);
    }
}
