package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.geografia.AprovacaoCoordenadasRequest;
import br.com.mindconnect.geografia.RelatorioAuditoriaGeograficaDto;
import br.com.mindconnect.geografia.RelatorioComparacaoCnesDto;
import br.com.mindconnect.service.AprovacaoCoordenadasService;
import br.com.mindconnect.service.AuditoriaGeograficaService;
import br.com.mindconnect.service.ComparacaoCnesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicos/auditoria-geografica")
public class AuditoriaGeograficaController {

    private final AuditoriaGeograficaService auditoriaService;
    private final AprovacaoCoordenadasService aprovacaoService;
    private final ComparacaoCnesService comparacaoCnesService;

    public AuditoriaGeograficaController(
            AuditoriaGeograficaService auditoriaService,
            AprovacaoCoordenadasService aprovacaoService,
            ComparacaoCnesService comparacaoCnesService
    ) {
        this.auditoriaService = auditoriaService;
        this.aprovacaoService = aprovacaoService;
        this.comparacaoCnesService = comparacaoCnesService;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<RelatorioAuditoriaGeograficaDto> auditar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int limite
    ) {
        return ResponseEntity.ok(
                auditoriaService.auditar(pagina, limite)
        );
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping("/cnes/comparar")
    public ResponseEntity<RelatorioComparacaoCnesDto> compararComCnes() {
        return ResponseEntity.ok(
                comparacaoCnesService.comparar()
        );
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{idServico}/coordenadas")
    public ResponseEntity<Void> aprovarCoordenadas(
            @PathVariable Long idServico,
            @Valid @RequestBody AprovacaoCoordenadasRequest request
    ) {
        aprovacaoService.aprovar(idServico, request);
        return ResponseEntity.noContent().build();
    }
}
