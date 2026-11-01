package br.com.acta.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifica a exigência de TLS nas URLs JDBC sem tentar abrir conexão. */
class ConexaoTest {

    @Test
    void exigeTlsRemotoEPreservaUrlsLocaisOuJaConfiguradas() throws Exception {
        Method metodo = Conexao.class.getDeclaredMethod("urlComTlsParaBancoRemoto", String.class);
        metodo.setAccessible(true);

        assertEquals("jdbc:postgresql://localhost:5432/acta",
                metodo.invoke(null, "jdbc:postgresql://localhost:5432/acta"));
        assertEquals("jdbc:postgresql://127.0.0.1/acta",
                metodo.invoke(null, "jdbc:postgresql://127.0.0.1/acta"));
        assertEquals("jdbc:postgresql://db.exemplo/acta?sslmode=require",
                metodo.invoke(null, "jdbc:postgresql://db.exemplo/acta"));
        assertEquals("jdbc:postgresql://db.exemplo/acta?sslmode=verify-full",
                metodo.invoke(null, "jdbc:postgresql://db.exemplo/acta?sslmode=verify-full"));
        assertEquals("jdbc:postgresql://db.exemplo/acta?x=1&sslmode=require",
                metodo.invoke(null, "jdbc:postgresql://db.exemplo/acta?x=1"));
    }
}
