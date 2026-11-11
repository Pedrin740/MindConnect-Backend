package br.com.mindconnect.model;

import java.time.LocalDate;

// Tabela GS_FAVORITO (chave composta idUsuario + idConteudo; titulo e assunto vem do JOIN)
public class Favorito {

    private Long idUsuario;

    private Long idConteudo;

    private LocalDate dataFavorito;

    private String tituloConteudo;

    private String assuntoConteudo;

    public Favorito() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdConteudo() {
        return idConteudo;
    }

    public void setIdConteudo(Long idConteudo) {
        this.idConteudo = idConteudo;
    }

    public LocalDate getDataFavorito() {
        return dataFavorito;
    }

    public void setDataFavorito(LocalDate dataFavorito) {
        this.dataFavorito = dataFavorito;
    }

    public String getTituloConteudo() {
        return tituloConteudo;
    }

    public void setTituloConteudo(String tituloConteudo) {
        this.tituloConteudo = tituloConteudo;
    }

    public String getAssuntoConteudo() {
        return assuntoConteudo;
    }

    public void setAssuntoConteudo(String assuntoConteudo) {
        this.assuntoConteudo = assuntoConteudo;
    }
}
