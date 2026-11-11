package br.com.mindconnect.dto;

import java.time.LocalDate;
import java.util.List;

public class ConteudoSaidaDto {

    private Long idConteudo;

    private String titulo;

    private String assunto;

    private String descricao;

    private String material;

    private String fonteInformacao;

    private LocalDate dataPublicacao;

    private String status;

    private List<CategoriaConteudoSaidaDto> categorias;

    public ConteudoSaidaDto() {
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

    public List<CategoriaConteudoSaidaDto> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaConteudoSaidaDto> categorias) {
        this.categorias = categorias;
    }
}
