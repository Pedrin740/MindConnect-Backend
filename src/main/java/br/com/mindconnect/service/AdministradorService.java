package br.com.mindconnect.service;

import br.com.mindconnect.dao.AdministradorDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.model.Administrador;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministradorService {

    private final AdministradorDAO administradorDAO;
    private final UsuarioDAO usuarioDAO;

    public AdministradorService(AdministradorDAO administradorDAO, UsuarioDAO usuarioDAO) {
        this.administradorDAO = administradorDAO;
        this.usuarioDAO = usuarioDAO;
    }

    // Regra de negócio: o usuário precisa existir e só pode ser administrador uma vez
    public Administrador cadastrar(Administrador administrador) {
        if (usuarioDAO.buscarPorId(administrador.getIdUsuario()) == null) {
            throw new RequisicaoInvalidaException("O usuário " + administrador.getIdUsuario() + " não existe.");
        }
        if (administradorDAO.buscarPorUsuario(administrador.getIdUsuario()) != null) {
            throw new RegraNegocioException("Este usuário já é administrador.");
        }
        if (administrador.getNivelAcesso() == null || administrador.getNivelAcesso().isBlank()) {
            administrador.setNivelAcesso("ADMIN");
        }

        Long id = administradorDAO.inserir(administrador);
        return buscarPorId(id);
    }

    public List<Administrador> listar() {
        return administradorDAO.listar();
    }

    public Administrador buscarPorId(Long id) {
        Administrador administrador = administradorDAO.buscarPorId(id);
        if (administrador == null) {
            throw new RecursoNaoEncontradoException("Administrador não encontrado com o id " + id);
        }
        return administrador;
    }

    public Administrador atualizar(Long id, String nivelAcesso) {
        Administrador existente = buscarPorId(id);
        existente.setNivelAcesso(nivelAcesso);

        administradorDAO.atualizar(existente);
        return buscarPorId(id);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        administradorDAO.excluir(id);
    }
}
