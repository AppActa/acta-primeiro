package br.com.acta.excecoes;

/**
 * Valor repetido em coluna UNIQUE (e-mail, CPF, CNPJ...). SQLState 23505.
 * O servlet transforma em HTTP 409.
 */

public class DuplicidadeException extends RuntimeException {

    public DuplicidadeException(String mensagem, Throwable causa) {
        super(mensagem,causa);
    }
}
