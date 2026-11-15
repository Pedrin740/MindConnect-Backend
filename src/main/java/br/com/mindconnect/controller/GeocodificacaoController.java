package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.geografia.GeocodificacaoRequest;
import br.com.mindconnect.geografia.GeocodificacaoResponse;
import br.com.mindconnect.service.GeocodificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocodificacao")
public class GeocodificacaoController {

    private final GeocodificacaoService geocodificacaoService;

    public GeocodificacaoController(GeocodificacaoService geocodificacaoService) {
        this.geocodificacaoService = geocodificacaoService;
    }

    @Acesso(Nivel.PUBLICO)
    @PostMapping
    public ResponseEntity<GeocodificacaoResponse> geocodificar(
            @RequestBody GeocodificacaoRequest request
    ) {
        GeocodificacaoResponse resultado =
                geocodificacaoService.geocodificar(request.endereco());

        if (resultado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado);
    }
}