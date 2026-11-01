package br.com.acta.excecoes;

/** Referência inválida ou ainda em uso (SQLState 23503); corresponde a HTTP 409. */

public class IntegridadeReferencialException extends RuntimeException {

    /** Descreve o vínculo que impediu a operação e mantém a causa original. */
    public IntegridadeReferencialException(String mensagem, Throwable causa) {
        super(mensagem,causa);
    }
}
