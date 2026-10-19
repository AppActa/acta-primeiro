package br.com.acta.excecoes;

//Erro ao acessar o banco; Substitui o RuntimeException(e) genérico
//Nos dao, guarda a causa original, vira http 500.

public class DaoException extends RuntimeException{

    public DaoException(String mensagem, Throwable causa){
        super(mensagem);
    }
}
