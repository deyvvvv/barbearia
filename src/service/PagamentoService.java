package service;

import dao.PagamentoDAO;
import model.Pagamento;
import util.InputHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class PagamentoService {

    private final PagamentoDAO pagamentoDAO;
    private final Scanner scanner;
    private final InputHelper input;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PagamentoService(Scanner scanner) {
        this.pagamentoDAO = new PagamentoDAO();
        this.scanner = scanner;
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n=== CADASTRAR PAGAMENTO ===");
            int idAgendamento = input.lerInt("ID do agendamento: ");
            double valor = input.lerDouble("Valor (R$): ");

            System.out.print("Data (dd/MM/yyyy) ou Enter para hoje: ");
            String dataStr = scanner.nextLine().trim();
            LocalDate data;
            try {
                data = dataStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dataStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Data invalida. Use dd/MM/yyyy (ex: 20/07/2026). Usando data atual.");
                data = LocalDate.now();
            }

            System.out.println("Formas: DINHEIRO | CARTAO_CREDITO | CARTAO_DEBITO | PIX");
            String forma = input.lerString("Forma de pagamento: ");

            System.out.println("Status: PENDENTE | PAGO | ESTORNADO");
            String status = input.lerString("Status: ");

            Pagamento pagamento = new Pagamento(null, idAgendamento, valor, data,
                    forma.toUpperCase(Locale.ROOT), status.toUpperCase(Locale.ROOT));
            pagamentoDAO.inserir(pagamento);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n=== EDITAR PAGAMENTO ===");
            int id = input.lerInt("ID do pagamento: ");
            Pagamento pagamento = pagamentoDAO.buscarPorId(id);
            if (pagamento == null) {
                System.out.println("Pagamento nao encontrado.");
                return;
            }
            System.out.printf("Pagamento atual: R$%.2f | %s | %s%n",
                    pagamento.getValor(), pagamento.getFormaPagamento(), pagamento.getStatusPagamento());
            System.out.println("(Deixe em branco para manter)");

            String idAg = input.lerString("Novo ID agendamento: ");
            if (!idAg.isEmpty()) {
                try { pagamento.setIdAgendamento(Integer.parseInt(idAg)); }
                catch (NumberFormatException e) { System.out.println("ID invalido."); }
            }

            String valStr = input.lerString("Novo valor: ");
            if (!valStr.isEmpty()) {
                try { pagamento.setValor(Double.parseDouble(valStr.replace(",", "."))); }
                catch (NumberFormatException e) { System.out.println("Valor invalido."); }
            }

            System.out.print("Nova data (dd/MM/yyyy): ");
            String dataStr = scanner.nextLine().trim();
            if (!dataStr.isEmpty()) {
                try { pagamento.setDataPagamento(LocalDate.parse(dataStr, DATE_FORMATTER)); }
                catch (DateTimeParseException e) { System.out.println("Data invalida. Use dd/MM/yyyy (ex: 20/07/2026)."); }
            }

            String forma = input.lerString("Nova forma (DINHEIRO/CARTAO_CREDITO/CARTAO_DEBITO/PIX): ");
            if (!forma.isEmpty()) pagamento.setFormaPagamento(forma.toUpperCase(Locale.ROOT));

            String status = input.lerString("Novo status (PENDENTE/PAGO/ESTORNADO): ");
            if (!status.isEmpty()) pagamento.setStatusPagamento(status.toUpperCase(Locale.ROOT));

            pagamentoDAO.atualizar(pagamento);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n=== EXCLUIR PAGAMENTO ===");
            int id = input.lerInt("ID do pagamento: ");
            Pagamento pagamento = pagamentoDAO.buscarPorId(id);
            if (pagamento == null) {
                System.out.println("Pagamento nao encontrado.");
                return;
            }
            System.out.println("Excluir pagamento de R$" + pagamento.getValor() + "?");
            String c = input.lerString("Digite SIM para confirmar: ");
            if (c.equalsIgnoreCase("SIM")) {
                pagamentoDAO.excluir(id);
            } else {
                System.out.println("Operacao cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n=== BUSCAR PAGAMENTO ===");
            int id = input.lerInt("ID do pagamento: ");
            Pagamento pagamento = pagamentoDAO.buscarPorId(id);
            if (pagamento != null) {
                System.out.printf("Agendamento %d | R$%.2f | %s | %s | %s%n",
                        pagamento.getIdAgendamento(), pagamento.getValor(),
                        pagamento.getFormaPagamento(), pagamento.getStatusPagamento(),
                        pagamento.getDataPagamento());
            } else {
                System.out.println("Pagamento nao encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n=== LISTA DE PAGAMENTOS ===");
            List<Pagamento> pagamentos = pagamentoDAO.listarTodos();
            if (pagamentos.isEmpty()) {
                System.out.println("Nenhum pagamento cadastrado.");
                return;
            }
            System.out.printf("%-4s | %-12s | %-10s | %-18s | %-12s | %s%n",
                    "ID", "Agendamento", "Valor", "Forma", "Status", "Data");
            System.out.println("-".repeat(80));
            for (Pagamento p : pagamentos) {
                System.out.printf("%-4d | %-12d | R$%7.2f | %-18s | %-12s | %s%n",
                        p.getIdPagamento(), p.getIdAgendamento(), p.getValor(),
                        p.getFormaPagamento(), p.getStatusPagamento(), p.getDataPagamento());
            }
            System.out.println("Total: " + pagamentos.size() + " pagamento(s)");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}