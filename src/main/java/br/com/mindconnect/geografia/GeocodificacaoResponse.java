package br.com.mindconnect.geografia;

import java.math.BigDecimal;

public record GeocodificacaoResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        String enderecoEncontrado,
        String municipio,
        String cep,
        Double confianca
) {
}