package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FavoritoEntradaDto {

    @NotNull(message = "O conteúdo é obrigatório")
    @Positive(message = "O id do conteúdo deve ser positivo")
    private Long idConteudo;

    public FavoritoEntradaDto() {
    }

    public Long getIdConteudo() {
        return idConteudo;
    }

    public void setIdConteudo(Long idConteudo) {
        this.idConteudo = idConteudo;
    }
}
