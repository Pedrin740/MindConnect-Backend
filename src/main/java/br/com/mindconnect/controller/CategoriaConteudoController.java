package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.CategoriaConteudoEntradaDto;
import br.com.mindconnect.dto.CategoriaConteudoSaidaDto;
import br.com.mindconnect.model.CategoriaConteudo;
import br.com.mindconnect.service.CategoriaConteudoService;
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
@RequestMapping("/api/categorias-conteudo")
public class CategoriaConteudoController {

    private final CategoriaConteudoService categoriaService;
    private final ModelMapper mapper;

    public CategoriaConteudoController(CategoriaConteudoService categoriaService, ModelMapper mapper) {
        this.categoriaService = categoriaService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<CategoriaConteudoSaidaDto> cadastrar(@Valid @RequestBody CategoriaConteudoEntradaDto dto) {
        CategoriaConteudo criada = categoriaService.cadastrar(mapper.map(dto, CategoriaConteudo.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/categorias-conteudo/{id}").buildAndExpand(criada.getIdCategoriaConteudo()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criada, CategoriaConteudoSaidaDto.class));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping
    public ResponseEntity<List<CategoriaConteudoSaidaDto>> listar() {
        List<CategoriaConteudoSaidaDto> lista = categoriaService.listar().stream()
                .map(c -> mapper.map(c, CategoriaConteudoSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaConteudoSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(categoriaService.buscarPorId(id), CategoriaConteudoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaConteudoSaidaDto> atualizar(@PathVariable("id") Long id,
                                                               @Valid @RequestBody CategoriaConteudoEntradaDto dto) {
        CategoriaConteudo atualizada = categoriaService.atualizar(id, mapper.map(dto, CategoriaConteudo.class));
        return ResponseEntity.ok(mapper.map(atualizada, CategoriaConteudoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
