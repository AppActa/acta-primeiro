package br.com.acta.utils;

import com.password4j.Hash;
import com.password4j.Password;

/** Centraliza a geração e a verificação dos hashes de senha. */
public class PasswordUtils {
    // Segredo compartilhado, obtido do ambiente ou do .env.
    private static final String PEPPER = Configuracao.obter("PASSWORD_PEPPER");

    /** Gera o hash BCrypt da senha com o pepper configurado. */
    public static String hashSenha(String senha) {
        exigirPepper();
        Hash hash = Password.hash(senha).addPepper(PEPPER).withBcrypt();
        return hash.getResult();
    }

    /** Compara a senha recebida com o hash armazenado, usando o mesmo pepper. */
    public static boolean verificarSenha(String senha, String hash){
        exigirPepper();
        return Password.check(senha,hash).addPepper(PEPPER).withBcrypt();
    }

    /** Interrompe a operação quando o segredo de senhas não está configurado. */
    private static void exigirPepper() {
        if (PEPPER == null || PEPPER.isBlank()) {
            throw new IllegalStateException("Configure PASSWORD_PEPPER no ambiente do servidor.");
        }
    }
}
