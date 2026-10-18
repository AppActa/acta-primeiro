package br.com.acta.excecoes;

//Erro ao acessar o banco; Substitui o RuntimeException(e) genérico
//Nos dao, guarda a causa original, vira http 500.

public class Daoexception extends RuntimeException{
    public Daoexception(String mensagem,Throwable causa){
        super(mensagem);
    }
}
