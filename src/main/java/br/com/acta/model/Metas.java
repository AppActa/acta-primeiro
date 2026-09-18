package br.com.acta.model;

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

    public Metas(long id_plano_acao, Long id_ciclo, OffsetDateTime criando_em, StatusMeta status, Date prazo, String prioridade, String objetivo, String descricao_meta, String meta) {
        this.id_plano_acao = id_plano_acao;
        this.id_ciclo = id_ciclo;
        this.criando_em = criando_em;
        this.status = status;
        this.prazo = prazo;
        this.prioridade = prioridade;
        this.objetivo = objetivo;
        this.descricao_meta = descricao_meta;
        this.meta = meta;
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
}
