package br.com.mindconnect.geografia;

import java.util.List;

public record RelatorioComparacaoCnesDto(
        String codigoIbge,
        String municipio,
        int totalRegistrosCsv,
        int totalServicosCadastrados,
        int correspondenciasPorCnes,
        int servicosSemCorrespondenciaNoCsv,
        int registrosCsvSemCorrespondenciaNoBanco,
        int resultadosCompativeis,
        int resultadosComDivergencias,
        int resultadosIncompletos,
        int registrosSemCoordenadasCnes,
        List<ResultadoComparacaoCnesDto> resultados
) {
}