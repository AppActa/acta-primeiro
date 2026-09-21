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
}

