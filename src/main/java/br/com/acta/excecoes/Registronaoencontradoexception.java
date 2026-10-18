package br.com.acta.excecoes;

// Quando não indentifica o que procurar no banco

public class Registronaoencontradoexception extends Exception {
    public Registronaoencontradoexception(String mensagem){
        super(mensagem);
    }
}
