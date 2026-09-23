package br.com.acta.model;

public class Empresa {

    //Atributos
    private Long id_empresa;
    private String nome;
    private String setor;
    private String cnpj;
    private Status status;
    private String tamanho;

    public Empresa() {}

    public Empresa(String nome, String setor, String cnpj, Status status, String tamanho) {
        this.nome = nome;
        this.setor = setor;
        this.cnpj = cnpj;
        this.status = status;
        this.tamanho = tamanho;
    }

    public Empresa(Long id_empresa, String nome, String setor, String cnpj, Status status, String tamanho) {
        this.id_empresa = id_empresa;
        this.nome = nome;
        this.setor = setor;
        this.cnpj = cnpj;
        this.status = status;
        this.tamanho = tamanho;
    }

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long id_empresa) {
        this.id_empresa = id_empresa;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }
}

