package service;

import dao.BarbeiroDAO;
import model.Barbeiro;
import util.InputHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class BarbeiroService {

    private final BarbeiroDAO barbeiroDAO;
    private final Scanner scanner;
    private final InputHelper input;

    public BarbeiroService(Scanner scanner) {
        this.barbeiroDAO = new BarbeiroDAO();
        this.scanner = scanner;
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n=== CADASTRAR BARBEIRO ===");
            String nome = input.lerStringObrigatorio("Nome: ");
            String telefone = input.lerStringObrigatorio("Telefone: ");
            String email = input.lerString("Email: ");
            String especialidade = input.lerStringObrigatorio("Especialidade: ");

            Barbeiro barbeiro = new Barbeiro(null, nome, telefone,
                    email.isEmpty() ? null : email, especialidade, LocalDate.now());
            barbeiroDAO.inserir(barbeiro);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n=== EDITAR BARBEIRO ===");
            int id = input.lerInt("ID do barbeiro: ");
            Barbeiro barbeiro = barbeiroDAO.buscarPorId(id);
            if (barbeiro == null) {
                System.out.println("Barbeiro nao encontrado.");
                return;
            }
            System.out.println("Editando: " + barbeiro.getNome());
            System.out.println("(Deixe em branco para manter o valor atual)");

            String nome = input.lerString("Novo nome: ");
            if (!nome.isEmpty()) barbeiro.setNome(nome);

            String telefone = input.lerString("Novo telefone: ");
            if (!telefone.isEmpty()) barbeiro.setTelefone(telefone);

            String email = input.lerString("Novo email: ");
            if (!email.isEmpty()) barbeiro.setEmail(email);

            String especialidade = input.lerString("Nova especialidade: ");
            if (!especialidade.isEmpty()) barbeiro.setEspecialidade(especialidade);

            barbeiroDAO.atualizar(barbeiro);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n=== EXCLUIR BARBEIRO ===");
            int id = input.lerInt("ID do barbeiro: ");
            Barbeiro barbeiro = barbeiroDAO.buscarPorId(id);
            if (barbeiro == null) {
                System.out.println("Barbeiro nao encontrado.");
                return;
            }
            System.out.println("Tem certeza que deseja excluir " + barbeiro.getNome() + "?");
            String sc = input.lerString("Confirme SIM para confirmar: ");
            if (sc.equalsIgnoreCase("SIM")) {
                barbeiroDAO.excluir(id);
            } else {
                System.out.println("Operacao cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n=== BUSCAR BARBEIRO ===");
            int id = input.lerInt("ID do barbeiro: ");
            Barbeiro barbeiro = barbeiroDAO.buscarPorId(id);
            if (barbeiro != null) {
                System.out.printf("%s | %s | %s | Contratacao: %s%n",
                        barbeiro.getNome(), barbeiro.getEspecialidade(),
                        barbeiro.getTelefone(), barbeiro.getDataContratacao());
            } else {
                System.out.println("Barbeiro nao encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n=== LISTA DE BARBEIROS ===");
            List<Barbeiro> barbeiros = barbeiroDAO.listarTodos();
            if (barbeiros.isEmpty()) {
                System.out.println("Nenhum barbeiro cadastrado.");
                return;
            }
            System.out.printf("%-4s | %-15s | %-18s | %-30s | %s\n",
                    "ID", "Nome", "Telefone", "Especialidade", "Contratacao");
            System.out.println("─".repeat(95));
            for (Barbeiro b : barbeiros) {
                System.out.printf("%-4d | %-15s | %-18s | %-30s | %s%n",
                        b.getIdBarbeiro(), b.getNome(), b.getTelefone(),
                        b.getEspecialidade(), b.getDataContratacao());
            }
            System.out.println("Total: " + barbeiros.size() + " barbeiro");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}