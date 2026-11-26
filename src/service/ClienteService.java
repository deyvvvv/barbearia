package service;

import dao.ClienteDAO;
import model.Cliente;
import util.InputHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final Scanner scanner;
    private final InputHelper input;

    public ClienteService(Scanner scanner) {
        this.clienteDAO = new ClienteDAO();
        this.scanner = scanner;
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n=== CADASTRAR CLIENTE ===");
            String nome = input.lerStringObrigatorio("Nome: ");
            String telefone = input.lerStringObrigatorio("Telefone: ");
            String email = input.lerString("Email: ");

            Cliente cliente = new Cliente(null, nome, telefone, email.isEmpty() ? null : email, LocalDate.now());
            clienteDAO.inserir(cliente);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n=== EDITAR CLIENTE ===");
            int id = input.lerInt("ID do cliente: ");
            Cliente cliente = clienteDAO.buscarPorId(id);
            if (cliente == null) {
                System.out.println("Cliente nao encontrado.");
                return;
            }
            System.out.println("Editando: " + cliente.getNome());
            System.out.println("(Deixe em branco para manter o valor atual)");

            String nome = input.lerString("Novo nome: ");
            if (!nome.isEmpty()) cliente.setNome(nome);

            String telefone = input.lerString("Novo telefone: ");
            if (!telefone.isEmpty()) cliente.setTelefone(telefone);

            String email = input.lerString("Novo email: ");
            if (!email.isEmpty()) cliente.setEmail(email);

            clienteDAO.atualizar(cliente);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n=== EXCLUIR CLIENTE ===");
            int id = input.lerInt("ID do cliente: ");
            Cliente cliente = clienteDAO.buscarPorId(id);
            if (cliente == null) {
                System.out.println("Cliente nao encontrado.");
                return;
            }
            System.out.println("Tem certeza que deseja excluir " + cliente.getNome() + "?");
            String conf = input.lerString("Digite SIM para confirmar: ");
            if (conf.equalsIgnoreCase("SIM")) {
                clienteDAO.excluir(id);
            } else {
                System.out.println("Operacao cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n=== BUSCAR CLIENTE ===");
            int id = input.lerInt("ID do cliente: ");
            Cliente cliente = clienteDAO.buscarPorId(id);
            if (cliente != null) {
                System.out.printf("%s | %s | %s | Cadastro: %s%n",
                        cliente.getNome(), cliente.getTelefone(),
                        cliente.getEmail(), cliente.getDataCadastro());
            } else {
                System.out.println("Cliente nao encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n=== LISTA DE CLIENTES ===");
            List<Cliente> clientes = clienteDAO.listarTodos();
            if (clientes.isEmpty()) {
                System.out.println("Nenhum cliente cadastrado.");
                return;
            }
            System.out.printf("%-4s | %-25s | %-18s | %-25s | %s%n",
                    "ID", "Nome", "Telefone", "Email", "Data Cadastro");
            System.out.println("-".repeat(100));
            for (Cliente c : clientes) {
                System.out.printf("%-4d | %-25s | %-18s | %-25s | %s%n",
                        c.getIdCliente(), c.getNome(), c.getTelefone(),
                        c.getEmail() != null ? c.getEmail() : "-", c.getDataCadastro());
            }
            System.out.println("Total: " + clientes.size() + " cliente(s)");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}