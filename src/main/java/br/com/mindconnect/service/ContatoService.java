package br.com.mindconnect.service;

import br.com.mindconnect.dao.ContatoDAO;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.model.Contato;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ContatoService {

    private final ContatoDAO contatoDAO;

    public ContatoService(ContatoDAO contatoDAO) {
        this.contatoDAO = contatoDAO;
    }

    public Contato cadastrar(Contato contato) {

        contato.setNome(contato.getNome().trim());

        contato.setEmail(
                contato.getEmail().trim().toLowerCase(Locale.ROOT)
        );

        contato.setMotivo(contato.getMotivo().trim());
        contato.setMensagem(contato.getMensagem().trim());

        Long id = contatoDAO.inserir(contato);

        if (id == null) {
            throw new IllegalStateException(
                    "Não foi possível confirmar o registro da mensagem."
            );
        }

        return buscarPorId(id);
    }

    private Contato buscarPorId(Long id) {

        Contato contato = contatoDAO.buscarPorId(id);

        if (contato == null) {
            throw new RecursoNaoEncontradoException(
                    "Mensagem de contato não encontrada após o cadastro."
            );
        }

        return contato;
    }
}
