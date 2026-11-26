package model;

import java.time.LocalDate;

public class Avaliacao {

    private Integer idAvaliacao;
    private Integer idAgendamento;
    private Integer idCliente;
    private Integer idBarbeiro;
    private Integer nota;
    private String comentario;
    private LocalDate dataAvaliacao;

    public Avaliacao() {
    }

    public Avaliacao(Integer idAvaliacao, Integer idAgendamento, Integer idCliente, Integer idBarbeiro,
                     Integer nota, String comentario, LocalDate dataAvaliacao) {
        this.idAvaliacao = idAvaliacao;
        this.idAgendamento = idAgendamento;
        this.idCliente = idCliente;
        this.idBarbeiro = idBarbeiro;
        this.nota = nota;
        this.comentario = comentario;
        this.dataAvaliacao = dataAvaliacao;
    }

    public Integer getIdAvaliacao() {
        return idAvaliacao;
    }

    public void setIdAvaliacao(Integer idAvaliacao) {
        this.idAvaliacao = idAvaliacao;
    }

    public Integer getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Integer idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdBarbeiro() {
        return idBarbeiro;
    }

    public void setIdBarbeiro(Integer idBarbeiro) {
        this.idBarbeiro = idBarbeiro;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    @Override
    public String toString() {
        return "Avaliacao{id=" + idAvaliacao + ", nota=" + nota + ", comentario='" + comentario + "'}";
    }
}
