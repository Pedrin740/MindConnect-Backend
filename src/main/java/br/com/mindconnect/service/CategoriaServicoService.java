package br.com.mindconnect.service;

import br.com.mindconnect.dao.CategoriaServicoDAO;
import br.com.mindconnect.dao.ServicoAcolhimentoDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RemocaoNaoPermitidaException;
import br.com.mindconnect.model.CategoriaServico;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServicoService {

    private final CategoriaServicoDAO categoriaDAO;
    private final ServicoAcolhimentoDAO servicoDAO;

    public CategoriaServicoService(CategoriaServicoDAO categoriaDAO, ServicoAcolhimentoDAO servicoDAO) {
        this.categoriaDAO = categoriaDAO;
        this.servicoDAO = servicoDAO;
    }

    public CategoriaServico cadastrar(CategoriaServico categoria) {
        categoria.setNome(categoria.getNome().trim());
        validarNomeUnico(categoria.getNome(), 0L);

        Long id = categoriaDAO.inserir(categoria);
        return buscarPorId(id);
    }

    public List<CategoriaServico> listar() {
        return categoriaDAO.listar();
    }

    public CategoriaServico buscarPorId(Long id) {
        CategoriaServico categoria = categoriaDAO.buscarPorId(id);
        if (categoria == null) {
            throw new RecursoNaoEncontradoException("Categoria de serviço não encontrada com o id " + id);
        }
        return categoria;
    }

    public CategoriaServico atualizar(Long id, CategoriaServico dados) {
        buscarPorId(id);
        dados.setNome(dados.getNome().trim());
        validarNomeUnico(dados.getNome(), id);

        dados.setIdCategoriaServico(id);
        categoriaDAO.atualizar(dados);
        return buscarPorId(id);
    }

    // Não remove categoria que ainda tem serviços (lado "1" do 1:N)
    public void excluir(Long id) {
        buscarPorId(id);

        if (servicoDAO.contarPorCategoria(id) > 0) {
            throw new RemocaoNaoPermitidaException(
                    "A categoria possui serviços cadastrados. Remova ou mude os serviços antes de excluí-la.");
        }

        categoriaDAO.excluir(id);
    }

    private void validarNomeUnico(String nome, Long idIgnorar) {
        if (categoriaDAO.existeNome(nome, idIgnorar)) {
            throw new RegraNegocioException("Já existe uma categoria de serviço com o nome " + nome);
        }
    }
}
