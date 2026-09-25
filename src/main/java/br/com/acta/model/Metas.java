package br.com.acta.model;

import br.com.acta.enums.StatusMeta;

import java.sql.Date;
import java.time.OffsetDateTime;

public class Metas {

    private Long id_meta;
    private String meta;
    private String descricao_meta;
    private String objetivo;
    private String prioridade;
    private Date prazo;
    private StatusMeta status;
    private OffsetDateTime criando_em;
    private Long id_ciclo;
    private long id_plano_acao;

    public Metas() {}

    public Metas(String meta, String descricao_meta, String objetivo, String prioridade, Date prazo, StatusMeta status, OffsetDateTime criando_em, Long id_ciclo, long id_plano_acao) {
        this.meta = meta;
        this.descricao_meta = descricao_meta;
        this.objetivo = objetivo;
        this.prioridade = prioridade;
        this.prazo = prazo;
        this.status = status;
        this.criando_em = criando_em;
        this.id_ciclo = id_ciclo;
        this.id_plano_acao = id_plano_acao;
    }

    public Metas(Long id_meta, String meta, String descricao_meta, String objetivo, String prioridade, Date prazo, StatusMeta status, OffsetDateTime criando_em, Long id_ciclo, long id_plano_acao) {
        this.id_meta = id_meta;
        this.meta = meta;
        this.descricao_meta = descricao_meta;
        this.objetivo = objetivo;
        this.prioridade = prioridade;
        this.prazo = prazo;
        this.status = status;
        this.criando_em = criando_em;
        this.id_ciclo = id_ciclo;
        this.id_plano_acao = id_plano_acao;
    }

    public Long getId_meta() {
        return id_meta;
    }

    public void setId_meta(Long id_meta) {
        this.id_meta = id_meta;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getDescricao_meta() {
        return descricao_meta;
    }

    public void setDescricao_meta(String descricao_meta) {
        this.descricao_meta = descricao_meta;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public Date getPrazo() {
        return prazo;
    }

    public void setPrazo(Date prazo) {
        this.prazo = prazo;
    }

    public StatusMeta getStatus() {
        return status;
    }

    public void setStatus(StatusMeta status) {
        this.status = status;
    }

    public OffsetDateTime getCriando_em() {
        return criando_em;
    }

    public void setCriando_em(OffsetDateTime criando_em) {
        this.criando_em = criando_em;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public long getId_plano_acao() {
        return id_plano_acao;
    }

    public void setId_plano_acao(long id_plano_acao) {
        this.id_plano_acao = id_plano_acao;
    }
}
