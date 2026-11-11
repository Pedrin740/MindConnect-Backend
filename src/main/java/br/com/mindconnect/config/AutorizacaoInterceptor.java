package br.com.mindconnect.config;

import br.com.mindconnect.exception.AcessoNegadoException;
import br.com.mindconnect.service.AutorizacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

// Roda ANTES do controller em toda rota /api/**: identifica o usuário pelo token
// e confere a permissão exigida pela anotação @Acesso. Se não puder, o controller nem é executado
// (401 sem login válido, 403 sem permissão).
@Component
public class AutorizacaoInterceptor implements HandlerInterceptor {

    // Nome do atributo da requisição com o id do usuário logado (lido nos controllers)
    public static final String ATRIBUTO_USUARIO = "idUsuarioLogado";

    private final AutorizacaoService autorizacaoService;

    public AutorizacaoInterceptor(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // pré-requisições do CORS (OPTIONS) e rotas que não são de controller passam direto
        if (!(handler instanceof HandlerMethod metodo) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Acesso acesso = metodo.getMethodAnnotation(Acesso.class);
        if (acesso == null) {
            acesso = metodo.getBeanType().getAnnotation(Acesso.class);
        }
        Nivel nivel = acesso != null ? acesso.value() : Nivel.ADMINISTRADOR;

        if (nivel == Nivel.PUBLICO) {
            return true;
        }

        // a partir daqui é preciso estar logado (401 se não estiver)
        Long idLogado = autorizacaoService.identificar(request.getHeader("Authorization"));
        request.setAttribute(ATRIBUTO_USUARIO, idLogado);

        switch (nivel) {
            case LOGADO:
                break;
            case ADMINISTRADOR:
                exigirAdministrador(idLogado);
                break;
            case PROPRIETARIO:
                exigirProprietario(request, idLogado);
                break;
            case PROPRIETARIO_OU_ADMINISTRADOR:
                if (!ehProprietario(request, idLogado)) {
                    exigirAdministrador(idLogado);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void exigirAdministrador(Long idLogado) {
        if (!autorizacaoService.ehAdministrador(idLogado)) {
            throw new AcessoNegadoException("Esta operação é exclusiva de administradores.");
        }
    }

    private void exigirProprietario(HttpServletRequest request, Long idLogado) {
        if (!ehProprietario(request, idLogado)) {
            throw new AcessoNegadoException("Você só pode acessar os seus próprios dados.");
        }
    }

    // Compara o {id} da URL com o usuário logado.
    // O id precisa estar em formato decimal estrito (sem zero à esquerda, sinal ou hexadecimal):
    // assim o número que conferimos aqui é exatamente o que o Spring vai entregar ao controller.
    private boolean ehProprietario(HttpServletRequest request, Long idLogado) {
        Object variaveis = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(variaveis instanceof Map<?, ?> mapa)) {
            return false;
        }

        Object id = mapa.get("id");
        if (!(id instanceof String texto) || !texto.matches("[1-9][0-9]{0,17}")) {
            return false;
        }
        return Long.parseLong(texto) == idLogado.longValue();
    }
}
