package br.com.acta.model;

public class PlanoAcao {

    //Atributos
    private Long id_plano_acao;
    private String nome;
    private String descricao;
    private String status;
    private String prioridade;
    private Long id_ciclo;
    private Long id_criador;

    //Construtores

    public PlanoAcao() {}

    public PlanoAcao(String nome, String descricao, String status, String prioridade, Long id_ciclo, Long id_criador) {
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.id_ciclo = id_ciclo;
        this.id_criador = id_criador;
    }

    public PlanoAcao(Long id_plano_acao, String nome, String descricao, String status, String prioridade, Long id_ciclo, Long id_criador) {
        this.id_plano_acao = id_plano_acao;
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.id_ciclo = id_ciclo;
        this.id_criador = id_criador;
    }

    public Long getId_plano_acao() {
        return id_plano_acao;
    }

    public void setId_plano_acao(Long id_plano_acao) {
        this.id_plano_acao = id_plano_acao;
    }

    public Long getId_criador() {
        return id_criador;
    }

    public void setId_criador(Long id_criador) {
        this.id_criador = id_criador;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
