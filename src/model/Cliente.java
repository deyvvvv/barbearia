package model;

import java.time.LocalDate;

public class Cliente {

    private Integer idCliente;
    private String nome;
    private String telefone;
    private String email;
    private LocalDate dataCadastro;

    public Cliente() {
    }

    public Cliente(Integer idCliente, String nome, String telefone, String email, LocalDate dataCadastro) {
        this.idCliente = idCliente;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.dataCadastro = dataCadastro;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return "Cliente{id=" + idCliente + ", nome='" + nome + "', telefone='" + telefone
                + "', email='" + email + "', dataCadastro=" + dataCadastro + "}";
    }
}
