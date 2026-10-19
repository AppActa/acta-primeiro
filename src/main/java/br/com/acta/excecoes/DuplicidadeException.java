package br.com.acta.excecoes;

//Verifica se a informações unicas tentando ser inseridas novamente.

public class DuplicidadeException extends RuntimeException {

    public DuplicidadeException(String mensagem, Throwable causa) {
        super(mensagem);
    }
}
