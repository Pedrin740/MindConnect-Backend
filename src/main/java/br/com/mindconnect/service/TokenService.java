package br.com.mindconnect.service;

import br.com.mindconnect.exception.ErroBancoDadosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

// Gera e valida o token que identifica o usuário logado nas rotas protegidas.
// Formato: idUsuario.expiracao.assinatura  (assinatura = HMAC-SHA256 com um segredo só do servidor).
// Como o token é assinado, não dá para trocar o id dentro dele sem invalidar a assinatura.
//
// O segredo vem da variável de ambiente AUTH_SECRET (mínimo 16 caracteres).
// Sem ela, um segredo aleatório é criado na inicialização: tudo funciona,
// mas os tokens deixam de valer quando a aplicação reinicia.
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final String ALGORITMO = "HmacSHA256";
    private static final long VALIDADE_SEGUNDOS = 8L * 60 * 60; // 8 horas

    private final byte[] segredo;

    public TokenService() {
        String configurado = System.getenv("AUTH_SECRET");

        if (configurado != null && configurado.length() >= 16) {
            this.segredo = configurado.getBytes(StandardCharsets.UTF_8);
        } else {
            this.segredo = new byte[32];
            new SecureRandom().nextBytes(this.segredo);
            log.warn("AUTH_SECRET não definida (ou com menos de 16 caracteres): usando segredo temporário. "
                    + "Os logins serão invalidados quando a aplicação reiniciar.");
        }
    }

    public String gerar(Long idUsuario) {
        long expiracao = Instant.now().getEpochSecond() + VALIDADE_SEGUNDOS;
        String conteudo = idUsuario + "." + expiracao;
        return conteudo + "." + assinar(conteudo);
    }

    // Devolve o id do usuário dono do token, ou null se o token for inválido ou estiver expirado
    public Long validar(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return null;
        }

        String conteudo = partes[0] + "." + partes[1];
        byte[] esperada = assinar(conteudo).getBytes(StandardCharsets.UTF_8);
        byte[] recebida = partes[2].getBytes(StandardCharsets.UTF_8);

        // comparação em tempo constante
        if (!MessageDigest.isEqual(esperada, recebida)) {
            return null;
        }

        try {
            long expiracao = Long.parseLong(partes[1]);
            if (expiracao <= Instant.now().getEpochSecond()) {
                return null;
            }
            return Long.valueOf(partes[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(segredo, ALGORITMO));
            byte[] assinatura = mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(assinatura);
        } catch (GeneralSecurityException e) {
            throw new ErroBancoDadosException("Erro ao assinar o token", e);
        }
    }
}
