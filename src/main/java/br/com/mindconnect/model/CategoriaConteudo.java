package br.com.mindconnect.model;

// Tabela GS_CATEGORIA_CONTEUDO
public class CategoriaConteudo {

    private Long idCategoriaConteudo;

    private String nome;

    private String descricao;

    public CategoriaConteudo() {
    }

    public Long getIdCategoriaConteudo() {
        return idCategoriaConteudo;
    }

    public void setIdCategoriaConteudo(Long idCategoriaConteudo) {
        this.idCategoriaConteudo = idCategoriaConteudo;
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
