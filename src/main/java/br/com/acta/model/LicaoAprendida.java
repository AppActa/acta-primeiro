package br.com.acta.model;

public class LicaoAprendida {

    //Atributos

    private Long id_licao;
    private String titulo;
    private String area;
    private String aprendizado;
    private String categoria;
    private String descricao;
    private String fase_origem;
    private String severidade;
    private Long id_ciclo;

    public LicaoAprendida() {}

    public LicaoAprendida(Long id_ciclo, String severidade, String fase_origem, String descricao, String categoria, String aprendizado, String area, String titulo) {
        this.id_ciclo = id_ciclo;
        this.severidade = severidade;
        this.fase_origem = fase_origem;
        this.descricao = descricao;
        this.categoria = categoria;
        this.aprendizado = aprendizado;
        this.area = area;
        this.titulo = titulo;
    }

    public LicaoAprendida(Long id_licao, String titulo, String area, String aprendizado, String categoria, String descricao, String fase_origem, String severidade, Long id_ciclo) {
        this.id_licao = id_licao;
        this.titulo = titulo;
        this.area = area;
        this.aprendizado = aprendizado;
        this.categoria = categoria;
        this.descricao = descricao;
        this.fase_origem = fase_origem;
        this.severidade = severidade;
        this.id_ciclo = id_ciclo;
    }

    public Long getId_licao() {
        return id_licao;
    }

    public void setId_licao(Long id_licao) {
        this.id_licao = id_licao;
    }

    public Long getId_ciclo() {
        return id_ciclo;
    }

    public void setId_ciclo(Long id_ciclo) {
        this.id_ciclo = id_ciclo;
    }

    public String getSeveridade() {
        return severidade;
    }

    public void setSeveridade(String severidade) {
        this.severidade = severidade;
    }

    public String getFase_origem() {
        return fase_origem;
    }

    public void setFase_origem(String fase_origem) {
        this.fase_origem = fase_origem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getAprendizado() {
        return aprendizado;
    }

    public void setAprendizado(String aprendizado) {
        this.aprendizado = aprendizado;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
