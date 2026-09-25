package br.com.acta.model;

import br.com.acta.enums.Prioridade;
import br.com.acta.enums.Situacao;

import java.sql.Date;

public class Tarefa {

    //Atributos

    private Long id_tarefa;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private Date dt_entrega;
    private Situacao status;
    private Date dt_inicio;
    private Long id_colaborador;


    public Tarefa() {}

    public Tarefa(String titulo, String descricao, Prioridade prioridade, Date dt_entrega, Situacao status, Date dt_inicio, Long id_colaborador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
    }

    public Tarefa(Long id_tarefa, String titulo, String descricao, Prioridade prioridade, Date dt_entrega, Situacao status, Date dt_inicio, Long id_colaborador) {
        this.id_tarefa = id_tarefa;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
    }

    public Long getId_tarefa() {
        return id_tarefa;
    }

    public void setId_tarefa(Long id_tarefa) {
        this.id_tarefa = id_tarefa;
    }

    public Long getId_colaborador() {
        return id_colaborador;
    }

    public void setId_colaborador(Long id_colaborador) {
        this.id_colaborador = id_colaborador;
    }

    public Date getDt_inicio() {
        return dt_inicio;
    }

    public void setDt_inicio(Date dt_inicio) {
        this.dt_inicio = dt_inicio;
    }

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    public Date getDt_entrega() {
        return dt_entrega;
    }

    public void setDt_entrega(Date dt_entrega) {
        this.dt_entrega = dt_entrega;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}