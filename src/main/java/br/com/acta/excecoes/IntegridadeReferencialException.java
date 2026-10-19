package br.com.acta.excecoes;

/*
Imagine que uma empresa esteja relacionada a vários colaboradores. Se o banco tiver uma chave estrangeira
 que impeça a exclusão dessa empresa enquanto existirem colaboradores vinculados, o PostgreSQL poderá
 gerar um erro.
 */

public class IntegridadeReferencialException extends RuntimeException {

    public IntegridadeReferencialException(String mensagem, Throwable causa) {
        super(mensagem);
    }
}
