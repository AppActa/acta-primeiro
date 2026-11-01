package br.com.acta.model;

import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;

/** Representa um plano de ação ligado a um ciclo e ao seu criador. */
public class PlanoAcao {

    // Dados do plano, prioridade e andamento da execução.
    private Long id_plano_acao;
    private String nome;
    private String descricao;
    private Situacao status;
    private Intensidade prioridade;

    // IDs do ciclo e do colaborador que criou o plano.
    private Long id_ciclo;
    private Long id_criador;

    // Nomes retornados pelas consultas para exibir os vínculos.
    private String nome_ciclo;
    private String nome_criador;

    /** Cria um plano vazio para preenchimento posterior. */
    public PlanoAcao() {}

    /** Monta os dados do plano sem um ID definido. */
    public PlanoAcao(
            String nome, String descricao, Situacao status, Intensidade prioridade, Long id_ciclo,
            Long id_criador) {
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.id_ciclo = id_ciclo;
        this.id_criador = id_criador;
    }

    /** Monta um plano com ID já conhecido. */
    public PlanoAcao(
            Long id_plano_acao, String nome, String descricao, Situacao status, Intensidade prioridade,
            Long id_ciclo, Long id_criador) {
        this.id_plano_acao = id_plano_acao;
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.id_ciclo = id_ciclo;
        this.id_criador = id_criador;
    }

    // Acesso e atualização dos dados do modelo.

    public Long getId_plano_acao() {
        return id_plano_acao;
    }

    public void setId_plano_acao(Long id_plano_acao) {
        this.id_plano_acao = id_plano_acao;
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

    public Situacao getStatus() {
        return status;
    }

    public void setStatus(Situacao status) {
        this.status = status;
    }

    public Intensidade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Intensidade prioridade) {
        this.prioridade = prioridade;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public Long getId_criador() {
        return id_criador;
    }

    public void setId_criador(Long id_criador) {
        this.id_criador = id_criador;
    }

    public String getNome_ciclo() {
        return nome_ciclo;
    }

    public void setNome_ciclo(String nome_ciclo) {
        this.nome_ciclo = nome_ciclo;
    }

    public String getNome_criador() {
        return nome_criador;
    }

    public void setNome_criador(String nome_criador) {
        this.nome_criador = nome_criador;
    }
}
