package br.com.mindconnect.dto;

public class CategoriaServicoSaidaDto {

    private Long idCategoriaServico;

    private String nome;

    private String descricao;

    public CategoriaServicoSaidaDto() {
    }

    public Long getIdCategoriaServico() {
        return idCategoriaServico;
    }

    public void setIdCategoriaServico(Long idCategoriaServico) {
        this.idCategoriaServico = idCategoriaServico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
