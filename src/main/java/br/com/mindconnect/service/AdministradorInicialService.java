package br.com.mindconnect.service;

import br.com.mindconnect.dao.AdministradorDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.model.Administrador;
import br.com.mindconnect.model.Usuario;
import org.springframework.stereotype.Service;

// Garante que exista pelo menos um administrador capaz de fazer login.
// Os administradores da carga inicial do SQL têm senha de demonstração e não conseguem logar,
// então o primeiro acesso é definido por variáveis de ambiente (nada de senha no código).
@Service
public class AdministradorInicialService {

    private final UsuarioDAO usuarioDAO;
    private final AdministradorDAO administradorDAO;
    private final SenhaService senhaService;

    public AdministradorInicialService(UsuarioDAO usuarioDAO, AdministradorDAO administradorDAO,
                                       SenhaService senhaService) {
        this.usuarioDAO = usuarioDAO;
        this.administradorDAO = administradorDAO;
        this.senhaService = senhaService;
    }

    // - e-mail novo: cria o usuário e o torna administrador;
    // - e-mail existente: define a senha informada e garante o acesso de administrador.
    public void garantir(String email, String senha) {
        String emailNormalizado = UsuarioService.normalizarEmail(email);
        Usuario usuario = usuarioDAO.buscarPorEmail(emailNormalizado);
        Long idUsuario;

        if (usuario == null) {
            Usuario novo = new Usuario();
            novo.setNome("Administrador");
            novo.setEmail(emailNormalizado);
            novo.setSenha(senhaService.gerarHash(senha));
            idUsuario = usuarioDAO.inserir(novo);
        } else {
            idUsuario = usuario.getIdUsuario();
            if (!senhaService.conferir(senha, usuario.getSenha())) {
                usuario.setSenha(senhaService.gerarHash(senha));
                usuarioDAO.atualizar(usuario);
            }
        }

        if (administradorDAO.buscarPorUsuario(idUsuario) == null) {
            Administrador admin = new Administrador();
            admin.setIdUsuario(idUsuario);
            admin.setNivelAcesso("ADMIN");
            administradorDAO.inserir(admin);
        }
    }
}
