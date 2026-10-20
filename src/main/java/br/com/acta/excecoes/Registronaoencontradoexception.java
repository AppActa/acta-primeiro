package br.com.acta.excecoes;

// Quando não indentifica o que procurar no banco

public class RegistroNaoEncontradoException extends RuntimeException {

    public RegistroNaoEncontradoException(String mensagem){
        super(mensagem);
    }
}
