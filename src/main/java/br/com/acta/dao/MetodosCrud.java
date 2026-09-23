package br.com.acta.dao;

import java.util.List;

public interface MetodosCrud<T> {
    T inserir(T t);
    T buscar(Long id);
    List<T> buscar();
    Long atualizar(T t);
    Long excluir(Long id);
}
