package br.com.mindconnect.geografia;

import java.util.List;

public record RelatorioCorrecaoLoteDto(
        int pagina,
        int limite,
        int analisados,
        int atualizados,
        int jaCorretos,
        int semCorrespondenciaSegura,
        int erros,
        List<ItemCorrecaoLoteDto> resultados
) {
    public record ItemCorrecaoLoteDto(
            Long idServico,
            String nome,
            String enderecoEncontrado,
            String cepEncontrado,
            String classificacao,
            String motivo
    ) {}
}
