package service;

import dao.AgendamentoDAO;
import dao.OperacaoBancoDAO;
import model.Agendamento;
import model.ResultadoOperacao;
import util.InputHelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Mostra na aplicacao os objetos avancados do PostgreSQL:
 * Procedure com transacao, Functions e Triggers.
 */
public class OperacaoBancoService {

    private final OperacaoBancoDAO operacaoBancoDAO;
    private final AgendamentoDAO agendamentoDAO;
    private final InputHelper input;

    public OperacaoBancoService(Scanner scanner) {
        this.operacaoBancoDAO = new OperacaoBancoDAO();
        this.agendamentoDAO = new AgendamentoDAO();
        this.input = new InputHelper(scanner);
    }

    public void exibirMenu() {
        int op;
        do {
            System.out.println("\n--- PROCEDURE / FUNCTIONS / TRIGGERS ---");
            System.out.println("1 - Finalizar atendimento (Procedure + Function + Trigger)");
            System.out.println("2 - Faturamento por barbeiro (Function tabela)");
            System.out.println("3 - Preco do servico (Function escalar)");
            System.out.println("4 - Auditoria de pagamentos (Trigger)");
            System.out.println("0 - Voltar");
            op = input.lerIntOpcao("Escolha: ");

            switch (op) {
                case 1 -> finalizar();
                case 2 -> faturamento();
                case 3 -> preco();
                case 4 -> auditoria();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void finalizar() {
        System.out.println("\n=== FINALIZAR ATENDIMENTO (PROCEDURE) ===");

        List<Agendamento> elegiveis = agendamentoDAO.listarTodos().stream()
                .filter(a -> "AGENDADO".equals(a.getStatus()) || "CONFIRMADO".equals(a.getStatus()))
                .toList();

        if (elegiveis.isEmpty()) {
            System.out.println("Nenhum agendamento aguardando finalizacao.");
            return;
        }

        System.out.println("Agendamentos que podem ser finalizados:");
        for (Agendamento a : elegiveis) {
            System.out.printf("ID %d | ClienteID %d | BarbeiroID %d | %s | %s%n",
                    a.getIdAgendamento(), a.getIdCliente(), a.getIdBarbeiro(),
                    a.getDataHora(), a.getStatus());
        }

        int id = input.lerInt("ID do agendamento: ");
        String forma = input.lerString(
                "Forma (DINHEIRO/CARTAO_CREDITO/CARTAO_DEBITO/PIX): ").toUpperCase(Locale.ROOT);

        System.out.println("SQL: CALL finalizar_atendimento(" + id + ", '" + forma + "', ?, ?)");
        ResultadoOperacao resultado = operacaoBancoDAO.finalizarAtendimento(id, forma);

        if (resultado.sucesso()) {
            System.out.println("ATENDIMENTO FINALIZADO");
        } else {
            System.out.println("ROLLBACK / ERRO NA PROCEDURE");
        }
        System.out.println("Resultado: " + resultado.mensagem());
    }

    private void faturamento() {
        System.out.println("\n=== FATURAMENTO POR BARBEIRO (FUNCTION TABELA) ===");
        String idStr = input.lerString("ID do barbeiro (Enter para todos): ");

        Integer id = null;
        if (!idStr.isEmpty()) {
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                System.out.println("ID invalido. Mostrando todos.");
            }
        }

        System.out.println("SQL: SELECT * FROM fn_faturamento_por_barbeiro(" + id + ")");
        List<String[]> linhas = operacaoBancoDAO.faturamentoPorBarbeiro(id);
        if (linhas.isEmpty()) {
            System.out.println("Nenhum barbeiro encontrado.");
            return;
        }

        System.out.printf("%-5s | %-18s | %-14s | %s%n", "ID", "Barbeiro", "Atendimentos", "Faturamento");
        System.out.println("-".repeat(65));
        for (String[] linha : linhas) {
            System.out.printf("%-5s | %-18s | %-14s | R$ %s%n",
                    linha[0], linha[1], linha[2], linha[3]);
        }
    }

    private void preco() {
        System.out.println("\n=== PRECO DO SERVICO (FUNCTION ESCALAR) ===");
        int id = input.lerInt("ID do servico: ");
        System.out.println("SQL: SELECT fn_preco_servico(" + id + ")");

        BigDecimal preco = operacaoBancoDAO.precoServico(id);
        if (preco == null) {
            System.out.println("Servico nao encontrado ou sem preco.");
        } else {
            System.out.println("Preco: R$ " + preco);
        }
    }

    private void auditoria() {
        System.out.println("\n=== AUDITORIA DE PAGAMENTOS (TRIGGER) ===");
        System.out.println("SQL: SELECT * FROM AuditoriaPagamento ORDER BY id_auditoria DESC");

        List<String[]> linhas = operacaoBancoDAO.listarAuditoriaPagamentos();
        if (linhas.isEmpty()) {
            System.out.println("Nenhuma alteracao de pagamento registrada.");
            return;
        }

        System.out.printf("%-5s | %-8s | %-12s | %-12s | %s%n",
                "ID", "Pagamento", "Anterior", "Novo", "Quando");
        System.out.println("-".repeat(85));
        for (String[] linha : linhas) {
            System.out.printf("%-5s | %-8s | %-12s | %-12s | %s%n",
                    linha[0], linha[1], linha[2], linha[3], linha[4]);
        }
    }
}
