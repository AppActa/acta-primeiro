package br.com.acta.model;

import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Intensidade;

public class LicoesAprendidas {

    //Atributos

    private Long id_licao;
    private String titulo;
    private String area;
    private String aprendizado;
    private String categoria;
    private String descricao;
    private EtapaCiclo fase_origem;
    private Intensidade severidade;
    private Long id_ciclo;
    private String nomeCiclo;

    public LicoesAprendidas() {
    }

    public LicoesAprendidas(String titulo, String area, String aprendizado, String categoria, String descricao, EtapaCiclo fase_origem, Intensidade severidade, Long id_ciclo, String nomeCiclo) {
        this.titulo = titulo;
        this.area = area;
        this.aprendizado = aprendizado;
        this.categoria = categoria;
        this.descricao = descricao;
        this.fase_origem = fase_origem;
        this.severidade = severidade;
        this.id_ciclo = id_ciclo;
        this.nomeCiclo = nomeCiclo;
    }

    public LicoesAprendidas(Long id_licao, String titulo, String area, String aprendizado, String categoria, String descricao, EtapaCiclo fase_origem, Intensidade severidade, Long id_ciclo, String nomeCiclo) {
        this.id_licao = id_licao;
        this.titulo = titulo;
        this.area = area;
        this.aprendizado = aprendizado;
        this.categoria = categoria;
        this.descricao = descricao;
        this.fase_origem = fase_origem;
        this.severidade = severidade;
        this.id_ciclo = id_ciclo;
        this.nomeCiclo = nomeCiclo;
    }

    public Long getId_licao() {
        return id_licao;
    }

    public void setId_licao(Long id_licao) {
        this.id_licao = id_licao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAprendizado() {
        return aprendizado;
    }

    public void setAprendizado(String aprendizado) {
        this.aprendizado = aprendizado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public EtapaCiclo getFase_origem() {
        return fase_origem;
    }

    public void setFase_origem(EtapaCiclo fase_origem) {
        this.fase_origem = fase_origem;
    }

    public Intensidade getSeveridade() {
        return severidade;
    }

    public void setSeveridade(Intensidade severidade) {
        this.severidade = severidade;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public String getNomeCiclo() {
        return nomeCiclo;
    }

    public void setNomeCiclo(String nomeCiclo) {
        this.nomeCiclo = nomeCiclo;
    }
}