package model;

import java.time.LocalDateTime;

public class Agendamento {

    private Integer idAgendamento;
    private Integer idCliente;
    private Integer idBarbeiro;
    private Integer idServico;
    private LocalDateTime dataHora;
    private String status;
    private String observacoes;

    private String nomeCliente;
    private String nomeBarbeiro;
    private String nomeServico;

    public Agendamento() {
    }

    public Agendamento(Integer idAgendamento, Integer idCliente, Integer idBarbeiro, Integer idServico,
                       LocalDateTime dataHora, String status, String observacoes) {
        this.idAgendamento = idAgendamento;
        this.idCliente = idCliente;
        this.idBarbeiro = idBarbeiro;
        this.idServico = idServico;
        this.dataHora = dataHora;
        this.status = status;
        this.observacoes = observacoes;
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

    public Integer getIdServico() {
        return idServico;
    }

    public void setIdServico(Integer idServico) {
        this.idServico = idServico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomeBarbeiro() {
        return nomeBarbeiro;
    }

    public void setNomeBarbeiro(String nomeBarbeiro) {
        this.nomeBarbeiro = nomeBarbeiro;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) {
        this.nomeServico = nomeServico;
    }

    @Override
    public String toString() {
        return "Agendamento{id=" + idAgendamento + ", dataHora=" + dataHora + ", status='" + status + "'}";
    }
}
