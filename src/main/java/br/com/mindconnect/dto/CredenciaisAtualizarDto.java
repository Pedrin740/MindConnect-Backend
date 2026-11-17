package br.com.mindconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CredenciaisAtualizarDto {

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "A senha atual é obrigatória.")
    @Size(max = 72, message = "A senha atual deve ter no máximo 72 caracteres.")
    private String senhaAtual;

    @Size(
            min = 6,
            max = 72,
            message = "A nova senha deve ter entre 6 e 72 caracteres."
    )
    private String novaSenha;

    public CredenciaisAtualizarDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}