package br.com.mindconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContatoEntradaDto {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Pattern(
            regexp = "^(?!\\.)(?!.*\\.\\.)[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+(?<!\\.)@(?:[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])(?:\\.[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])*\\.[A-Za-z]{2,63}$",
            message = "Informe um endereço de e-mail válido, como usuario@gmail.com"
    )
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "O motivo é obrigatório")
    @Size(max = 100, message = "O motivo deve ter no máximo 100 caracteres")
    private String motivo;

    @NotBlank(message = "A mensagem é obrigatória")
    @Size(min = 10, max = 1000, message = "A mensagem deve ter entre 10 e 1000 caracteres")
    private String mensagem;

    public ContatoEntradaDto() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
