package br.com.mindconnect.service;

import br.com.mindconnect.dao.CategoriaServicoDAO;
import br.com.mindconnect.dao.ServicoAcolhimentoDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.model.ServicoAcolhimento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoAcolhimentoService {

    private final ServicoAcolhimentoDAO servicoDAO;
    private final CategoriaServicoDAO categoriaDAO;

    public ServicoAcolhimentoService(ServicoAcolhimentoDAO servicoDAO, CategoriaServicoDAO categoriaDAO) {
        this.servicoDAO = servicoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    // POST /servicos: a categoria vem no corpo; se não existir, é dado inválido (400)
    public ServicoAcolhimento cadastrar(ServicoAcolhimento servico) {
        if (categoriaDAO.buscarPorId(servico.getIdCategoriaServico()) == null) {
            throw new RequisicaoInvalidaException(
                    "A categoria de serviço " + servico.getIdCategoriaServico() + " não existe.");
        }
        return salvarNovo(servico);
    }

    // POST /categorias-servico/{id}/servicos: a categoria vem da URL; se não existir, 404
    public ServicoAcolhimento cadastrarNaCategoria(Long idCategoria, ServicoAcolhimento servico) {
        if (categoriaDAO.buscarPorId(idCategoria) == null) {
            throw new RecursoNaoEncontradoException("Categoria de serviço não encontrada com o id " + idCategoria);
        }
        servico.setIdCategoriaServico(idCategoria);
        return salvarNovo(servico);
    }

    public List<ServicoAcolhimento> listar(String nome, String regiao, String categoria) {
        Long idCategoria = null;
        String nomeCategoria = null;

        // a categoria pode ser informada pelo id (numérico) ou pelo nome (ex.: CAPS)
        if (categoria != null && !categoria.isBlank()) {
            if (categoria.trim().matches("\\d{1,18}")) {
                idCategoria = Long.valueOf(categoria.trim());
            } else {
                nomeCategoria = categoria;
            }
        }
        return servicoDAO.listar(nome, regiao, idCategoria, nomeCategoria);
    }

    // GET /categorias-servico/{id}/servicos
    public List<ServicoAcolhimento> listarPorCategoria(Long idCategoria) {
        if (categoriaDAO.buscarPorId(idCategoria) == null) {
            throw new RecursoNaoEncontradoException("Categoria de serviço não encontrada com o id " + idCategoria);
        }
        return servicoDAO.listar(null, null, idCategoria, null);
    }

    public ServicoAcolhimento buscarPorId(Long id) {
        ServicoAcolhimento servico = servicoDAO.buscarPorId(id);
        if (servico == null) {
            throw new RecursoNaoEncontradoException("Serviço de acolhimento não encontrado com o id " + id);
        }
        return servico;
    }

    public ServicoAcolhimento atualizar(Long id, ServicoAcolhimento dados) {
        buscarPorId(id);

        if (categoriaDAO.buscarPorId(dados.getIdCategoriaServico()) == null) {
            throw new RequisicaoInvalidaException(
                    "A categoria de serviço " + dados.getIdCategoriaServico() + " não existe.");
        }

        limpar(dados);
        validarDuplicado(dados, id);

        dados.setIdServico(id);
        servicoDAO.atualizar(dados);
        return buscarPorId(id);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        servicoDAO.excluir(id);
    }

    private ServicoAcolhimento salvarNovo(ServicoAcolhimento servico) {
        limpar(servico);
        validarDuplicado(servico, 0L);

        Long id = servicoDAO.inserir(servico);
        return buscarPorId(id);
    }

    private void limpar(ServicoAcolhimento servico) {
        servico.setNome(servico.getNome().trim());
        servico.setEndereco(servico.getEndereco().trim());
        servico.setRegiao(servico.getRegiao().trim());
    }

    // Regra de negócio: não pode haver dois serviços com o mesmo nome e endereço
    private void validarDuplicado(ServicoAcolhimento servico, Long idIgnorar) {
        if (servicoDAO.existeDuplicado(servico.getNome(), servico.getEndereco(), idIgnorar)) {
            throw new RegraNegocioException(
                    "Já existe um serviço com o nome '" + servico.getNome()
                            + "' neste endereço.");
        }
    }
}
