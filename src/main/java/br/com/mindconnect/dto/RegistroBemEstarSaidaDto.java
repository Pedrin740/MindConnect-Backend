package br.com.mindconnect.dto;

import java.time.LocalDate;

public class RegistroBemEstarSaidaDto {

    private Long idRegistro;

    private Long idUsuario;

    private LocalDate dataRegistro;

    private String humor;

    private Integer nivelHumor;

    private String sentimentos;

    private String observacoes;

    public RegistroBemEstarSaidaDto() {
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Long idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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
