package dao;

import database.DatabaseConnection;
import model.Avaliacao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDAO {

    public void inserir(Avaliacao avaliacao) {
        String sql = "INSERT INTO Avaliacao (id_agendamento, id_cliente, id_barbeiro, nota, comentario, data_avaliacao) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, avaliacao.getIdAgendamento());
            stmt.setInt(2, avaliacao.getIdCliente());
            stmt.setInt(3, avaliacao.getIdBarbeiro());
            stmt.setInt(4, avaliacao.getNota());
            stmt.setString(5, avaliacao.getComentario());
            stmt.setDate(6, Date.valueOf(avaliacao.getDataAvaliacao() != null
                    ? avaliacao.getDataAvaliacao() : LocalDate.now()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                avaliacao.setIdAvaliacao(rs.getInt(1));
            }
            System.out.println("Avaliacao inserida com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir avaliacao: " + e.getMessage());
        }
    }

    public void atualizar(Avaliacao avaliacao) {
        String sql = "UPDATE Avaliacao SET id_agendamento = ?, id_cliente = ?, id_barbeiro = ?, "
                + "nota = ?, comentario = ?, data_avaliacao = ? WHERE id_avaliacao = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, avaliacao.getIdAgendamento());
            stmt.setInt(2, avaliacao.getIdCliente());
            stmt.setInt(3, avaliacao.getIdBarbeiro());
            stmt.setInt(4, avaliacao.getNota());
            stmt.setString(5, avaliacao.getComentario());
            stmt.setDate(6, Date.valueOf(avaliacao.getDataAvaliacao()));
            stmt.setInt(7, avaliacao.getIdAvaliacao());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Avaliacao atualizada com sucesso!");
            } else {
                System.out.println("Avaliacao nao encontrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar avaliacao: " + e.getMessage());
        }
    }

    public void excluir(Integer id) {
        String sql = "DELETE FROM Avaliacao WHERE id_avaliacao = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Avaliacao excluida com sucesso!");
            } else {
                System.out.println("Avaliacao nao encontrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir avaliacao: " + e.getMessage());
        }
    }

    public Avaliacao buscarPorId(Integer id) {
        String sql = "SELECT * FROM Avaliacao WHERE id_avaliacao = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar avaliacao: " + e.getMessage());
        }
        return null;
    }

    public List<Avaliacao> listarTodos() {
        String sql = "SELECT * FROM Avaliacao ORDER BY id_avaliacao";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar avaliacoes: " + e.getMessage());
        }
        return lista;
    }

    private Avaliacao mapear(ResultSet rs) throws SQLException {
        Avaliacao a = new Avaliacao();
        a.setIdAvaliacao(rs.getInt("id_avaliacao"));
        a.setIdAgendamento(rs.getInt("id_agendamento"));
        a.setIdCliente(rs.getInt("id_cliente"));
        a.setIdBarbeiro(rs.getInt("id_barbeiro"));
        a.setNota(rs.getInt("nota"));
        a.setComentario(rs.getString("comentario"));
        Date d = rs.getDate("data_avaliacao");
        if (d != null) {
            a.setDataAvaliacao(d.toLocalDate());
        }
        return a;
    }
}
