package br.com.acta.model;

import java.sql.Date;

public class Colaborador {
    private Long id_colaborador;
    private String nome;
    private String sobrenome;
    private Boolean permissao_gestor;
    private String area;
    private String cargo;
    private Date dt_contratacao;
    private String email;
    private String senha;
    private String telefone;
    private String cpf;
    private Long id_empresa;

    public Colaborador() {
    }

    public Colaborador(String nome, String sobrenome, Boolean permissao_gestor, String area, String cargo, Date dt_contratacao, String email, String senha, String telefone, String cpf, Long id_empresa) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.permissao_gestor = permissao_gestor;
        this.area = area;
        this.cargo = cargo;
        this.dt_contratacao = dt_contratacao;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cpf = cpf;
        this.id_empresa = id_empresa;
    }

    public Colaborador(Long id_colaborador, String nome, String sobrenome, Boolean permissao_gestor, String area, String cargo, Date dt_contratacao, String email, String senha, String telefone, String cpf, Long id_empresa) {
        this.id_colaborador = id_colaborador;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.permissao_gestor = permissao_gestor;
        this.area = area;
        this.cargo = cargo;
        this.dt_contratacao = dt_contratacao;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cpf = cpf;
        this.id_empresa = id_empresa;
    }

    public Long getId_colaborador() {
        return id_colaborador;
    }

    public void setId_colaborador(Long id_colaborador) {
        this.id_colaborador = id_colaborador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public Boolean getPermissao_gestor() {
        return permissao_gestor;
    }

    public void setPermissao_gestor(Boolean permissao_gestor) {
        this.permissao_gestor = permissao_gestor;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Date getDt_contratacao() {
        return dt_contratacao;
    }

    public void setDt_contratacao(Date dt_contratacao) {
        this.dt_contratacao = dt_contratacao;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long id_empresa) {
        this.id_empresa = id_empresa;
    }
}
