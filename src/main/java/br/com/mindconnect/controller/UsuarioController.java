package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.FavoritoEntradaDto;
import br.com.mindconnect.dto.FavoritoSaidaDto;
import br.com.mindconnect.dto.RegistroBemEstarAninhadoDto;
import br.com.mindconnect.dto.RegistroBemEstarSaidaDto;
import br.com.mindconnect.dto.UsuarioAtualizarDto;
import br.com.mindconnect.dto.UsuarioEntradaDto;
import br.com.mindconnect.dto.UsuarioSaidaDto;
import br.com.mindconnect.model.Favorito;
import br.com.mindconnect.model.RegistroBemEstar;
import br.com.mindconnect.model.Usuario;
import br.com.mindconnect.service.FavoritoService;
import br.com.mindconnect.service.RegistroBemEstarService;
import br.com.mindconnect.service.UsuarioService;
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

// Rotas de usuários e as rotas aninhadas (registros de bem-estar do 1:N e conteúdos favoritos)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RegistroBemEstarService registroService;
    private final FavoritoService favoritoService;
    private final ModelMapper mapper;

    public UsuarioController(UsuarioService usuarioService, RegistroBemEstarService registroService,
                             FavoritoService favoritoService, ModelMapper mapper) {
        this.usuarioService = usuarioService;
        this.registroService = registroService;
        this.favoritoService = favoritoService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.PUBLICO)
    @PostMapping
    public ResponseEntity<UsuarioSaidaDto> cadastrar(@Valid @RequestBody UsuarioEntradaDto dto) {
        Usuario criado = usuarioService.cadastrar(mapper.map(dto, Usuario.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/{id}").buildAndExpand(criado.getIdUsuario()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, UsuarioSaidaDto.class));
    }

    @Acesso(Nivel.ADMINISTRADOR)
    @GetMapping
    public ResponseEntity<List<UsuarioSaidaDto>> listar() {
        List<UsuarioSaidaDto> lista = usuarioService.listar().stream()
                .map(u -> mapper.map(u, UsuarioSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.PROPRIETARIO_OU_ADMINISTRADOR)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mapper.map(usuarioService.buscarPorId(id), UsuarioSaidaDto.class));
    }

    @Acesso(Nivel.PROPRIETARIO)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> atualizar(@PathVariable("id") Long id,
                                                     @Valid @RequestBody UsuarioAtualizarDto dto) {
        Usuario atualizado = usuarioService.atualizar(id, mapper.map(dto, Usuario.class));
        return ResponseEntity.ok(mapper.map(atualizado, UsuarioSaidaDto.class));
    }

    @Acesso(Nivel.PROPRIETARIO_OU_ADMINISTRADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---- rotas aninhadas: registros de bem-estar do usuário ----

    @Acesso(Nivel.PROPRIETARIO)
    @GetMapping("/{id}/bem-estar")
    public ResponseEntity<List<RegistroBemEstarSaidaDto>> listarRegistros(@PathVariable("id") Long id) {
        List<RegistroBemEstarSaidaDto> lista = registroService.listarPorUsuario(id).stream()
                .map(r -> mapper.map(r, RegistroBemEstarSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.PROPRIETARIO)
    @PostMapping("/{id}/bem-estar")
    public ResponseEntity<RegistroBemEstarSaidaDto> cadastrarRegistro(
            @PathVariable("id") Long id, @Valid @RequestBody RegistroBemEstarAninhadoDto dto) {
        RegistroBemEstar criado = registroService.cadastrarParaUsuario(id, mapper.map(dto, RegistroBemEstar.class));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/bem-estar/{id}").buildAndExpand(criado.getIdRegistro()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, RegistroBemEstarSaidaDto.class));
    }

    // ---- rotas aninhadas: conteúdos favoritos do usuário ----

    @Acesso(Nivel.PROPRIETARIO)
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<FavoritoSaidaDto>> listarFavoritos(@PathVariable("id") Long id) {
        List<FavoritoSaidaDto> lista = favoritoService.listarPorUsuario(id).stream()
                .map(f -> mapper.map(f, FavoritoSaidaDto.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @Acesso(Nivel.PROPRIETARIO)
    @PostMapping("/{id}/favoritos")
    public ResponseEntity<FavoritoSaidaDto> favoritar(
            @PathVariable("id") Long id, @Valid @RequestBody FavoritoEntradaDto dto) {
        Favorito criado = favoritoService.adicionar(id, dto.getIdConteudo());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/{id}/favoritos/{idConteudo}")
                .buildAndExpand(id, criado.getIdConteudo()).toUri();
        return ResponseEntity.created(location).body(mapper.map(criado, FavoritoSaidaDto.class));
    }

    @Acesso(Nivel.PROPRIETARIO)
    @GetMapping("/{id}/favoritos/{idConteudo}")
    public ResponseEntity<FavoritoSaidaDto> buscarFavorito(
            @PathVariable("id") Long id, @PathVariable("idConteudo") Long idConteudo) {
        return ResponseEntity.ok(mapper.map(favoritoService.buscarPorId(id, idConteudo), FavoritoSaidaDto.class));
    }

    @Acesso(Nivel.PROPRIETARIO)
    @DeleteMapping("/{id}/favoritos/{idConteudo}")
    public ResponseEntity<Void> desfavoritar(
            @PathVariable("id") Long id, @PathVariable("idConteudo") Long idConteudo) {
        favoritoService.remover(id, idConteudo);
        return ResponseEntity.noContent().build();
    }
}
