package br.com.acta.dao;

import java.util.List;

public interface MetodosCrud<T> {

    String inserir(T t);
    T buscar(Long id);
    List<T> buscar();
    String atualizar(T t);
    String excluir(Long id);
}

