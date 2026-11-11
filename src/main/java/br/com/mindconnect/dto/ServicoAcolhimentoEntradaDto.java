package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ServicoAcolhimentoEntradaDto extends ServicoAcolhimentoAninhadoDto {

    @NotNull(message = "A categoria do serviço é obrigatória")
    @Positive(message = "O id da categoria deve ser positivo")
    private Long idCategoriaServico;

    public ServicoAcolhimentoEntradaDto() {
    }

    public Long getIdCategoriaServico() {
        return idCategoriaServico;
    }

    public void setIdCategoriaServico(Long idCategoriaServico) {
        this.idCategoriaServico = idCategoriaServico;
    }
}
