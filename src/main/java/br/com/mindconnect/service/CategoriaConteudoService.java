package br.com.mindconnect.service;

import br.com.mindconnect.dao.CategoriaConteudoDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RemocaoNaoPermitidaException;
import br.com.mindconnect.model.CategoriaConteudo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaConteudoService {

    private final CategoriaConteudoDAO categoriaDAO;

    public CategoriaConteudoService(CategoriaConteudoDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public CategoriaConteudo cadastrar(CategoriaConteudo categoria) {
        categoria.setNome(categoria.getNome().trim());
        validarNomeUnico(categoria.getNome(), 0L);

        Long id = categoriaDAO.inserir(categoria);
        return buscarPorId(id);
    }

    public List<CategoriaConteudo> listar() {
        return categoriaDAO.listar();
    }

    public CategoriaConteudo buscarPorId(Long id) {
        CategoriaConteudo categoria = categoriaDAO.buscarPorId(id);
        if (categoria == null) {
            throw new RecursoNaoEncontradoException("Categoria de conteúdo não encontrada com o id " + id);
        }
        return categoria;
    }

    public CategoriaConteudo atualizar(Long id, CategoriaConteudo dados) {
        buscarPorId(id);
        dados.setNome(dados.getNome().trim());
        validarNomeUnico(dados.getNome(), id);

        dados.setIdCategoriaConteudo(id);
        categoriaDAO.atualizar(dados);
        return buscarPorId(id);
    }

    // Não remove categoria que ainda tem conteúdos associados
    public void excluir(Long id) {
        buscarPorId(id);

        if (categoriaDAO.contarConteudos(id) > 0) {
            throw new RemocaoNaoPermitidaException(
                    "A categoria possui conteúdos associados. Remova a associação antes de excluí-la.");
        }

        categoriaDAO.excluir(id);
    }

    private void validarNomeUnico(String nome, Long idIgnorar) {
        if (categoriaDAO.existeNome(nome, idIgnorar)) {
            throw new RegraNegocioException("Já existe uma categoria de conteúdo com o nome " + nome);
        }
    }
}
