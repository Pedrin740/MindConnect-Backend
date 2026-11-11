package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.ServicoAcolhimentoEntradaDto;
import br.com.mindconnect.dto.ServicoAcolhimentoSaidaDto;
import br.com.mindconnect.model.ServicoAcolhimento;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoAcolhimentoController {

    private final ServicoAcolhimentoService servicoService;
    private final ModelMapper mapper;

    public ServicoAcolhimentoController(ServicoAcolhimentoService servicoService, ModelMapper mapper) {
        this.servicoService = servicoService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PostMapping
    public ResponseEntity<ServicoAcolhimentoSaidaDto> cadastrar(@Valid @RequestBody ServicoAcolhimentoEntradaDto dto) {
        ServicoAcolhimento criado = servicoService.cadastrar(mapper.map(dto, ServicoAcolhimento.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/servicos/{id}").buildAndExpand(criado.getIdServico()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, ServicoAcolhimentoSaidaDto.class));
    }

    // Filtros opcionais: /api/servicos?nome=caps&regiao=ZONA SUL&categoria=CAPS (nome ou id da categoria)
    @Acesso(Nivel.PUBLICO)
    @GetMapping
    public ResponseEntity<List<ServicoAcolhimentoSaidaDto>> listar(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "regiao", required = false) String regiao,
            @RequestParam(name = "categoria", required = false) String categoria) {
        return ResponseEntity.ok(converter(servicoService.listar(nome, regiao, categoria)));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/regiao/{regiao}")
    public ResponseEntity<List<ServicoAcolhimentoSaidaDto>> listarPorRegiao(@PathVariable("regiao") String regiao) {
        return ResponseEntity.ok(converter(servicoService.listar(null, regiao, null)));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ServicoAcolhimentoSaidaDto>> listarPorCategoria(
            @PathVariable("categoria") String categoria) {
        return ResponseEntity.ok(converter(servicoService.listar(null, null, categoria)));
    }

    @Acesso(Nivel.PUBLICO)
    @GetMapping("/{id}")
    public ResponseEntity<ServicoAcolhimentoSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(servicoService.buscarPorId(id), ServicoAcolhimentoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @PutMapping("/{id}")
    public ResponseEntity<ServicoAcolhimentoSaidaDto> atualizar(
            @PathVariable("id") Long id, @Valid @RequestBody ServicoAcolhimentoEntradaDto dto) {
        ServicoAcolhimento atualizado = servicoService.atualizar(id, mapper.map(dto, ServicoAcolhimento.class));
        return ResponseEntity.ok(mapper.map(atualizado, ServicoAcolhimentoSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        servicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private List<ServicoAcolhimentoSaidaDto> converter(List<ServicoAcolhimento> servicos) {
        return servicos.stream()
                .map(s -> mapper.map(s, ServicoAcolhimentoSaidaDto.class))
                .toList();
    }
}
