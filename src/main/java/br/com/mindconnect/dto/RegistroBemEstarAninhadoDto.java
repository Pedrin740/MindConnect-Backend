package br.com.mindconnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Dados do registro SEM o usuario (usado no PUT e na rota aninhada /usuarios/{id}/bem-estar). dataRegistro é opcional (padrão: hoje)
public class RegistroBemEstarAninhadoDto {

    @PastOrPresent(message = "A data do registro não pode ser futura")
    private LocalDate dataRegistro;

    @NotBlank(message = "O humor é obrigatório")
    @Size(max = 50, message = "O humor deve ter no máximo 50 caracteres")
    private String humor;

    @NotNull(message = "O nível de humor é obrigatório")
    @Min(value = 1, message = "O nível de humor deve ser de 1 a 5")
    @Max(value = 5, message = "O nível de humor deve ser de 1 a 5")
    private Integer nivelHumor;

    @Size(max = 200, message = "Os sentimentos devem ter no máximo 200 caracteres")
    private String sentimentos;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    public RegistroBemEstarAninhadoDto() {
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getHumor() {
        return humor;
    }

    public void setHumor(String humor) {
        this.humor = humor;
    }

    public Integer getNivelHumor() {
        return nivelHumor;
    }

    public void setNivelHumor(Integer nivelHumor) {
        this.nivelHumor = nivelHumor;
    }

    public String getSentimentos() {
        return sentimentos;
    }

    public void setSentimentos(String sentimentos) {
        this.sentimentos = sentimentos;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
