package service;

import dao.AgendamentoDAO;
import dao.BarbeiroDAO;
import dao.ClienteDAO;
import dao.ServicoDAO;
import model.Agendamento;
import model.Barbeiro;
import model.Cliente;
import model.Servico;
import util.InputHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class AgendamentoService {

    private final AgendamentoDAO agendamentoDAO;
    private final BarbeiroDAO barbeiroDAO;
    private final ClienteDAO clienteDAO;
    private final ServicoDAO servicoDAO;
    private final Scanner scanner;
    private final InputHelper input;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // REALIZADO fica de fora: a procedure é a única via para concluir um atendimento.
    private static final Set<String> STATUS_VALIDOS =
            Set.of("AGENDADO", "CONFIRMADO", "CANCELADO");

    public AgendamentoService(Scanner scanner) {
        this.agendamentoDAO = new AgendamentoDAO();
        this.barbeiroDAO = new BarbeiroDAO();
        this.clienteDAO = new ClienteDAO();
        this.servicoDAO = new ServicoDAO();
        this.scanner = scanner;
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n--- CADASTRAR AGENDAMENTO ---");

            List<Cliente> clientes = clienteDAO.listarTodos();
            if (clientes.isEmpty()) {
                System.out.println("Nenhum cliente cadastrado. Cadastre um cliente primeiro.");
                return;
            }
            System.out.println("\nClientes disponiveis:");
            for (Cliente c : clientes) {
                System.out.printf("ID %d | %s | %s%n", c.getIdCliente(), c.getNome(), c.getTelefone());
            }
            int idCliente = input.lerInt("ID do cliente: ");
            Cliente cliente = clienteDAO.buscarPorId(idCliente);
            if (cliente == null) {
                System.out.println("Cliente nao encontrado.");
                return;
            }

            List<Barbeiro> barbeiros = barbeiroDAO.listarTodos();
            if (barbeiros.isEmpty()) {
                System.out.println("Nenhum barbeiro cadastrado.");
                return;
            }
            System.out.println("\nBarbeiros disponiveis:");
            for (Barbeiro b : barbeiros) {
                System.out.printf("ID %d | %s | %s%n", b.getIdBarbeiro(), b.getNome(), b.getEspecialidade());
            }
            int idBarbeiro = input.lerInt("ID do barbeiro: ");
            Barbeiro barbeiro = barbeiroDAO.buscarPorId(idBarbeiro);
            if (barbeiro == null) {
                System.out.println("Barbeiro nao encontrado.");
                return;
            }

            List<Servico> servicos = servicoDAO.listarTodos();
            if (servicos.isEmpty()) {
                System.out.println("Nenhum servico cadastrado.");
                return;
            }
            System.out.println("\nServicos disponiveis:");
            for (Servico s : servicos) {
                System.out.printf("ID %d | %s | R$%.2f | %d min%n", s.getIdServico(), s.getNome(), s.getPreco(), s.getDuracaoMinutos());
            }
            int idServico = input.lerInt("ID do servico: ");

            Servico servico = servicoDAO.buscarPorId(idServico);
            if (servico == null) {
                System.out.println("Servico nao encontrado.");
                return;
            }

            System.out.println("\nHorarios disponiveis para este barbeiro hoje:");
            List<String[]> horarios = agendamentoDAO.listarHorariosDisponiveis(idBarbeiro, LocalDate.now());
            if (horarios.isEmpty()) {
                System.out.println("Nenhum horario disponivel hoje.");
            } else {
                for (String[] h : horarios) {
                    System.out.print(h[0] + "  ");
                }
                System.out.println();
            }

            System.out.print("Data e hora (dd/MM/yyyy HH:mm): ");
            String dataStr = scanner.nextLine().trim();
            if (dataStr.isEmpty()) {
                System.out.println("Operacao cancelada.");
                return;
            }
            LocalDateTime dataHora;
            try {
                dataHora = LocalDateTime.parse(dataStr, FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Erro: Formato de data/hora invalido. Use dd/MM/yyyy HH:mm (ex: 20/07/2026 14:30).");
                return;
            }

            if (agendamentoDAO.temConflito(idBarbeiro, dataHora, servico.getDuracaoMinutos())) {
                System.out.println("Erro: Este barbeiro ja possui um agendamento neste horario.");
                System.out.println("Veja os horarios disponiveis na opcao 'Ver horarios disponiveis'.");
                return;
            }

            System.out.print("Observacoes: ");
            String observacoes = scanner.nextLine().trim();

            Agendamento agendamento = new Agendamento(null, idCliente, idBarbeiro, idServico,
                    dataHora, "AGENDADO", observacoes.isEmpty() ? null : observacoes);
            agendamentoDAO.inserir(agendamento);
            if (agendamento.getIdAgendamento() != null) {
                System.out.println("Agendamento cadastrado com ID: " + agendamento.getIdAgendamento());
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n--- EDITAR AGENDAMENTO ---");
            int id = input.lerInt("ID do agendamento: ");
            Agendamento agendamento = agendamentoDAO.buscarPorId(id);
            if (agendamento == null) {
                System.out.println("Agendamento nao encontrado.");
                return;
            }
            System.out.println("Agendamento atual: ID " + agendamento.getIdAgendamento()
                    + " | Data: " + agendamento.getDataHora().format(FORMATTER)
                    + " | Status: " + agendamento.getStatus());

            String idClienteStr = input.lerString("Novo ID do cliente (Enter para manter): ");
            if (!idClienteStr.isEmpty()) {
                try {
                    int novoIdCli = Integer.parseInt(idClienteStr);
                    if (clienteDAO.buscarPorId(novoIdCli) == null) {
                        System.out.println("Cliente nao existe. Operacao cancelada.");
                        return;
                    }
                    agendamento.setIdCliente(novoIdCli);
                } catch (NumberFormatException e) {
                    System.out.println("ID invalido.");
                }
            }

            String idBarbeiroStr = input.lerString("Novo ID do barbeiro (Enter para manter): ");
            if (!idBarbeiroStr.isEmpty()) {
                try {
                    int novoIdBarb = Integer.parseInt(idBarbeiroStr);
                    if (barbeiroDAO.buscarPorId(novoIdBarb) == null) {
                        System.out.println("Barbeiro nao existe. Operacao cancelada.");
                        return;
                    }
                    agendamento.setIdBarbeiro(novoIdBarb);
                } catch (NumberFormatException e) {
                    System.out.println("ID invalido.");
                }
            }

            String idServicoStr = input.lerString("Novo ID do servico (Enter para manter): ");
            if (!idServicoStr.isEmpty()) {
                try {
                    int novoIdServ = Integer.parseInt(idServicoStr);
                    if (servicoDAO.buscarPorId(novoIdServ) == null) {
                        System.out.println("Servico nao existe. Operacao cancelada.");
                        return;
                    }
                    agendamento.setIdServico(novoIdServ);
                } catch (NumberFormatException e) {
                    System.out.println("ID invalido.");
                }
            }

            System.out.print("Nova data e hora (dd/MM/yyyy HH:mm) (Enter para manter): ");
            String dataStr = scanner.nextLine().trim();
            if (!dataStr.isEmpty()) {
                try {
                    agendamento.setDataHora(LocalDateTime.parse(dataStr, FORMATTER));
                } catch (DateTimeParseException e) {
                    System.out.println("Formato invalido. Mantido anterior.");
                }
            }

            System.out.print("Novo status (AGENDADO/CONFIRMADO/CANCELADO): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (!status.isEmpty()) {
                if (!STATUS_VALIDOS.contains(status)) {
                    System.out.println("Status invalido. Use AGENDADO, CONFIRMADO ou CANCELADO. " +
                            "Para REALIZADO, use a opcao 9 > Finalizar atendimento.");
                    return;
                }
                agendamento.setStatus(status);
            }

            System.out.print("Novas observacoes (Enter para manter): ");
            String obs = scanner.nextLine().trim();
            if (!obs.isEmpty()) agendamento.setObservacoes(obs);

            agendamentoDAO.atualizar(agendamento);
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n--- EXCLUIR AGENDAMENTO ---");
            int id = input.lerInt("ID do agendamento: ");
            agendamentoDAO.excluir(id);
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n--- BUSCAR AGENDAMENTO ---");
            int id = input.lerInt("ID do agendamento: ");
            Agendamento agendamento = agendamentoDAO.buscarPorId(id);
            if (agendamento != null) {
                System.out.printf("ID %d | ClienteID %d | BarbeiroID %d | ServicoID %d | %s | %s%n",
                        agendamento.getIdAgendamento(), agendamento.getIdCliente(),
                        agendamento.getIdBarbeiro(), agendamento.getIdServico(),
                        agendamento.getDataHora().format(FORMATTER), agendamento.getStatus());
            } else {
                System.out.println("Agendamento nao encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n--- LISTAR AGENDAMENTOS ---");
            List<Agendamento> agendamentos = agendamentoDAO.listarTodos();
            if (agendamentos.isEmpty()) {
                System.out.println("Nenhum agendamento cadastrado.");
                return;
            }
            for (Agendamento a : agendamentos) {
                System.out.printf("ID %d | ClienteID %d | BarbeiroID %d | ServicoID %d | %s | %s%n",
                        a.getIdAgendamento(), a.getIdCliente(), a.getIdBarbeiro(),
                        a.getIdServico(), a.getDataHora().format(FORMATTER), a.getStatus());
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void historicoComNomes() {
        try {
            System.out.println("\n--- HISTORICO DE AGENDAMENTOS ---");
            List<Agendamento> historico = agendamentoDAO.listarHistoricoComNomes();
            if (historico.isEmpty()) {
                System.out.println("Nenhum agendamento encontrado.");
                return;
            }
            for (Agendamento a : historico) {
                System.out.printf("ID %d | Cliente: %s | Barbeiro: %s | Servico: %s | %s | %s%n",
                        a.getIdAgendamento(), a.getNomeCliente(), a.getNomeBarbeiro(),
                        a.getNomeServico(), a.getDataHora().format(FORMATTER), a.getStatus());
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void verHorariosDisponiveis() {
        try {
            System.out.println("\n--- HORARIOS DISPONIVEIS ---");
            List<Barbeiro> barbeiros = barbeiroDAO.listarTodos();
            if (barbeiros.isEmpty()) {
                System.out.println("Nenhum barbeiro cadastrado.");
                return;
            }
            System.out.println("Barbeiros:");
            for (Barbeiro b : barbeiros) {
                System.out.printf("ID %d | %s%n", b.getIdBarbeiro(), b.getNome());
            }
            int idBarbeiro = input.lerInt("ID do barbeiro: ");

            System.out.print("Data (dd/MM/yyyy) ou Enter para hoje: ");
            String dataStr = scanner.nextLine().trim();
            LocalDate data;
            try {
                if (dataStr.isEmpty()) {
                    data = LocalDate.now();
                } else {
                    data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (DateTimeParseException e) {
                System.out.println("Data invalida. Usando data atual.");
                data = LocalDate.now();
            }

            List<String[]> horarios = agendamentoDAO.listarHorariosDisponiveis(idBarbeiro, data);
            System.out.println("\nHorarios disponiveis para " + barbeiroDAO.buscarPorId(idBarbeiro).getNome()
                    + " em " + data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ":");
            if (horarios.isEmpty()) {
                System.out.println("Nenhum horario disponivel neste dia.");
            } else {
                for (String[] h : horarios) {
                    System.out.println("- " + h[0]);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }
}
