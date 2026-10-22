package br.com.acta.excecoes;

/**
 * Erro ao acessar o banco (SQL, driver ou conexão).
 * Guarda a causa original. O servlet transforma em HTTP 500.
 */

public class DaoException extends RuntimeException{

    public DaoException(String mensagem, Throwable causa){
        super(mensagem,causa);
    }
}
