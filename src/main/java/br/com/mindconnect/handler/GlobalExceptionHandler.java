package br.com.mindconnect.handler;

import br.com.mindconnect.dto.ErroDto;
import br.com.mindconnect.exception.AcessoNegadoException;
import br.com.mindconnect.exception.CredenciaisInvalidasException;
import br.com.mindconnect.exception.NaoAutenticadoException;
import br.com.mindconnect.exception.RecursoNaoEncontradoException;
import br.com.mindconnect.exception.RegraNegocioException;
import br.com.mindconnect.exception.RequisicaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Captura TODOS os erros da API e devolve sempre o mesmo JSON (ErroDto).
// Nenhum stack trace e nenhum detalhe de SQL sai daqui: o detalhe técnico vai só para o log.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - registro não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDto> tratarNaoEncontrado(RecursoNaoEncontradoException e,
                                                       HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    // 409 - regra de negócio violada ou remoção de registro com filhos
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDto> tratarRegraNegocio(RegraNegocioException e,
                                                      HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    // 400 - dado inválido para o sistema (ex.: id de categoria que não existe)
    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroDto> tratarRequisicaoInvalida(RequisicaoInvalidaException e,
                                                            HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    // 401 - login inválido
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroDto> tratarCredenciais(CredenciaisInvalidasException e,
                                                     HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    // 401 - rota protegida sem token válido
    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroDto> tratarNaoAutenticado(NaoAutenticadoException e,
                                                        HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    // 403 - usuário logado, mas sem permissão para a operação ou para os dados de outra pessoa
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroDto> tratarAcessoNegado(AcessoNegadoException e,
                                                      HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    // 400 - falha no Bean Validation (@Valid no controller)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDto> tratarValidacao(MethodArgumentNotValidException e,
                                                   HttpServletRequest request) {
        ErroDto erro = new ErroDto(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Dados inválidos. Verifique os campos informados.",
                request.getRequestURI());

        e.getBindingResult().getFieldErrors().forEach(campo ->
                erro.getCampos().putIfAbsent(campo.getField(), campo.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 400 - JSON malformado, vazio ou com tipo errado (ex.: texto onde é número ou data inválida)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDto> tratarJsonInvalido(HttpMessageNotReadableException e,
                                                      HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido. Envie um JSON válido, com os tipos e as datas (aaaa-mm-dd) corretos.",
                request);
    }

    // 400 - parâmetro da URL com tipo errado (ex.: /usuarios/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDto> tratarTipoParametro(MethodArgumentTypeMismatchException e,
                                                       HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "O valor informado para '" + e.getName() + "' é inválido.", request);
    }

    // 405 - método HTTP não suportado na rota
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroDto> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                            HttpServletRequest request) {
        ErroDto erro = new ErroDto(HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                "O método " + e.getMethod() + " não é suportado para esta rota.",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(e.getHeaders())
                .body(erro);
    }

    // Qualquer outro erro: erros do próprio Spring (404 de rota, 415, parâmetro faltando...)
    // mantêm o status deles; o resto vira 500 com mensagem genérica.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDto> tratarGenerico(Exception e, HttpServletRequest request) {

        if (e instanceof ErrorResponse resposta) {
            HttpStatusCode codigo = resposta.getStatusCode();
            HttpStatus status = HttpStatus.resolve(codigo.value());

            if (status != null && status.is4xxClientError()) {
                String mensagem = status == HttpStatus.NOT_FOUND
                        ? "Rota não encontrada."
                        : "Requisição inválida.";
                return montar(status, mensagem, request);
            }
        }

        // o detalhe (inclusive SQL) fica só no log do servidor
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErroDto> montar(HttpStatus status, String mensagem,
                                           HttpServletRequest request) {
        ErroDto erro = new ErroDto(status.value(), status.getReasonPhrase(),
                mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(erro);
    }
}
