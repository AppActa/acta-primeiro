package br.com.acta.model;

public class Endereco {
    private Long id_endereco;
    private String rua;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String numero;
    private String complemento;
    private String unidade;
    private Long id_empresa;
    private String nomeEmpresa;

    public Endereco() {
    }

    public Endereco(String rua, String bairro, String cidade, String estado, String cep, String numero, String complemento, String unidade, Long id_empresa) {
        this.rua = rua;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
        this.unidade = unidade;
        this.id_empresa = id_empresa;
    }

    public Endereco(Long id_endereco, String rua, String bairro, String cidade, String estado, String cep, String numero, String complemento, String unidade, Long id_empresa) {
        this.id_endereco = id_endereco;
        this.rua = rua;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
        this.unidade = unidade;
        this.id_empresa = id_empresa;
    }

    public Long getId_endereco() {
        return id_endereco;
    }

    public void setId_endereco(Long id_endereco) {
        this.id_endereco = id_endereco;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long id_empresa) {
        this.id_empresa = id_empresa;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }
}