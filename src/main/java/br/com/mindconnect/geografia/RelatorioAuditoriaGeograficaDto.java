package br.com.mindconnect.geografia;

import java.util.List;

public record RelatorioAuditoriaGeograficaDto(
        int pagina,
        int limite,
        int quantidadeAnalisada,
        long compativeis,
        long divergentes,
        long semCoordenadas,
        long resultadosAmbiguos,
        long naoEncontrados,
        long errosConsulta,
        List<ResultadoAuditoriaGeograficaDto> resultados
) {
}
