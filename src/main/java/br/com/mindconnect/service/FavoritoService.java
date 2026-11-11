package br.com.mindconnect.service;

import br.com.mindconnect.dao.ConteudoDAO;
import br.com.mindconnect.dao.FavoritoDAO;
import br.com.mindconnect.dao.UsuarioDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.model.Favorito;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoDAO favoritoDAO;
    private final UsuarioDAO usuarioDAO;
    private final ConteudoDAO conteudoDAO;

    public FavoritoService(FavoritoDAO favoritoDAO, UsuarioDAO usuarioDAO, ConteudoDAO conteudoDAO) {
        this.favoritoDAO = favoritoDAO;
        this.usuarioDAO = usuarioDAO;
        this.conteudoDAO = conteudoDAO;
    }

    // POST /usuarios/{id}/favoritos: o usuário vem da URL (404 se não existir);
    // o conteúdo vem no corpo (400 se não existir)
    // Regra de negócio: o mesmo conteúdo não pode ser favoritado duas vezes pelo mesmo usuário
    public Favorito adicionar(Long idUsuario, Long idConteudo) {
        verificarUsuario(idUsuario);

        if (conteudoDAO.buscarPorId(idConteudo) == null) {
            throw new RequisicaoInvalidaException("O conteúdo " + idConteudo + " não existe.");
        }
        if (favoritoDAO.buscar(idUsuario, idConteudo) != null) {
            throw new RegraNegocioException("Este conteúdo já está nos favoritos do usuário.");
        }

        Favorito favorito = new Favorito();
        favorito.setIdUsuario(idUsuario);
        favorito.setIdConteudo(idConteudo);
        favorito.setDataFavorito(LocalDate.now());

        favoritoDAO.inserir(favorito);
        return buscarPorId(idUsuario, idConteudo);
    }

    public List<Favorito> listarPorUsuario(Long idUsuario) {
        verificarUsuario(idUsuario);
        return favoritoDAO.listarPorUsuario(idUsuario);
    }

    public Favorito buscarPorId(Long idUsuario, Long idConteudo) {
        Favorito favorito = favoritoDAO.buscar(idUsuario, idConteudo);
        if (favorito == null) {
            throw new RecursoNaoEncontradoException(
                    "O conteúdo " + idConteudo + " não está nos favoritos do usuário " + idUsuario);
        }
        return favorito;
    }

    public void remover(Long idUsuario, Long idConteudo) {
        buscarPorId(idUsuario, idConteudo);
        favoritoDAO.excluir(idUsuario, idConteudo);
    }

    private void verificarUsuario(Long idUsuario) {
        if (usuarioDAO.buscarPorId(idUsuario) == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o id " + idUsuario);
        }
    }
}
