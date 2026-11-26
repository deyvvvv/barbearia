package model;

import java.time.LocalDate;

public class Pagamento {

    private Integer idPagamento;
    private Integer idAgendamento;
    private Double valor;
    private LocalDate dataPagamento;
    private String formaPagamento;
    private String statusPagamento;

    public Pagamento() {
    }

    public Pagamento(Integer idPagamento, Integer idAgendamento, Double valor, LocalDate dataPagamento,
                     String formaPagamento, String statusPagamento) {
        this.idPagamento = idPagamento;
        this.idAgendamento = idAgendamento;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.formaPagamento = formaPagamento;
        this.statusPagamento = statusPagamento;
    }

    public Integer getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Integer idPagamento) {
        this.idPagamento = idPagamento;
    }

    public Integer getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Integer idAgendamento) {
        this.idAgendamento = idAgendamento;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    @Override
    public String toString() {
        return "Pagamento{id=" + idPagamento + ", valor=" + valor + ", forma='" + formaPagamento
                + "', status='" + statusPagamento + "'}";
    }
}
