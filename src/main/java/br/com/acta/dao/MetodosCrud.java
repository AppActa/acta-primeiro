package br.com.acta.dao;

import java.util.List;

public interface MetodosCrud<T> {

    int inserir(T t);
    T buscar(int id);
    List<T> buscar();
    int atualizar(T t);
    int excluir(int id);
}

