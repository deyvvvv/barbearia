package model;

import java.time.LocalDate;

public class Barbeiro {

    private Integer idBarbeiro;
    private String nome;
    private String telefone;
    private String email;
    private String especialidade;
    private LocalDate dataContratacao;

    public Barbeiro() {
    }

    public Barbeiro(Integer idBarbeiro, String nome, String telefone, String email,
                    String especialidade, LocalDate dataContratacao) {
        this.idBarbeiro = idBarbeiro;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.especialidade = especialidade;
        this.dataContratacao = dataContratacao;
    }

    public Integer getIdBarbeiro() {
        return idBarbeiro;
    }

    public void setIdBarbeiro(Integer idBarbeiro) {
        this.idBarbeiro = idBarbeiro;
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

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public LocalDate getDataContratacao() {
        return dataContratacao;
    }

    public void setDataContratacao(LocalDate dataContratacao) {
        this.dataContratacao = dataContratacao;
    }

    @Override
    public String toString() {
        return "Barbeiro{id=" + idBarbeiro + ", nome='" + nome + "', especialidade='" + especialidade + "'}";
    }
}
