package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.LoginEntradaDto;
import br.com.mindconnect.dto.LoginSaidaDto;
import br.com.mindconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Acesso(Nivel.PUBLICO)
    @PostMapping("/login")
    public ResponseEntity<LoginSaidaDto> login(@Valid @RequestBody LoginEntradaDto dto) {
        return ResponseEntity.ok(authService.login(dto.getEmail(), dto.getSenha()));
    }
}
