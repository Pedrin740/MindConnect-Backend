package br.com.mindconnect.geografia;

import java.math.BigDecimal;

public record ServicoGeograficoDto(
        Long idServico,
        String nome,
        String endereco,
        String bairro,
        String municipio,
        String cep,
        String codigoCnes,
        BigDecimal latitudeAtual,
        BigDecimal longitudeAtual
) {
}