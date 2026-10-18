package br.com.acta.excecoes;

    /*
    erro de dados errados
    É uma exceção checada: o servlet é obrigado a tratar. Vira HTTP 400.
    */

public class Validacaoexception extends Exception{

    public Validacaoexception(String mensagem){
        super(mensagem);
    }

}
