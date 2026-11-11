package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.AutorizacaoInterceptor;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.RegistroBemEstarAninhadoDto;
import br.com.mindconnect.dto.RegistroBemEstarEntradaDto;
import br.com.mindconnect.dto.RegistroBemEstarSaidaDto;
import br.com.mindconnect.exception.AcessoNegadoException;
import br.com.mindconnect.model.RegistroBemEstar;
import br.com.mindconnect.service.RegistroBemEstarService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Diário de bem-estar: dado pessoal. Todas as rotas exigem login e cada usuário só enxerga
// e altera os PRÓPRIOS registros (o id do dono vem do token, nunca do que o cliente informa).
@RestController
@RequestMapping("/api/bem-estar")
public class RegistroBemEstarController {

    private final RegistroBemEstarService registroService;
    private final ModelMapper mapper;

    public RegistroBemEstarController(RegistroBemEstarService registroService, ModelMapper mapper) {
        this.registroService = registroService;
        this.mapper = mapper;
    }

    // O idUsuario do corpo precisa ser o do usuário logado
    @Acesso(Nivel.LOGADO)
    @PostMapping
    public ResponseEntity<RegistroBemEstarSaidaDto> cadastrar(
            @Valid @RequestBody RegistroBemEstarEntradaDto dto,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        exigirMesmoUsuario(dto.getIdUsuario(), idLogado);

        RegistroBemEstar criado = registroService.cadastrar(mapper.map(dto, RegistroBemEstar.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/bem-estar/{id}").buildAndExpand(criado.getIdRegistro()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, RegistroBemEstarSaidaDto.class));
    }

    // Lista SEMPRE os registros do usuário logado. O filtro ?idUsuario= é opcional,
    // mas se for informado precisa ser o do próprio usuário (senão, 403).
    @Acesso(Nivel.LOGADO)
    @GetMapping
    public ResponseEntity<List<RegistroBemEstarSaidaDto>> listar(
            @RequestParam(name = "idUsuario", required = false) Long idUsuario,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        exigirMesmoUsuario(idUsuario, idLogado);
        return ResponseEntity.ok(converter(registroService.listarPorUsuario(idLogado)));
    }

    // Histórico do usuário logado (mais recente primeiro): /api/bem-estar/historico?idUsuario=1
    @Acesso(Nivel.LOGADO)
    @GetMapping("/historico")
    public ResponseEntity<List<RegistroBemEstarSaidaDto>> historico(
            @RequestParam(name = "idUsuario") Long idUsuario,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        exigirMesmoUsuario(idUsuario, idLogado);
        return ResponseEntity.ok(converter(registroService.listarPorUsuario(idLogado)));
    }

    @Acesso(Nivel.LOGADO)
    @GetMapping("/{id}")
    public ResponseEntity<RegistroBemEstarSaidaDto> buscarPorId(
            @PathVariable("id") Long id,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        RegistroBemEstar registro = registroService.buscarDoUsuario(id, idLogado);
        return ResponseEntity.ok(mapper.map(registro, RegistroBemEstarSaidaDto.class));
    }

    // O registro não troca de dono: o PUT não recebe idUsuario
    @Acesso(Nivel.LOGADO)
    @PutMapping("/{id}")
    public ResponseEntity<RegistroBemEstarSaidaDto> atualizar(
            @PathVariable("id") Long id, @Valid @RequestBody RegistroBemEstarAninhadoDto dto,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        RegistroBemEstar atualizado = registroService.atualizar(id, mapper.map(dto, RegistroBemEstar.class), idLogado);
        return ResponseEntity.ok(mapper.map(atualizado, RegistroBemEstarSaidaDto.class));
    }

    @Acesso(Nivel.LOGADO)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable("id") Long id,
            @RequestAttribute(AutorizacaoInterceptor.ATRIBUTO_USUARIO) Long idLogado) {
        registroService.excluir(id, idLogado);
        return ResponseEntity.noContent().build();
    }

    // Se um id de usuário foi informado, ele precisa ser o do usuário logado
    private void exigirMesmoUsuario(Long idInformado, Long idLogado) {
        if (idInformado != null && !idInformado.equals(idLogado)) {
            throw new AcessoNegadoException("Você só pode acessar os seus próprios registros de bem-estar.");
        }
    }

    private List<RegistroBemEstarSaidaDto> converter(List<RegistroBemEstar> registros) {
        return registros.stream()
                .map(r -> mapper.map(r, RegistroBemEstarSaidaDto.class))
                .toList();
    }
}
