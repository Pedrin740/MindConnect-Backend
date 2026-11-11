package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RegistroBemEstarEntradaDto extends RegistroBemEstarAninhadoDto {

    @NotNull(message = "O usuário é obrigatório")
    @Positive(message = "O id do usuário deve ser positivo")
    private Long idUsuario;

    public RegistroBemEstarEntradaDto() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
