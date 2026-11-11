package br.com.mindconnect.service;

import br.com.mindconnect.dao.AdministradorDAO;
import br.com.mindconnect.dao.FavoritoDAO;
import br.com.mindconnect.dao.RegistroBemEstarDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.exception.EmailJaCadastradoException;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RemocaoNaoPermitidaException;
import br.com.mindconnect.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final RegistroBemEstarDAO registroDAO;
    private final AdministradorDAO administradorDAO;
    private final FavoritoDAO favoritoDAO;
    private final SenhaService senhaService;

    public UsuarioService(UsuarioDAO usuarioDAO, RegistroBemEstarDAO registroDAO,
                          AdministradorDAO administradorDAO, FavoritoDAO favoritoDAO,
                          SenhaService senhaService) {
        this.usuarioDAO = usuarioDAO;
        this.registroDAO = registroDAO;
        this.administradorDAO = administradorDAO;
        this.favoritoDAO = favoritoDAO;
        this.senhaService = senhaService;
    }

    // Regra de negócio: o e-mail do usuário é único
    public Usuario cadastrar(Usuario usuario) {
        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(normalizarEmail(usuario.getEmail()));

        if (usuarioDAO.existeEmail(usuario.getEmail(), 0L)) {
            throw new EmailJaCadastradoException(usuario.getEmail());
        }

        usuario.setSenha(senhaService.gerarHash(usuario.getSenha()));
        Long id = usuarioDAO.inserir(usuario);
        return buscarPorId(id);
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    public Usuario buscarPorId(Long id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o id " + id);
        }
        return usuario;
    }

    public Usuario atualizar(Long id, Usuario dados) {
        Usuario existente = buscarPorId(id);
        String email = normalizarEmail(dados.getEmail());

        if (usuarioDAO.existeEmail(email, id)) {
            throw new EmailJaCadastradoException(email);
        }

        existente.setNome(dados.getNome().trim());
        existente.setEmail(email);

        // a senha só muda se uma nova for enviada
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            existente.setSenha(senhaService.gerarHash(dados.getSenha()));
        }

        usuarioDAO.atualizar(existente);
        return buscarPorId(id);
    }

    // Não remove usuário que ainda tem registros de bem-estar, favoritos ou acesso de administrador
    public void excluir(Long id) {
        buscarPorId(id);

        if (registroDAO.contarPorUsuario(id) > 0) {
            throw new RemocaoNaoPermitidaException(
                    "O usuário possui registros de bem-estar. Remova os registros antes de excluir o usuário.");
        }
        if (favoritoDAO.contarPorUsuario(id) > 0) {
            throw new RemocaoNaoPermitidaException(
                    "O usuário possui conteúdos favoritados. Remova os favoritos antes de excluir o usuário.");
        }
        if (administradorDAO.buscarPorUsuario(id) != null) {
            throw new RemocaoNaoPermitidaException(
                    "O usuário é administrador. Remova o acesso administrativo antes de excluir o usuário.");
        }

        usuarioDAO.excluir(id);
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
