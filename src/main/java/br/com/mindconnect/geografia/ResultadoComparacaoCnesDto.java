package br.com.mindconnect.geografia;

import java.math.BigDecimal;

public record ResultadoComparacaoCnesDto(
        Long idServico,
        String codigoCnes,
        String nomeCadastrado,
        String nomeCnes,
        String enderecoCadastrado,
        String enderecoCnes,
        String bairroCadastrado,
        String bairroCnes,
        String cepCadastrado,
        String cepCnes,
        BigDecimal latitudeCadastrada,
        BigDecimal longitudeCadastrada,
        BigDecimal latitudeCnes,
        BigDecimal longitudeCnes,
        BigDecimal distanciaMetros,
        String classificacao,
        String divergencias
) {
}