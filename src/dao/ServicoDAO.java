package dao;

import database.DatabaseConnection;
import model.Servico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    public void inserir(Servico servico) {
        String sql = "INSERT INTO Servico (nome, descricao, preco, duracao_minutos) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setDouble(3, servico.getPreco());
            stmt.setInt(4, servico.getDuracaoMinutos());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                servico.setIdServico(rs.getInt(1));
            }
            System.out.println("Servico inserido com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir servico: " + e.getMessage());
        }
    }

    public void atualizar(Servico servico) {
        String sql = "UPDATE Servico SET nome = ?, descricao = ?, preco = ?, duracao_minutos = ? WHERE id_servico = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setDouble(3, servico.getPreco());
            stmt.setInt(4, servico.getDuracaoMinutos());
            stmt.setInt(5, servico.getIdServico());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Servico atualizado com sucesso!");
            } else {
                System.out.println("Servico nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar servico: " + e.getMessage());
        }
    }

    public void excluir(Integer id) {
        String sql = "DELETE FROM Servico WHERE id_servico = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Servico excluido com sucesso!");
            } else {
                System.out.println("Servico nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir servico: " + e.getMessage());
        }
    }

    public Servico buscarPorId(Integer id) {
        String sql = "SELECT * FROM Servico WHERE id_servico = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar servico: " + e.getMessage());
        }
        return null;
    }

    public List<Servico> listarTodos() {
        String sql = "SELECT * FROM Servico ORDER BY id_servico";
        List<Servico> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar servicos: " + e.getMessage());
        }
        return lista;
    }

    private Servico mapear(ResultSet rs) throws SQLException {
        Servico s = new Servico();
        s.setIdServico(rs.getInt("id_servico"));
        s.setNome(rs.getString("nome"));
        s.setDescricao(rs.getString("descricao"));
        s.setPreco(rs.getDouble("preco"));
        s.setDuracaoMinutos(rs.getInt("duracao_minutos"));
        return s;
    }
}
