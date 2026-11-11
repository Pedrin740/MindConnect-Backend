package br.com.mindconnect.service;

import br.com.mindconnect.dao.CategoriaConteudoDAO;
import br.com.mindconnect.dao.ConteudoDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import br.com.mindconnect.model.CategoriaConteudo;
import br.com.mindconnect.model.Conteudo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConteudoService {

    private final ConteudoDAO conteudoDAO;
    private final CategoriaConteudoDAO categoriaDAO;

    public ConteudoService(ConteudoDAO conteudoDAO, CategoriaConteudoDAO categoriaDAO) {
        this.conteudoDAO = conteudoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public Conteudo cadastrar(Conteudo conteudo, List<Long> idsCategorias) {
        List<Long> ids = validarCategorias(idsCategorias);

        // valores padrão quando não enviados
        if (conteudo.getStatus() == null || conteudo.getStatus().isBlank()) {
            conteudo.setStatus("ATIVO");
        }
        if (conteudo.getDataPublicacao() == null) {
            conteudo.setDataPublicacao(LocalDate.now());
        }

        Long id = conteudoDAO.inserir(conteudo, ids);
        return buscarPorId(id);
    }

    public List<Conteudo> listar(String busca, String status, String categoria) {
        Long idCategoria = null;
        String nomeCategoria = null;

        // a categoria pode ser informada pelo id (numérico) ou pelo nome
        if (categoria != null && !categoria.isBlank()) {
            if (categoria.trim().matches("\\d{1,18}")) {
                idCategoria = Long.valueOf(categoria.trim());
            } else {
                nomeCategoria = categoria;
            }
        }
        return conteudoDAO.listar(busca, status, idCategoria, nomeCategoria);
    }

    public Conteudo buscarPorId(Long id) {
        Conteudo conteudo = conteudoDAO.buscarPorId(id);
        if (conteudo == null) {
            throw new RecursoNaoEncontradoException("Conteúdo não encontrado com o id " + id);
        }
        return conteudo;
    }

    public Conteudo atualizar(Long id, Conteudo dados, List<Long> idsCategorias) {
        Conteudo existente = buscarPorId(id);

        List<Long> ids;
        if (idsCategorias == null) {
            // categorias não enviadas: mantém as que o conteúdo já tinha
            ids = new ArrayList<>();
            for (CategoriaConteudo categoria : existente.getCategorias()) {
                ids.add(categoria.getIdCategoriaConteudo());
            }
        } else {
            ids = validarCategorias(idsCategorias);
        }

        if (dados.getStatus() == null || dados.getStatus().isBlank()) {
            dados.setStatus(existente.getStatus());
        }
        if (dados.getDataPublicacao() == null) {
            dados.setDataPublicacao(existente.getDataPublicacao() != null
                    ? existente.getDataPublicacao() : LocalDate.now());
        }

        dados.setIdConteudo(id);
        conteudoDAO.atualizar(dados, ids);
        return buscarPorId(id);
    }

    public void excluir(Long id) {
        buscarPorId(id);
        conteudoDAO.excluir(id);
    }

    // Remove ids repetidos e confere se cada categoria existe
    private List<Long> validarCategorias(List<Long> idsCategorias) {
        List<Long> ids = new ArrayList<>();
        if (idsCategorias == null) {
            return ids;
        }

        for (Long id : idsCategorias) {
            if (id == null || id <= 0) {
                throw new RequisicaoInvalidaException("Id de categoria inválido.");
            }
            if (ids.contains(id)) {
                continue;
            }
            if (categoriaDAO.buscarPorId(id) == null) {
                throw new RequisicaoInvalidaException("A categoria de conteúdo " + id + " não existe.");
            }
            ids.add(id);
        }
        return ids;
    }
}
