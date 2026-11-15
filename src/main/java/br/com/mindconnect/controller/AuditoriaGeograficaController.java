package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.geografia.RelatorioAuditoriaGeograficaDto;
import br.com.mindconnect.service.AuditoriaGeograficaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicos/auditoria-geografica")
public class AuditoriaGeograficaController {

    private final AuditoriaGeograficaService auditoriaService;

    public AuditoriaGeograficaController(AuditoriaGeograficaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<RelatorioAuditoriaGeograficaDto> auditar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int limite
    ) {
        return ResponseEntity.ok(auditoriaService.auditar(pagina, limite));
    }
}