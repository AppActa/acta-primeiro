package br.com.acta.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String DATABASE_URL = Configuracao.obter("DB_URL");
    private static final String DATABASE_USER = Configuracao.obter("DB_USER");
    private static final String DATABASE_PASSWORD = Configuracao.obter("DB_PASSWORD");

    public static Connection conectar() throws ClassNotFoundException, SQLException {
        if (DATABASE_URL == null || DATABASE_URL.isBlank()
                || DATABASE_USER == null || DATABASE_USER.isBlank()
                || DATABASE_PASSWORD == null || DATABASE_PASSWORD.isBlank()) {
            throw new SQLException("Configuração do banco ausente: verifique DB_URL, DB_USER e DB_PASSWORD. "
                    + "Em produção, defina essas variáveis no ambiente do Tomcat.");
        }
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(urlComTlsParaBancoRemoto(DATABASE_URL), DATABASE_USER, DATABASE_PASSWORD);
    }

    private static String urlComTlsParaBancoRemoto(String url) {
        boolean bancoLocal = url.matches("(?i)^jdbc:postgresql://(localhost|127\\.0\\.0\\.1)(:[0-9]+)?/.*");
        if (bancoLocal || url.matches("(?i).*([?&])sslmode=[^&]+.*")) return url;
        return url + (url.contains("?") ? "&" : "?") + "sslmode=require";
    }

    public static void desconectar(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
