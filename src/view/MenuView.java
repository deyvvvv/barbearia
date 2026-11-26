package view;

import dao.RelatorioDAO;
import service.*;

import java.util.List;
import java.util.Scanner;

public class MenuView {

    private final Scanner scanner;
    private final ClienteService clienteService;
    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;
    private final PagamentoService pagamentoService;
    private final AvaliacaoService avaliacaoService;
    private final OperacaoBancoService operacaoBancoService;
    private final RelatorioDAO relatorioDAO;

    public MenuView() {
        this.scanner = new Scanner(System.in);
        this.clienteService = new ClienteService(scanner);
        this.barbeiroService = new BarbeiroService(scanner);
        this.servicoService = new ServicoService(scanner);
        this.agendamentoService = new AgendamentoService(scanner);
        this.pagamentoService = new PagamentoService(scanner);
        this.avaliacaoService = new AvaliacaoService(scanner);
        this.operacaoBancoService = new OperacaoBancoService(scanner);
        this.relatorioDAO = new RelatorioDAO();
    }

    public void exibirMenuPrincipal() {
        int opcao;
        do {
            System.out.println("\n===============================");
            System.out.println("   \u2712  BARBEARIA DO BRAZ  \u2712");
            System.out.println("===============================");
            System.out.println("1 - Clientes");
            System.out.println("2 - Barbeiros");
            System.out.println("3 - Servicos");
            System.out.println("4 - Agendamentos");
            System.out.println("5 - Pagamentos");
            System.out.println("6 - Avaliacoes");
            System.out.println("7 - Relatórios");
            System.out.println("8 - Ver horarios disponiveis");
            System.out.println("9 - Procedure / Functions / Triggers");
            System.out.println("10 - Como usar / comandos");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            try {
                String linha = scanner.nextLine().trim();
                opcao = linha.isEmpty() ? -1 : Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1 -> menuClientes();
                case 2 -> menuBarbeiros();
                case 3 -> menuServicos();
                case 4 -> menuAgendamentos();
                case 5 -> menuPagamentos();
                case 6 -> menuAvaliacoes();
                case 7 -> menuRelatorios();
                case 8 -> agendamentoService.verHorariosDisponiveis();
                case 9 -> operacaoBancoService.exibirMenu();
                case 10 -> exibirComoUsar();
                case 0 -> System.out.println("Encerrando...");
                default -> System.out.println("Opcao invalida!");
            }
        } while (opcao != 0);
    }

    private void exibirComoUsar() {
        System.out.println("\n======== COMANDOS PARA O SEMINARIO ========");
        System.out.println("Deixe esta tela visivel e execute os comandos no terminal.");
        System.out.println();
        System.out.println("1) Abrir o sistema:");
        System.out.println("   .\\executar.ps1");
        System.out.println("2) Recriar o banco e rodar todos os testes:");
        System.out.println("   .\\scripts\\smoke.ps1");
        System.out.println("3) Entrar no psql:");
        System.out.println("   psql -U postgres -d barbearia");
        System.out.println("4) Consultas rapidas no psql:");
        System.out.println("   SELECT * FROM fn_faturamento_por_barbeiro(NULL);");
        System.out.println("   SELECT * FROM AuditoriaPagamento;");
        System.out.println("   CALL finalizar_atendimento(1, 'PIX', NULL, NULL);");
        System.out.println("5) Demonstracao no menu do sistema:");
        System.out.println("   9 > 1 > ID do agendamento > PIX  (finaliza / rollback)");
        System.out.println("   9 > 2 > ID do barbeiro            (faturamento)");
        System.out.println("   9 > 3 > ID do servico             (preco)");
        System.out.println("   9 > 4                              (auditoria)");
        System.out.println("6) Sair do sistema: 0");
        System.out.println("=========================================");
    }

    private int lerOpcao() {
        try {
            String linha = scanner.nextLine().trim();
            if (linha.isEmpty()) return -1;
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void menuClientes() {
        int op;
        do {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> clienteService.cadastrar();
                case 2 -> clienteService.editar();
                case 3 -> clienteService.excluir();
                case 4 -> clienteService.buscar();
                case 5 -> clienteService.listar();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuBarbeiros() {
        int op;
        do {
            System.out.println("\n--- BARBEIROS ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> barbeiroService.cadastrar();
                case 2 -> barbeiroService.editar();
                case 3 -> barbeiroService.excluir();
                case 4 -> barbeiroService.buscar();
                case 5 -> barbeiroService.listar();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuServicos() {
        int op;
        do {
            System.out.println("\n--- SERVICOS ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> servicoService.cadastrar();
                case 2 -> servicoService.editar();
                case 3 -> servicoService.excluir();
                case 4 -> servicoService.buscar();
                case 5 -> servicoService.listar();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuAgendamentos() {
        int op;
        do {
            System.out.println("\n--- AGENDAMENTOS ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("6 - Historico (com JOIN)");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> agendamentoService.cadastrar();
                case 2 -> agendamentoService.editar();
                case 3 -> agendamentoService.excluir();
                case 4 -> agendamentoService.buscar();
                case 5 -> agendamentoService.listar();
                case 6 -> agendamentoService.historicoComNomes();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuPagamentos() {
        int op;
        do {
            System.out.println("\n--- PAGAMENTOS ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> pagamentoService.cadastrar();
                case 2 -> pagamentoService.editar();
                case 3 -> pagamentoService.excluir();
                case 4 -> pagamentoService.buscar();
                case 5 -> pagamentoService.listar();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuAvaliacoes() {
        int op;
        do {
            System.out.println("\n--- AVALIACOES ---");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Editar");
            System.out.println("3 - Excluir");
            System.out.println("4 - Buscar");
            System.out.println("5 - Listar");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> avaliacaoService.cadastrar();
                case 2 -> avaliacaoService.editar();
                case 3 -> avaliacaoService.excluir();
                case 4 -> avaliacaoService.buscar();
                case 5 -> avaliacaoService.listar();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void menuRelatorios() {
        int op;
        do {
            System.out.println("\n--- RELATORIOS ---");
            System.out.println("1 - Historico de agendamentos (JOIN)");
            System.out.println("2 - Total arrecadado por barbeiro (GROUP BY)");
            System.out.println("3 - Quantidade de atendimentos por barbeiro (GROUP BY)");
            System.out.println("4 - Media das avaliacoes por barbeiro (GROUP BY)");
            System.out.println("5 - Clientes que mais agendaram (GROUP BY)");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            op = lerOpcao();
            switch (op) {
                case 1 -> relatorioHistorico();
                case 2 -> relatorioTotalArrecadado();
                case 3 -> relatorioAtendimentos();
                case 4 -> relatorioMediaAvaliacoes();
                case 5 -> relatorioClientesTop();
                case 0 -> {}
                default -> System.out.println("Opcao invalida!");
            }
        } while (op != 0);
    }

    private void relatorioHistorico() {
        System.out.println("\n--- HISTORICO DE AGENDAMENTOS (JOIN) ---");
        List<String[]> dados = relatorioDAO.historicoAgendamentos();
        if (dados.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String[] r : dados) {
            System.out.printf("ID %s | Cliente: %s | Barbeiro: %s | Servico: %s | %s | %s%n",
                    r[0], r[1], r[2], r[3], r[4], r[5]);
        }
    }

    private void relatorioTotalArrecadado() {
        System.out.println("\n--- TOTAL ARRECADADO POR BARBEIRO (GROUP BY) ---");
        List<String[]> dados = relatorioDAO.totalArrecadadoPorBarbeiro();
        if (dados.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String[] r : dados) {
            System.out.printf("ID %s | %s | Atendimentos: %s | Total Arrecadado: R$%s%n",
                    r[0], r[1], r[2], r[3]);
        }
    }

    private void relatorioAtendimentos() {
        System.out.println("\n--- QUANTIDADE DE ATENDIMENTOS POR BARBEIRO (GROUP BY) ---");
        List<String[]> dados = relatorioDAO.atendimentosPorBarbeiro();
        if (dados.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String[] r : dados) {
            System.out.printf("ID %s | %s | Atendimentos: %s%n", r[0], r[1], r[2]);
        }
    }

    private void relatorioMediaAvaliacoes() {
        System.out.println("\n--- MEDIA DAS AVALIACOES POR BARBEIRO (GROUP BY) ---");
        List<String[]> dados = relatorioDAO.mediaAvaliacoesPorBarbeiro();
        if (dados.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String[] r : dados) {
            System.out.printf("ID %s | %s | Media: %s | Total Avaliacoes: %s%n",
                    r[0], r[1], r[2], r[3]);
        }
    }

    private void relatorioClientesTop() {
        System.out.println("\n--- CLIENTES QUE MAIS REALIZARAM AGENDAMENTOS (GROUP BY) ---");
        List<String[]> dados = relatorioDAO.clientesTopAgendamentos();
        if (dados.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String[] r : dados) {
            System.out.printf("ID %s | %s | %s | Total Agendamentos: %s%n",
                    r[0], r[1], r[2], r[3]);
        }
    }
}