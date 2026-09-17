package br.com.acta.model;

public class Empresa {

    //Atributos
    private Long empresa_id;
    private String nome;
    private String setor;
    private String cnpj;
    private String status;
    private String tamanho;

    //Contrutores

    public Empresa() {}

    public Empresa(String nome, String setor, String cnpj, String status, String tamanho) {
        this.nome = nome;
        this.setor = setor;
        this.cnpj = cnpj;
        this.status = status;
        this.tamanho = tamanho;
    }

    public Empresa(Long empresa_id, String nome, String setor, String cnpj, String status, String tamanho) {
        this.empresa_id = empresa_id;
        this.nome = nome;
        this.setor = setor;
        this.cnpj = cnpj;
        this.status = status;
        this.tamanho = tamanho;
    }

    //Getters e Setters


    public Long getEmpresa_id() {
        return empresa_id;
    }

    public void setEmpresa_id(Long empresa_id) {
        this.empresa_id = empresa_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }
}
