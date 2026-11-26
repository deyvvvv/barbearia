import database.DatabaseConnection;
import view.MenuView;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        exibirArte();

        System.out.println("Iniciando Sistema Barbearia do Braz...\n");

        Connection conexao = DatabaseConnection.getConnection();
        if (conexao == null) {
            System.err.println("Erro: Nao foi possivel conectar ao banco de dados.");
            System.err.println("Verifique se o PostgreSQL esta rodando e se as credenciais estao corretas.");
            return;
        }

        System.out.println("Conectado ao banco de dados com sucesso!");

        try {
            MenuView menu = new MenuView();
            menu.exibirMenuPrincipal();
        } finally {
            // A conexao usada no teste inicial volta para o pool antes de fechar.
            try {
                conexao.close();
            } catch (SQLException e) {
                System.err.println("Erro ao devolver conexao ao pool: " + e.getMessage());
            }
            DatabaseConnection.closeConnection();
        }

        System.out.println("Conexao encerrada. Ate logo!");
    }

    private static void exibirArte() {
        System.out.println("  _____________________________");
        System.out.println(" |       BARBEARIA DO BRAZ     |");
        System.out.println(" |    (\u256d\u256e\u256d\u256e)              |");
        System.out.println(" |    \u256f\u2564\u2564\u2564\u2564\u2564\u2570  \u2712  \u2712\u2712\u2712     |");
        System.out.println(" |     \u2570\u256d\u2576\u2570\u2576\u256f               |");
        System.out.println(" |        \u2570\u2570\u256f\u256f                |");
        System.out.println(" |  _________________________  |");
        System.out.println(" |  Seja bem-vindo, parceiro! |");
        System.out.println(" *******************************");
        System.out.println();
    }
}
