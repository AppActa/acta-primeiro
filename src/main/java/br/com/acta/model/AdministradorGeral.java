package br.com.acta.model;



public class AdministradorGeral {
    //Atributos
    private Integer adm_geral_id;
    private String nome;
    private String senha;
    private String email;
    private String telefone;

    //construtores

    public AdministradorGeral() {}

    public AdministradorGeral(String nome, String senha, String email, String telefone) {
        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.telefone = telefone;
    }

    //construtor com id para os metodos do doPost (exceto o inserir)
    public AdministradorGeral(Integer adm_geral_id, String telefone, String email, String senha, String nome) {
        this.adm_geral_id = adm_geral_id;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }


    //Getters e Setters

    public Integer getAdm_geral_id() {
        return adm_geral_id;
    }

    public void setAdm_geral_id(Integer adm_geral_id) {
        this.adm_geral_id = adm_geral_id;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
