package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AdministradorAtualizarDto {

    @NotBlank(message = "O nível de acesso é obrigatório")
    @Pattern(regexp = "ADMIN|SUPERVISOR", message = "O nível de acesso deve ser ADMIN ou SUPERVISOR")
    private String nivelAcesso;

    public AdministradorAtualizarDto() {
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}
