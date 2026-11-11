package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// nivelAcesso é opcional (padrão: ADMIN)
public class AdministradorEntradaDto {

    @NotNull(message = "O usuário é obrigatório")
    @Positive(message = "O id do usuário deve ser positivo")
    private Long idUsuario;

    @Pattern(regexp = "ADMIN|SUPERVISOR", message = "O nível de acesso deve ser ADMIN ou SUPERVISOR")
    private String nivelAcesso;

    public AdministradorEntradaDto() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}
