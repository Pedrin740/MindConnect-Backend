package br.com.mindconnect.dto;

import java.time.LocalDate;

public class FavoritoSaidaDto {

    private Long idUsuario;

    private Long idConteudo;

    private LocalDate dataFavorito;

    private String tituloConteudo;

    private String assuntoConteudo;

    public FavoritoSaidaDto() {
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
