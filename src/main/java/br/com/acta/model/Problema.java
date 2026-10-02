package br.com.acta.model;


import br.com.acta.enums.Intensidade;
import br.com.acta.enums.StatusProblema;

import java.sql.Date;

public class Problema {

    private Long id_problema;
    private String titulo;
    private String descricao;
    private Intensidade peso;
    private String solucao;
    private StatusProblema status;
    private String origem;
    private Date encontrado_em;
    private Long id_ciclo;
    private Long id_plano_acao;
    private Long id_colaborador;
    private String nomeCiclo;
    private String nomePlanoAcao;
    private String nomeColaborador;

    public Problema() {}

    public Problema(String titulo, String descricao, Intensidade peso, String solucao, StatusProblema status, String origem, Date encontrado_em, Long id_ciclo, Long id_plano_acao, Long id_colaborador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.peso = peso;
        this.solucao = solucao;
        this.status = status;
        this.origem = origem;
        this.encontrado_em = encontrado_em;
        this.id_ciclo = id_ciclo;
        this.id_plano_acao = id_plano_acao;
        this.id_colaborador = id_colaborador;
    }

    public Problema(Long id_problema, String titulo, String descricao,Intensidade peso, String solucao, StatusProblema status, String origem, Date encontrado_em, Long id_ciclo, Long id_plano_acao, Long id_colaborador) {
        this.id_problema = id_problema;
        this.titulo = titulo;
        this.descricao = descricao;
        this.peso = peso;
        this.solucao = solucao;
        this.status = status;
        this.origem = origem;
        this.encontrado_em = encontrado_em;
        this.id_ciclo = id_ciclo;
        this.id_plano_acao = id_plano_acao;
        this.id_colaborador = id_colaborador;
    }

    //Geteers e Setters


    public Long getId_problema() {
        return id_problema;
    }

    public void setId_problema(Long id_problema) {
        this.id_problema = id_problema;
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

    public Intensidade getPeso() {
        return peso;
    }

    public void setPeso(Intensidade peso) {
        this.peso = peso;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public StatusProblema getStatus() {
        return status;
    }

    public void setStatus(StatusProblema status) {
        this.status = status;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public Date getEncontrado_em() {
        return encontrado_em;
    }

    public void setEncontrado_em(Date encontrado_em) {
        this.encontrado_em = encontrado_em;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public Long getId_plano_acao() {
        return id_plano_acao;
    }

    public void setId_plano_acao(Long id_plano_acao) {
        this.id_plano_acao = id_plano_acao;
    }

    public Long getId_colaborador() {
        return id_colaborador;
    }

    public void setId_colaborador(Long id_colaborador) {
        this.id_colaborador = id_colaborador;
    }

    public String getNomeCiclo() {
        return nomeCiclo;
    }

    public void setNomeCiclo(String nomeCiclo) {
        this.nomeCiclo = nomeCiclo;
    }

    public String getNomePlanoAcao() {
        return nomePlanoAcao;
    }

    public void setNomePlanoAcao(String nomePlanoAcao) {
        this.nomePlanoAcao = nomePlanoAcao;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }

    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }
}
