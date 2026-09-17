package br.com.acta.model;



public class AdministradorGeral {
    //Atributos
    private Long id_adm_geral;
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
    public AdministradorGeral(Long id_adm_geral, String telefone, String email, String senha, String nome) {
        this.id_adm_geral = id_adm_geral;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }


    //Getters e Setters

    public Long getAdm_geral_id() {
        return id_adm_geral;
    }

    public void setAdm_geral_id(Long id_adm_geral) {
        this.id_adm_geral = id_adm_geral;
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