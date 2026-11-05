package br.com.acta.utils;

import com.password4j.Hash;
import com.password4j.Password;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/** Centraliza a geração e a verificação dos hashes de senha. */
public class PasswordUtils {

    // Segredo compartilhado, obtido do ambiente ou do .env.
    private static final String PEPPER = Configuracao.obter("PASSWORD_PEPPER");
    private static final String PREFIXO_V2 = "{acta-v2}";

    /** Processa toda a senha com HMAC antes do BCrypt, evitando o limite de 72 bytes. */
    public static String hashSenha(String senha) {
        exigirPepper();
        Hash hash = Password.hash(prepararSenha(senha)).withBcrypt();
        return PREFIXO_V2 + hash.getResult();
    }

    /** Compara a senha recebida com o hash armazenado, usando o mesmo pepper. */
    public static boolean verificarSenha(String senha, String hash) {
        exigirPepper();
        if (senha == null || senha.isEmpty() || hash == null || hash.isBlank()) {
            return false;
        }
        if (hash.startsWith(PREFIXO_V2)) {
            return Password.check(prepararSenha(senha), hash.substring(PREFIXO_V2.length())).withBcrypt();
        }
        // Preserva a verificação original das contas existentes, inclusive o limite do BCrypt.
        // O formato v2 passa a valer quando a senha é criada ou alterada.
        return Password.check(senha, hash).addPepper(PEPPER).withBcrypt();
    }

    /** HMAC-SHA384 em Base64 produz 64 caracteres ASCII, abaixo do limite do BCrypt. */
    private static String prepararSenha(String senha) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA384");
            hmac.init(new SecretKeySpec(PEPPER.getBytes(StandardCharsets.UTF_8), "HmacSHA384"));
            return Base64.getEncoder().encodeToString(hmac.doFinal(senha.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível preparar a senha com segurança.", e);
        }
    }

    /** Interrompe a operação quando o segredo de senhas não está configurado. */
    private static void exigirPepper() {
        if (PEPPER == null || PEPPER.isBlank()) {
            throw new IllegalStateException("Configure PASSWORD_PEPPER no ambiente do servidor.");
        }
    }
}
