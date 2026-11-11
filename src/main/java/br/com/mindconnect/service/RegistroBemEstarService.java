package br.com.mindconnect.service;

import br.com.mindconnect.dao.RegistroBemEstarDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.exception.AcessoNegadoException;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegistroBemEstarDuplicadoException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.model.RegistroBemEstar;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistroBemEstarService {

    private final RegistroBemEstarDAO registroDAO;
    private final UsuarioDAO usuarioDAO;

    public RegistroBemEstarService(RegistroBemEstarDAO registroDAO, UsuarioDAO usuarioDAO) {
        this.registroDAO = registroDAO;
        this.usuarioDAO = usuarioDAO;
    }

    // POST /bem-estar: o usuário vem no corpo; se não existir, é dado inválido (400)
    public RegistroBemEstar cadastrar(RegistroBemEstar registro) {
        if (usuarioDAO.buscarPorId(registro.getIdUsuario()) == null) {
            throw new RequisicaoInvalidaException("O usuário " + registro.getIdUsuario() + " não existe.");
        }
        return salvarNovo(registro);
    }

    // POST /usuarios/{id}/bem-estar: o usuário vem da URL; se não existir, 404
    public RegistroBemEstar cadastrarParaUsuario(Long idUsuario, RegistroBemEstar registro) {
        verificarUsuario(idUsuario);
        registro.setIdUsuario(idUsuario);
        return salvarNovo(registro);
    }

    // idUsuario null = todos os registros; com valor = histórico do usuário
    public List<RegistroBemEstar> listar(Long idUsuario) {
        if (idUsuario != null) {
            verificarUsuario(idUsuario);
        }
        return registroDAO.listar(idUsuario);
    }

    public List<RegistroBemEstar> listarPorUsuario(Long idUsuario) {
        verificarUsuario(idUsuario);
        return registroDAO.listar(idUsuario);
    }

    public RegistroBemEstar buscarPorId(Long id) {
        RegistroBemEstar registro = registroDAO.buscarPorId(id);
        if (registro == null) {
            throw new RecursoNaoEncontradoException("Registro de bem-estar não encontrado com o id " + id);
        }
        return registro;
    }

    // Só o dono do registro pode consultar, alterar ou excluir (senão, 403)
    public RegistroBemEstar buscarDoUsuario(Long id, Long idLogado) {
        RegistroBemEstar registro = buscarPorId(id);
        if (!registro.getIdUsuario().equals(idLogado)) {
            throw new AcessoNegadoException("Você só pode acessar os seus próprios registros de bem-estar.");
        }
        return registro;
    }

    public RegistroBemEstar atualizar(Long id, RegistroBemEstar dados, Long idLogado) {
        RegistroBemEstar existente = buscarDoUsuario(id, idLogado);

        if (dados.getDataRegistro() == null) {
            dados.setDataRegistro(existente.getDataRegistro());
        }
        validarDiaLivre(existente.getIdUsuario(), dados.getDataRegistro(), id);

        dados.setIdRegistro(id);
        dados.setIdUsuario(existente.getIdUsuario());
        registroDAO.atualizar(dados);
        return buscarPorId(id);
    }

    public void excluir(Long id, Long idLogado) {
        buscarDoUsuario(id, idLogado);
        registroDAO.excluir(id);
    }

    private RegistroBemEstar salvarNovo(RegistroBemEstar registro) {
        if (registro.getDataRegistro() == null) {
            registro.setDataRegistro(LocalDate.now());
        }
        validarDiaLivre(registro.getIdUsuario(), registro.getDataRegistro(), 0L);

        Long id = registroDAO.inserir(registro);
        return buscarPorId(id);
    }

    // Regra de negócio: apenas um registro de bem-estar por usuário por dia
    private void validarDiaLivre(Long idUsuario, LocalDate data, Long idIgnorar) {
        if (registroDAO.existeNoDia(idUsuario, data, idIgnorar)) {
            throw new RegistroBemEstarDuplicadoException(data);
        }
    }

    private void verificarUsuario(Long idUsuario) {
        if (usuarioDAO.buscarPorId(idUsuario) == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o id " + idUsuario);
        }
    }
}
