package dao;

import database.DatabaseConnection;
import model.Barbeiro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BarbeiroDAO {

    public void inserir(Barbeiro barbeiro) {
        String sql = "INSERT INTO Barbeiro (nome, telefone, email, especialidade, data_contratacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, barbeiro.getNome());
            stmt.setString(2, barbeiro.getTelefone());
            stmt.setString(3, barbeiro.getEmail());
            stmt.setString(4, barbeiro.getEspecialidade());
            stmt.setDate(5, Date.valueOf(barbeiro.getDataContratacao() != null
                    ? barbeiro.getDataContratacao() : LocalDate.now()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                barbeiro.setIdBarbeiro(rs.getInt(1));
            }
            System.out.println("Barbeiro inserido com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir barbeiro: " + e.getMessage());
        }
    }

    public void atualizar(Barbeiro barbeiro) {
        String sql = "UPDATE Barbeiro SET nome = ?, telefone = ?, email = ?, especialidade = ?, data_contratacao = ? WHERE id_barbeiro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barbeiro.getNome());
            stmt.setString(2, barbeiro.getTelefone());
            stmt.setString(3, barbeiro.getEmail());
            stmt.setString(4, barbeiro.getEspecialidade());
            stmt.setDate(5, Date.valueOf(barbeiro.getDataContratacao()));
            stmt.setInt(6, barbeiro.getIdBarbeiro());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Barbeiro atualizado com sucesso!");
            } else {
                System.out.println("Barbeiro nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar barbeiro: " + e.getMessage());
        }
    }

    public void excluir(Integer id) {
        String sql = "DELETE FROM Barbeiro WHERE id_barbeiro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Barbeiro excluido com sucesso!");
            } else {
                System.out.println("Barbeiro nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir barbeiro: " + e.getMessage());
        }
    }

    public Barbeiro buscarPorId(Integer id) {
        String sql = "SELECT * FROM Barbeiro WHERE id_barbeiro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar barbeiro: " + e.getMessage());
        }
        return null;
    }

    public List<Barbeiro> listarTodos() {
        String sql = "SELECT * FROM Barbeiro ORDER BY id_barbeiro";
        List<Barbeiro> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar barbeiros: " + e.getMessage());
        }
        return lista;
    }

    private Barbeiro mapear(ResultSet rs) throws SQLException {
        Barbeiro b = new Barbeiro();
        b.setIdBarbeiro(rs.getInt("id_barbeiro"));
        b.setNome(rs.getString("nome"));
        b.setTelefone(rs.getString("telefone"));
        b.setEmail(rs.getString("email"));
        b.setEspecialidade(rs.getString("especialidade"));
        Date d = rs.getDate("data_contratacao");
        if (d != null) {
            b.setDataContratacao(d.toLocalDate());
        }
        return b;
    }
}
