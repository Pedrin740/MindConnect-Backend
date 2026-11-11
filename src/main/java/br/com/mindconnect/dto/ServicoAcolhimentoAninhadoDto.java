package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Dados do servico SEM a categoria (usado na rota aninhada /categorias-servico/{id}/servicos)
public class ServicoAcolhimentoAninhadoDto {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    private String endereco;

    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "A região é obrigatória")
    @Size(max = 100, message = "A região deve ter no máximo 100 caracteres")
    private String regiao;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Pattern(regexp = "^[0-9()\\s+-]*$", message = "O telefone deve conter apenas números, espaços, parênteses, + e -")
    private String telefone;

    @Size(max = 150, message = "O horário deve ter no máximo 150 caracteres")
    private String horarioFuncionamento;

    @Size(max = 500, message = "A descrição dos serviços deve ter no máximo 500 caracteres")
    private String descricaoServicos;

    @NotBlank(message = "A fonte da informação é obrigatória")
    @Size(max = 300, message = "A fonte deve ter no máximo 300 caracteres")
    private String fonteInformacao;

    @PastOrPresent(message = "A data de verificação não pode ser futura")
    private LocalDate dataVerificacao;

    public ServicoAcolhimentoAninhadoDto() {
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
