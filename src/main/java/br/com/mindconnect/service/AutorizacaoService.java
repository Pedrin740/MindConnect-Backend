package br.com.mindconnect.service;

import br.com.mindconnect.dao.AdministradorDAO;
import br.com.mindconnect.exception.NaoAutenticadoException;
import org.springframework.stereotype.Service;

@Service
public class AutorizacaoService {

    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;
    private final AdministradorDAO administradorDAO;

    public AutorizacaoService(TokenService tokenService, AdministradorDAO administradorDAO) {
        this.tokenService = tokenService;
        this.administradorDAO = administradorDAO;
    }

    // Lê o header Authorization e devolve o id do usuário logado (ou 401)
    public Long identificar(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, PREFIXO, 0, PREFIXO.length())) {
            throw new NaoAutenticadoException();
        }

        Long idUsuario = tokenService.validar(authorization.substring(PREFIXO.length()).trim());
        if (idUsuario == null) {
            throw new NaoAutenticadoException();
        }
        return idUsuario;
    }

    // A permissão de administrador é conferida no banco a cada requisição
    // (e não guardada no token), então remover o acesso vale na hora.
    public boolean ehAdministrador(Long idUsuario) {
        return administradorDAO.buscarPorUsuario(idUsuario) != null;
    }
}
