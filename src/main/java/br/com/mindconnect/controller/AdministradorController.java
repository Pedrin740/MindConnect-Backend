package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.AdministradorAtualizarDto;
import br.com.mindconnect.dto.AdministradorEntradaDto;
import br.com.mindconnect.dto.AdministradorSaidaDto;
import br.com.mindconnect.model.Administrador;
import br.com.mindconnect.service.AdministradorService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final ModelMapper mapper;

    public AdministradorController(AdministradorService administradorService, ModelMapper mapper) {
        this.administradorService = administradorService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<AdministradorSaidaDto> cadastrar(@Valid @RequestBody AdministradorEntradaDto dto) {
        Administrador criado = administradorService.cadastrar(mapper.map(dto, Administrador.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/administradores/{id}").buildAndExpand(criado.getIdAdministrador()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, AdministradorSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @GetMapping
    public ResponseEntity<List<AdministradorSaidaDto>> listar() {
        List<AdministradorSaidaDto> lista = administradorService.listar().stream()
                .map(a -> mapper.map(a, AdministradorSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @GetMapping("/{id}")
    public ResponseEntity<AdministradorSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(administradorService.buscarPorId(id), AdministradorSaidaDto.class));
    }

    // Só o nível de acesso pode mudar; o usuário vinculado continua o mesmo
    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<AdministradorSaidaDto> atualizar(@PathVariable("id") Long id,
                                                           @Valid @RequestBody AdministradorAtualizarDto dto) {
        Administrador atualizado = administradorService.atualizar(id, dto.getNivelAcesso());
        return ResponseEntity.ok(mapper.map(atualizado, AdministradorSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        administradorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
