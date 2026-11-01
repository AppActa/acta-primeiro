package br.com.acta.model;

import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;

import java.sql.Date;

/** Representa uma tarefa do plano de ação e o colaborador responsável. */
public class Tarefa {

    // Descrição, prioridade, datas e andamento da tarefa.

    private Long id_tarefa;
    private String titulo;
    private String descricao;
    private Intensidade prioridade;
    private Date dt_entrega;
    private Situacao status;
    private Date dt_inicio;
    // IDs do responsável e do plano ao qual a tarefa pertence.
    private Long id_colaborador;
    private Long id_plano_acao;
    // Nomes trazidos pelas consultas para exibir os vínculos.
    private String nome_colaborador;
    private String nome_plano_acao;

    /** Cria uma tarefa vazia para preenchimento posterior. */
    public Tarefa() {}

    /** Monta os dados da tarefa sem um ID definido. */
    public Tarefa(String titulo, String descricao, Intensidade prioridade, Date dt_entrega, Situacao status, Date dt_inicio, Long id_colaborador, Long id_plano_acao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
        this.id_plano_acao = id_plano_acao;
    }

    /** Monta uma tarefa com ID já conhecido. */
    public Tarefa(Long id_tarefa, String titulo, String descricao, Intensidade prioridade, Date dt_entrega, Situacao status, Date dt_inicio, Long id_colaborador, Long id_plano_acao) {
        this.id_tarefa = id_tarefa;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dt_entrega = dt_entrega;
        this.status = status;
        this.dt_inicio = dt_inicio;
        this.id_colaborador = id_colaborador;
        this.id_plano_acao = id_plano_acao;
    }

    // Acesso e atualização dos dados da tarefa.
    public Long getId_tarefa() {
        return id_tarefa;
    }

    public void setId_tarefa(Long id_tarefa) {
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

    public Intensidade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Intensidade prioridade) {
        this.prioridade = prioridade;
    }

    public Date getDt_entrega() {
        return dt_entrega;
    }

    public void setDt_entrega(Date dt_entrega) {
        this.dt_entrega = dt_entrega;
    }

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    public Date getDt_inicio() {
        return dt_inicio;
    }

    public void setDt_inicio(Date dt_inicio) {
        this.dt_inicio = dt_inicio;
    }

    public Long getId_colaborador() {
        return id_colaborador;
    }

    public void setId_colaborador(Long id_colaborador) {
        this.id_colaborador = id_colaborador;
    }

    public Long getId_plano_acao() {
        return id_plano_acao;
    }

    public void setId_plano_acao(Long id_plano_acao) {
        this.id_plano_acao = id_plano_acao;
    }

    public String getNome_colaborador() {
        return nome_colaborador;
    }

    public void setNome_colaborador(String nome_colaborador) {
        this.nome_colaborador = nome_colaborador;
    }

    public String getNome_plano_acao() {
        return nome_plano_acao;
    }

    public void setNome_plano_acao(String nome_plano_acao) {
        this.nome_plano_acao = nome_plano_acao;
    }
}