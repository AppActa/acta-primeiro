package br.com.acta.excecoes;

    /*
    erro de dados errados
    */

public class ValidacaoException extends RuntimeException{

    public ValidacaoException(String mensagem){

        super(mensagem);
    }

}
