package br.com.acta.excecoes;

/**
 * Violação de chave estrangeira (SQLState 23503): referência que não existe
 * ou registro que ainda é usado por outro. O servlet transforma em HTTP 409.
 */

public class IntegridadeReferencialException extends RuntimeException {

    public IntegridadeReferencialException(String mensagem, Throwable causa) {
        super(mensagem,causa);
    }
}
