package br.com.acta.model;

import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Situacao;

import java.sql.Date;
import java.time.OffsetDateTime;

public class Ciclo {

    private Long id_ciclo;
    private String nome;
    private String descricao;
    private EtapaCiclo etapa_atual;
    private Date dt_inicio;
    private Date dt_fim;
    private Situacao status;
    private OffsetDateTime criado_em;
    private Long id_empresa;
    private Long id_responsavel;

    public Ciclo() {}

    public Ciclo(String nome, String descricao, EtapaCiclo etapa_atual, Date dt_inicio, Date dt_fim, Situacao status, OffsetDateTime criado_em, Long id_empresa, Long id_responsavel) {
        this.nome = nome;
        this.descricao = descricao;
        this.etapa_atual = etapa_atual;
        this.dt_inicio = dt_inicio;
        this.dt_fim = dt_fim;
        this.status = status;
        this.criado_em = criado_em;
        this.id_empresa = id_empresa;
        this.id_responsavel = id_responsavel;
    }

    public Ciclo(Long id_ciclo, String nome, String descricao, EtapaCiclo etapa_atual, Date dt_inicio, Date dt_fim, Situacao status, OffsetDateTime criado_em, Long id_responsavel, Long id_empresa) {
        this.id_ciclo = id_ciclo;
        this.nome = nome;
        this.descricao = descricao;
        this.etapa_atual = etapa_atual;
        this.dt_inicio = dt_inicio;
        this.dt_fim = dt_fim;
        this.status = status;
        this.criado_em = criado_em;
        this.id_responsavel = id_responsavel;
        this.id_empresa = id_empresa;
    }

    //Getters e Setters


    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public EtapaCiclo getEtapa_atual() {
        return etapa_atual;
    }

    public void setEtapa_atual(EtapaCiclo etapa_atual) {
        this.etapa_atual = etapa_atual;
    }

    public Date getDt_inicio() {
        return dt_inicio;
    }

    public void setDt_inicio(Date dt_inicio) {
        this.dt_inicio = dt_inicio;
    }

    public Date getDt_fim() {
        return dt_fim;
    }

    public void setDt_fim(Date dt_fim) {
        this.dt_fim = dt_fim;
    }

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    public OffsetDateTime getCriado_em() {
        return criado_em;
    }

    public void setCriado_em(OffsetDateTime criado_em) {
        this.criado_em = criado_em;
    }

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long id_empresa) {
        this.id_empresa = id_empresa;
    }

    public Long getId_responsavel() {
        return id_responsavel;
    }

    public void setId_responsavel(Long id_responsavel) {
        this.id_responsavel = id_responsavel;
    }
}
