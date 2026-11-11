package br.com.mindconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

// status e dataPublicacao são opcionais (padrão: ATIVO e a data de hoje)
public class ConteudoEntradaDto {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String titulo;

    @NotBlank(message = "O assunto é obrigatório")
    @Size(max = 150, message = "O assunto deve ter no máximo 150 caracteres")
    private String assunto;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotBlank(message = "O material é obrigatório")
    @Size(max = 20000, message = "O material deve ter no máximo 20000 caracteres")
    private String material;

    @NotBlank(message = "A fonte da informação é obrigatória")
    @Size(max = 300, message = "A fonte deve ter no máximo 300 caracteres")
    private String fonteInformacao;

    private LocalDate dataPublicacao;

    @Pattern(regexp = "ATIVO|INATIVO", message = "O status deve ser ATIVO ou INATIVO")
    private String status;

    @Size(max = 10, message = "Informe no máximo 10 categorias")
    private List<Long> idsCategorias;

    public ConteudoEntradaDto() {
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

    public List<Long> getIdsCategorias() {
        return idsCategorias;
    }

    public void setIdsCategorias(List<Long> idsCategorias) {
        this.idsCategorias = idsCategorias;
    }
}
