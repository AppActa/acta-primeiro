package br.com.acta.excecoes;

/** Falha de acesso ao banco, tratada pelos servlets como HTTP 500. */
public class DaoException extends RuntimeException {

    /** Preserva a mensagem da operação e a causa original da falha. */
    public DaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
