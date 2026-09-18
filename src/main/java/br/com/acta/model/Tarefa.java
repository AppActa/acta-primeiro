package br.com.acta.model;

import java.sql.Date;

public class Tarefa {

    //Atributos

    private Long id_tarefa;
    private String titulo;
    private String descricao;
    private String prioridade;
    private Date dt_entrega;
    private String status;
    private Date dt_inicio;
    private Long id_colaborador;

    //Construtor

    //Para o do get
    public Tarefa() {}

    //Para o inserir
    public Tarefa(String titulo, String descricao, String prioridade, Date dt_entrega, String status, Date dt_inicio, Long id_colaborador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
    }

    //Com id para os demais
    public Tarefa(Long id_tarefa, String titulo, String descricao, String prioridade, Date dt_entrega, String status, Date dt_inicio, Long id_colaborador) {
        this.id_tarefa = id_tarefa;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
    }

    //Getters e Setters

    public Long getTarefa_id() {
        return id_tarefa;
    }

    public void setTarefa_id(Long id_tarefa) {
        this.id_tarefa = id_tarefa;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public Date getDt_entrega() {
        return dt_entrega;
    }

    public void setDt_entrega(Date dt_entrega) {
        this.dt_entrega = dt_entrega;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDt_inicio() {
        return dt_inicio;
    }

    public void setDt_inicio(Date dt_inicio) {
        this.dt_inicio = dt_inicio;
    }

    public Long getColaborador_id() {
        return id_colaborador;
    }

    public void setColaborador_id(Long id_colaborador) {
        this.id_colaborador = id_colaborador;
    }
}