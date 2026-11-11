package br.com.mindconnect.service;

import br.com.mindconnect.exception.ErroBancoDadosException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Guarda as senhas com hash PBKDF2 + salt (nunca em texto puro).
// Formato salvo na coluna SENHA: pbkdf2$iteracoes$salt$hash
@Service
public class SenhaService {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIXO = "pbkdf2";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        random.nextBytes(salt);
        byte[] hash = calcular(senha, salt, ITERACOES);

        return PREFIXO + "$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean conferir(String senha, String armazenado) {
        if (senha == null || armazenado == null) {
            return false;
        }

        String[] partes = armazenado.split("\\$");
        if (partes.length != 4 || !PREFIXO.equals(partes[0])) {
            return false;
        }

        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = calcular(senha, salt, iteracoes);

            // comparação em tempo constante
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] calcular(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new ErroBancoDadosException("Erro ao gerar hash da senha", e);
        }
    }
}
