package br.com.mindconnect.service;

import br.com.mindconnect.dao.AdministradorDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.dto.LoginSaidaDto;
import br.com.mindconnect.exception.CredenciaisInvalidasException;
import br.com.mindconnect.model.Administrador;
import br.com.mindconnect.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final AdministradorDAO administradorDAO;
    private final SenhaService senhaService;
    private final TokenService tokenService;

    public AuthService(UsuarioDAO usuarioDAO, AdministradorDAO administradorDAO, SenhaService senhaService,
                       TokenService tokenService) {
        this.usuarioDAO = usuarioDAO;
        this.administradorDAO = administradorDAO;
        this.senhaService = senhaService;
        this.tokenService = tokenService;
    }

    // Confere e-mail e senha. A mensagem de erro é a mesma nos dois casos
    // para não revelar se o e-mail existe.
    public LoginSaidaDto login(String email, String senha) {
        Usuario usuario = usuarioDAO.buscarPorEmail(UsuarioService.normalizarEmail(email));

        if (usuario == null || !senhaService.conferir(senha, usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        Administrador admin = administradorDAO.buscarPorUsuario(usuario.getIdUsuario());

        LoginSaidaDto saida = new LoginSaidaDto();
        saida.setIdUsuario(usuario.getIdUsuario());
        saida.setNome(usuario.getNome());
        saida.setEmail(usuario.getEmail());
        saida.setAdministrador(admin != null);
        saida.setNivelAcesso(admin != null ? admin.getNivelAcesso() : null);
        saida.setToken(tokenService.gerar(usuario.getIdUsuario()));
        return saida;
    }
}
