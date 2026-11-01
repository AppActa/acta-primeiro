package br.com.acta.excecoes;

/** Registro solicitado não existe; os servlets respondem com HTTP 404. */

public class RegistroNaoEncontradoException extends RuntimeException {

    /** Descreve qual registro não foi encontrado. */
    public RegistroNaoEncontradoException(String mensagem){
        super(mensagem);
    }
}
