package br.com.mindconnect.dto;

import java.time.LocalDate;

public class ServicoAcolhimentoSaidaDto {

    private Long idServico;

    private Long idCategoriaServico;

    private String nomeCategoria;

    private String nome;

    private String endereco;

    private String bairro;

    private String regiao;

    private String telefone;

    private String horarioFuncionamento;

    private String descricaoServicos;

    private String fonteInformacao;

    private LocalDate dataVerificacao;

    public ServicoAcolhimentoSaidaDto() {
    }

    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }

    public Long getIdCategoriaServico() {
        return idCategoriaServico;
    }

    public void setIdCategoriaServico(Long idCategoriaServico) {
        this.idCategoriaServico = idCategoriaServico;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public String getDescricaoServicos() {
        return descricaoServicos;
    }

    public void setDescricaoServicos(String descricaoServicos) {
        this.descricaoServicos = descricaoServicos;
    }

    public String getFonteInformacao() {
        return fonteInformacao;
    }

    public void setFonteInformacao(String fonteInformacao) {
        this.fonteInformacao = fonteInformacao;
    }

    public LocalDate getDataVerificacao() {
        return dataVerificacao;
    }

    public void setDataVerificacao(LocalDate dataVerificacao) {
        this.dataVerificacao = dataVerificacao;
    }
}
