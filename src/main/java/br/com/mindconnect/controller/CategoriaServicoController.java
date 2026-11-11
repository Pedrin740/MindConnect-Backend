package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.CategoriaServicoEntradaDto;
import br.com.mindconnect.dto.CategoriaServicoSaidaDto;
import br.com.mindconnect.dto.ServicoAcolhimentoAninhadoDto;
import br.com.mindconnect.dto.ServicoAcolhimentoSaidaDto;
import br.com.mindconnect.model.CategoriaServico;
import br.com.mindconnect.model.ServicoAcolhimento;
import br.com.mindconnect.service.CategoriaServicoService;
import br.com.mindconnect.service.ServicoAcolhimentoService;
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

// Rotas de categorias de serviço e a rota aninhada do relacionamento 1:N (categoria -> serviços)
@RestController
@RequestMapping("/api/categorias-servico")
public class CategoriaServicoController {

    private final CategoriaServicoService categoriaService;
    private final ServicoAcolhimentoService servicoService;
    private final ModelMapper mapper;

    public CategoriaServicoController(CategoriaServicoService categoriaService,
                                      ServicoAcolhimentoService servicoService, ModelMapper mapper) {
        this.categoriaService = categoriaService;
        this.servicoService = servicoService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<CategoriaServicoSaidaDto> cadastrar(@Valid @RequestBody CategoriaServicoEntradaDto dto) {
        CategoriaServico criada = categoriaService.cadastrar(mapper.map(dto, CategoriaServico.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/categorias-servico/{id}").buildAndExpand(criada.getIdCategoriaServico()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criada, CategoriaServicoSaidaDto.class));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping
    public ResponseEntity<List<CategoriaServicoSaidaDto>> listar() {
        List<CategoriaServicoSaidaDto> lista = categoriaService.listar().stream()
                .map(c -> mapper.map(c, CategoriaServicoSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaServicoSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(categoriaService.buscarPorId(id), CategoriaServicoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaServicoSaidaDto> atualizar(@PathVariable("id") Long id,
                                                              @Valid @RequestBody CategoriaServicoEntradaDto dto) {
        CategoriaServico atualizada = categoriaService.atualizar(id, mapper.map(dto, CategoriaServico.class));
        return ResponseEntity.ok(mapper.map(atualizada, CategoriaServicoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---- rotas aninhadas: serviços da categoria ----

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/{id}/servicos")
    public ResponseEntity<List<ServicoAcolhimentoSaidaDto>> listarServicos(@PathVariable("id") Long id) {
        List<ServicoAcolhimentoSaidaDto> lista = servicoService.listarPorCategoria(id).stream()
                .map(s -> mapper.map(s, ServicoAcolhimentoSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping("/{id}/servicos")
    public ResponseEntity<ServicoAcolhimentoSaidaDto> cadastrarServico(
            @PathVariable("id") Long id, @Valid @RequestBody ServicoAcolhimentoAninhadoDto dto) {
        ServicoAcolhimento criado = servicoService.cadastrarNaCategoria(id, mapper.map(dto, ServicoAcolhimento.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/servicos/{id}").buildAndExpand(criado.getIdServico()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, ServicoAcolhimentoSaidaDto.class));
    }
}
