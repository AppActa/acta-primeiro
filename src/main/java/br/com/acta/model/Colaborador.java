package br.com.acta.model;

import br.com.acta.enums.Status;

import java.sql.Date;

public class Colaborador {
    private Long id_colaborador;
    private String nome;
    private String sobrenome;
    private Boolean permissao_gestor;
    private Status status;
    private String area;
    private String cargo;
    private Date dt_contratacao;
    private String email;
    private String senha;
    private String telefone;
    private String cpf;
    private Long id_empresa;

    public Colaborador() {}

    public Colaborador(String nome, String sobrenome, Boolean permissao_gestor, Status status, String area, String cargo, Date dt_contratacao, String email, String senha, String telefone, String cpf, Long id_empresa) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.permissao_gestor = permissao_gestor;
        this.status = status;
        this.area = area;
        this.cargo = cargo;
        this.dt_contratacao = dt_contratacao;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cpf = cpf;
        this.id_empresa = id_empresa;
    }

    public Colaborador(Long id_colaborador, String nome, String sobrenome, Boolean permissao_gestor, Status status, String area, String cargo, Date dt_contratacao, String email, String senha, String telefone, String cpf, Long id_empresa) {
        this.id_colaborador = id_colaborador;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.permissao_gestor = permissao_gestor;
        this.status = status;
        this.area = area;
        this.cargo = cargo;
        this.dt_contratacao = dt_contratacao;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cpf = cpf;
        this.id_empresa = id_empresa;
    }
}
