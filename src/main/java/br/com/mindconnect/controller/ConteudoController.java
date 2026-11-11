package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.ConteudoEntradaDto;
import br.com.mindconnect.dto.ConteudoSaidaDto;
import br.com.mindconnect.model.Conteudo;
import br.com.mindconnect.service.ConteudoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/conteudos")
public class ConteudoController {

    private final ConteudoService conteudoService;
    private final ModelMapper mapper;

    public ConteudoController(ConteudoService conteudoService, ModelMapper mapper) {
        this.conteudoService = conteudoService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<ConteudoSaidaDto> cadastrar(@Valid @RequestBody ConteudoEntradaDto dto) {
        Conteudo criado = conteudoService.cadastrar(mapper.map(dto, Conteudo.class), dto.getIdsCategorias());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/conteudos/{id}").buildAndExpand(criado.getIdConteudo()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, ConteudoSaidaDto.class));
    }

    // Filtros opcionais: /api/conteudos?busca=ansiedade&status=ATIVO&categoria=Ansiedade (nome ou id)
    @Acesso(Nivel.PUBLICO)
    @GetMapping
    public ResponseEntity<List<ConteudoSaidaDto>> listar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "categoria", required = false) String categoria) {
        return ResponseEntity.ok(converter(conteudoService.listar(busca, status, categoria)));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ConteudoSaidaDto>> listarPorCategoria(@PathVariable("categoria") String categoria) {
        return ResponseEntity.ok(converter(conteudoService.listar(null, null, categoria)));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/{id}")
    public ResponseEntity<ConteudoSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(conteudoService.buscarPorId(id), ConteudoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<ConteudoSaidaDto> atualizar(@PathVariable("id") Long id,
                                                      @Valid @RequestBody ConteudoEntradaDto dto) {
        Conteudo atualizado = conteudoService.atualizar(id, mapper.map(dto, Conteudo.class), dto.getIdsCategorias());
        return ResponseEntity.ok(mapper.map(atualizado, ConteudoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        conteudoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private List<ConteudoSaidaDto> converter(List<Conteudo> conteudos) {
        return conteudos.stream()
                .map(c -> mapper.map(c, ConteudoSaidaDto.class))
                .toList();
    }
}
