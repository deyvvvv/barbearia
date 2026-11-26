package dao;

import database.DatabaseConnection;
import model.Pagamento;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    public void inserir(Pagamento pagamento) {
        String sql = "INSERT INTO Pagamento (id_agendamento, valor, data_pagamento, forma_pagamento, status_pagamento) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pagamento.getIdAgendamento());
            stmt.setDouble(2, pagamento.getValor());
            stmt.setDate(3, Date.valueOf(pagamento.getDataPagamento() != null
                    ? pagamento.getDataPagamento() : LocalDate.now()));
            stmt.setString(4, pagamento.getFormaPagamento());
            stmt.setString(5, pagamento.getStatusPagamento() != null ? pagamento.getStatusPagamento() : "PENDENTE");
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                pagamento.setIdPagamento(rs.getInt(1));
            }
            System.out.println("Pagamento inserido com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao inserir pagamento: " + e.getMessage());
        }
    }

    public void atualizar(Pagamento pagamento) {
        String sql = "UPDATE Pagamento SET id_agendamento = ?, valor = ?, data_pagamento = ?, "
                + "forma_pagamento = ?, status_pagamento = ? WHERE id_pagamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pagamento.getIdAgendamento());
            stmt.setDouble(2, pagamento.getValor());
            stmt.setDate(3, Date.valueOf(pagamento.getDataPagamento()));
            stmt.setString(4, pagamento.getFormaPagamento());
            stmt.setString(5, pagamento.getStatusPagamento());
            stmt.setInt(6, pagamento.getIdPagamento());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Pagamento atualizado com sucesso!");
            } else {
                System.out.println("Pagamento nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar pagamento: " + e.getMessage());
        }
    }

    public void excluir(Integer id) {
        String sql = "DELETE FROM Pagamento WHERE id_pagamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Pagamento excluido com sucesso!");
            } else {
                System.out.println("Pagamento nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir pagamento: " + e.getMessage());
        }
    }

    public Pagamento buscarPorId(Integer id) {
        String sql = "SELECT * FROM Pagamento WHERE id_pagamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar pagamento: " + e.getMessage());
        }
        return null;
    }

    public List<Pagamento> listarTodos() {
        String sql = "SELECT * FROM Pagamento ORDER BY id_pagamento";
        List<Pagamento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos: " + e.getMessage());
        }
        return lista;
    }

    private Pagamento mapear(ResultSet rs) throws SQLException {
        Pagamento p = new Pagamento();
        p.setIdPagamento(rs.getInt("id_pagamento"));
        p.setIdAgendamento(rs.getInt("id_agendamento"));
        p.setValor(rs.getDouble("valor"));
        Date d = rs.getDate("data_pagamento");
        if (d != null) {
            p.setDataPagamento(d.toLocalDate());
        }
        p.setFormaPagamento(rs.getString("forma_pagamento"));
        p.setStatusPagamento(rs.getString("status_pagamento"));
        return p;
    }
}
