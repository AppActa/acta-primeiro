package br.com.acta.utils;

//imports
import com.password4j.Hash;
import com.password4j.Password;

public class PasswordUtils {
    //acessa o dotenv
    private static final String PEPPER = Configuracao.obter("PASSWORD_PEPPER");

    //transforma a senha digitada em hash
    public static String hashSenha(String senha) {
        exigirPepper();
        Hash hash = Password.hash(senha).addPepper(PEPPER).withBcrypt();
        return hash.getResult();
    }

    //compara a senha digitada com o hash salvo no banco
    public static boolean verificarSenha(String senha, String hash){
        exigirPepper();
        return Password.check(senha,hash).addPepper(PEPPER).withBcrypt();
    }

    private static void exigirPepper() {
        if (PEPPER == null || PEPPER.isBlank()) {
            throw new IllegalStateException("Configure PASSWORD_PEPPER no ambiente do servidor.");
        }
    }
}
