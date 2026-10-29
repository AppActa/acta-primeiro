package br.com.acta.utils;

//imports
import com.password4j.Hash;
import com.password4j.Password;
import io.github.cdimascio.dotenv.Dotenv;

public class PasswordUtils {
    //acessa o dotenv
    private static final Dotenv dotenv = Dotenv.load();
    private static final String PEPPER = dotenv.get("PASSWORD_PEPPER");

    //transforma a senha digitada em hash
    public static String hashSenha(String senha) {
        Hash hash = Password.hash(senha).addPepper(PEPPER).withBcrypt();
        return hash.getResult();
    }

    //compara a senha digitada com o hash salvo no banco
    public static boolean verificarSenha(String senha, String hash){
        return Password.check(senha,hash).addPepper(PEPPER).withBcrypt();
    }
}
