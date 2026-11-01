package br.com.acta.excecoes;

/** Entrada inválida, como campo vazio ou formato incorreto; corresponde a HTTP 400. */
public class ValidacaoException extends RuntimeException {

    /** Leva ao servlet a mensagem da regra de validação que falhou. */
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
