package br.com.mindconnect.model;

import java.time.LocalDate;
import java.util.List;

// Tabela GS_CONTEUDO (as categorias vem da tabela de associacao GS_CONTEUDO_CATEGORIA)
public class Conteudo {

    private Long idConteudo;

    private String titulo;

    private String assunto;

    private String descricao;

    private String material;

    private String fonteInformacao;

    private LocalDate dataPublicacao;

    private String status;

    private List<CategoriaConteudo> categorias;

    public Conteudo() {
    }

    public Long getIdConteudo() {
        return idConteudo;
    }

    public void setIdConteudo(Long idConteudo) {
        this.idConteudo = idConteudo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getFonteInformacao() {
        return fonteInformacao;
    }

    public void setFonteInformacao(String fonteInformacao) {
        this.fonteInformacao = fonteInformacao;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CategoriaConteudo> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaConteudo> categorias) {
        this.categorias = categorias;
    }
}
