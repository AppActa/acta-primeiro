package br.com.acta.excecoes;

/**
 * Dado de entrada inválido (campo vazio, formato errado...).
 * O servlet transforma em HTTP 400.
 */

public class ValidacaoException extends RuntimeException{

    public ValidacaoException(String mensagem){

        super(mensagem);
    }

}
