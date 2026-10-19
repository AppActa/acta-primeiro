package br.com.acta.excecoes;

    /*
    erro de dados errados
    */

public class Validacaoexception extends RuntimeException{

    public Validacaoexception(String mensagem){

        super(mensagem);
    }

}
