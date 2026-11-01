package br.com.acta.dao;

import java.util.List;

/** Contrato de persistência; T representa o tipo de modelo atendido pelo DAO. */
public interface MetodosCrud<T> {
    /** Insere o modelo e informa se houve gravação. */
    int inserir(T t);

    /** Busca um registro pelo ID; pode retornar null quando ausente. */
    T buscar(Long id);

    /** Lista os registros encontrados pelo DAO. */
    List<T> buscar();

    /** Atualiza o registro identificado no modelo. */
    int atualizar(T t);

    /** Exclui o registro identificado pelo ID. */
    int excluir(Long id);
}
