package service;

import dao.ServicoDAO;
import model.Servico;
import util.InputHelper;

import java.util.List;
import java.util.Scanner;

public class ServicoService {

    private final ServicoDAO servicoDAO;
    private final InputHelper input;

    public ServicoService(Scanner scanner) {
        this.servicoDAO = new ServicoDAO();
        this.input = new InputHelper(scanner);
    }

    public void cadastrar() {
        try {
            System.out.println("\n=== CADASTRAR SERVICO ===");
            String nome = input.lerStringObrigatorio("Nome: ");
            String descricao = input.lerString("Descricao: ");
            double preco = input.lerDouble("Preco (R$): ");
            int duracao = input.lerInt("Duracao (minutos): ");

            Servico servico = new Servico(null, nome,
                    descricao.isEmpty() ? null : descricao, preco, duracao);
            servicoDAO.inserir(servico);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void editar() {
        try {
            System.out.println("\n=== EDITAR SERVICO ===");
            int id = input.lerInt("ID do servico: ");
            Servico servico = servicoDAO.buscarPorId(id);
            if (servico == null) {
                System.out.println("Servico nao encontrado.");
                return;
            }
            System.out.println("Editando: " + servico.getNome());
            System.out.println("(Deixe em branco para manter o valor atual)");

            String nome = input.lerString("Novo nome: ");
            if (!nome.isEmpty()) servico.setNome(nome);

            String descricao = input.lerString("Nova descricao: ");
            if (!descricao.isEmpty()) servico.setDescricao(descricao);

            String precoStr = input.lerString("Novo preco: ");
            if (!precoStr.isEmpty()) {
                try { servico.setPreco(Double.parseDouble(precoStr.replace(",", "."))); }
                catch (NumberFormatException e) { System.out.println("Preco invalido."); }
            }

            String duracaoStr = input.lerString("Nova duracao em minutos: ");
            if (!duracaoStr.isEmpty()) {
                try { servico.setDuracaoMinutos(Integer.parseInt(duracaoStr)); }
                catch (NumberFormatException e) { System.out.println("Duracao invalida."); }
            }

            servicoDAO.atualizar(servico);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void excluir() {
        try {
            System.out.println("\n=== EXCLUIR SERVICO ===");
            int id = input.lerInt("ID do servico: ");
            Servico servico = servicoDAO.buscarPorId(id);
            if (servico == null) {
                System.out.println("Servico nao encontrado.");
                return;
            }
            System.out.println("Tem certeza que deseja excluir '" + servico.getNome() + "'?");
            String c = input.lerString("Confirme SIM para confirmar: ");
            if (c.equalsIgnoreCase("SIM")) {
                servicoDAO.excluir(id);
            } else {
                System.out.println("Operacao cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void buscar() {
        try {
            System.out.println("\n=== BUSCAR SERVICO ===");
            int id = input.lerInt("ID do servico: ");
            Servico servico = servicoDAO.buscarPorId(id);
            if (servico != null) {
                System.out.printf("%s | R$%.2f | %d min | %s%n",
                        servico.getNome(), servico.getPreco(),
                        servico.getDuracaoMinutos(), servico.getDescricao());
            } else {
                System.out.println("Servico nao encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void listar() {
        try {
            System.out.println("\n=== LISTA DE SERVICOS ===");
            List<Servico> servicos = servicoDAO.listarTodos();
            if (servicos.isEmpty()) {
                System.out.println("Nenhum servico cadastrado.");
                return;
            }
            System.out.printf("%-4s | %-25s | %-10s | %-10s | %s%n",
                    "ID", "Nome", "Preco", "Duracao", "Descricao");
            System.out.println("-".repeat(85));
            for (Servico s : servicos) {
                System.out.printf("%-4d | %-25s | R$%7.2f | %6d min | %s%n",
                        s.getIdServico(), s.getNome(), s.getPreco(),
                        s.getDuracaoMinutos(),
                        s.getDescricao() != null ? s.getDescricao() : "");
            }
            System.out.println("Total: " + servicos.size() + " servico(s)");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}