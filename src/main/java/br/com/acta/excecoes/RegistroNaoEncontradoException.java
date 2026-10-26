package br.com.acta.excecoes;

/**
 * Registro procurado não existe (buscar, atualizar ou excluir com id inexistente).
 * O servlet transforma em HTTP 404.
 */

public class RegistroNaoEncontradoException extends RuntimeException {

    public RegistroNaoEncontradoException(String mensagem){
        super(mensagem);
    }
}
