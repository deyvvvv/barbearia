package service;

import dao.AvaliacaoDAO;
import model.Avaliacao;
import util.InputHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO;
    private final Scanner scanner;
    private final InputHelper input;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AvaliacaoService(Scanner scanner) {
        this.avaliacaoDAO = new AvaliacaoDAO();
        this.scanner = scanner;
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n--- CADASTRAR AVALIACAO ---");
            int idAgendamento = input.lerInt("ID do agendamento: ");
            int idCliente = input.lerInt("ID do cliente: ");
            int idBarbeiro = input.lerInt("ID do barbeiro: ");
            int nota = input.lerInt("Nota (1 a 5): ");
            String comentario = input.lerString("Comentario: ");
            System.out.print("Data (dd/MM/yyyy): ");
            String dataStr = scanner.nextLine().trim();
            LocalDate data;
            try {
                data = LocalDate.parse(dataStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Data invalida. Use dd/MM/yyyy (ex: 20/07/2026). Usando data atual.");
                data = LocalDate.now();
            }

            Avaliacao avaliacao = new Avaliacao(null, idAgendamento, idCliente, idBarbeiro,
                    nota, comentario, data);
            avaliacaoDAO.inserir(avaliacao);
            System.out.println("Avaliacao cadastrada com ID: " + avaliacao.getIdAvaliacao());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n--- EDITAR AVALIACAO ---");
            int id = input.lerInt("ID da avaliacao: ");
            Avaliacao avaliacao = avaliacaoDAO.buscarPorId(id);
            if (avaliacao == null) {
                System.out.println("Avaliacao nao encontrada.");
                return;
            }

            String idAgendStr = input.lerString("Novo ID do agendamento (Enter para manter): ");
            if (!idAgendStr.isEmpty()) { try { avaliacao.setIdAgendamento(Integer.parseInt(idAgendStr)); } catch (NumberFormatException e) { System.out.println("ID invalido."); } }

            String idCliStr = input.lerString("Novo ID do cliente (Enter para manter): ");
            if (!idCliStr.isEmpty()) { try { avaliacao.setIdCliente(Integer.parseInt(idCliStr)); } catch (NumberFormatException e) { System.out.println("ID invalido."); } }

            String idBarbStr = input.lerString("Novo ID do barbeiro (Enter para manter): ");
            if (!idBarbStr.isEmpty()) { try { avaliacao.setIdBarbeiro(Integer.parseInt(idBarbStr)); } catch (NumberFormatException e) { System.out.println("ID invalido."); } }

            String notaStr = input.lerString("Nova nota 1-5 (Enter para manter): ");
            if (!notaStr.isEmpty()) { try { avaliacao.setNota(Integer.parseInt(notaStr)); } catch (NumberFormatException e) { System.out.println("Nota invalida."); } }

            String comentario = input.lerString("Novo comentario (Enter para manter): ");
            if (!comentario.isEmpty()) avaliacao.setComentario(comentario);

            System.out.print("Nova data (dd/MM/yyyy) (Enter para manter): ");
            String dataStr = scanner.nextLine().trim();
            if (!dataStr.isEmpty()) {
                try { avaliacao.setDataAvaliacao(LocalDate.parse(dataStr, DATE_FORMATTER)); } catch (DateTimeParseException e) { System.out.println("Data invalida. Use dd/MM/yyyy (ex: 20/07/2026)."); }
            }

            avaliacaoDAO.atualizar(avaliacao);
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n--- EXCLUIR AVALIACAO ---");
            int id = input.lerInt("ID da avaliacao: ");
            avaliacaoDAO.excluir(id);
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n--- BUSCAR AVALIACAO ---");
            int id = input.lerInt("ID da avaliacao: ");
            Avaliacao avaliacao = avaliacaoDAO.buscarPorId(id);
            if (avaliacao != null) {
                System.out.println(avaliacao);
            } else {
                System.out.println("Avaliacao nao encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n--- LISTAR AVALIACOES ---");
            List<Avaliacao> avaliacoes = avaliacaoDAO.listarTodos();
            if (avaliacoes.isEmpty()) {
                System.out.println("Nenhuma avaliacao cadastrada.");
                return;
            }
            for (Avaliacao a : avaliacoes) {
                System.out.printf("ID %d | AgendamentoID %d | ClienteID %d | BarbeiroID %d | Nota %d | %s%n",
                        a.getIdAvaliacao(), a.getIdAgendamento(), a.getIdCliente(),
                        a.getIdBarbeiro(), a.getNota(), a.getDataAvaliacao());
            }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }
}
