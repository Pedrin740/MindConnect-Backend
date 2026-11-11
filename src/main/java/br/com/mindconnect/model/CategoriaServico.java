package br.com.mindconnect.model;

// Tabela GS_CATEGORIA_SERVICO (lado 1 do relacionamento 1:N com servico)
public class CategoriaServico {

    private Long idCategoriaServico;

    private String nome;

    private String descricao;

    public CategoriaServico() {
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
