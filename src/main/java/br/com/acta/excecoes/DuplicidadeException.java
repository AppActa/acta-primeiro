package br.com.acta.excecoes;

/** Violação de unicidade (SQLState 23505), tratada como HTTP 409. */

public class DuplicidadeException extends RuntimeException {

    /** Informa a duplicidade e preserva a exceção lançada pelo banco. */
    public DuplicidadeException(String mensagem, Throwable causa) {
        super(mensagem,causa);
    }
}
