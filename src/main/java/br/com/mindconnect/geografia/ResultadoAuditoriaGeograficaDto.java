package br.com.mindconnect.geografia;

import java.math.BigDecimal;

public record ResultadoAuditoriaGeograficaDto(
        Long idServico,
        String nome,
        String enderecoConsultado,
        String enderecoEncontrado,
        String municipioEncontrado,
        String cepEncontrado,
        BigDecimal latitudeAtual,
        BigDecimal longitudeAtual,
        BigDecimal latitudeEncontrada,
        BigDecimal longitudeEncontrada,
        BigDecimal distanciaMetros,
        Double confianca,
        String classificacao,
        String justificativa
) {
}