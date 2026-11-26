package dao;

import database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelatorioDAO {

    public List<String[]> historicoAgendamentos() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
            SELECT a.id_agendamento, c.nome AS cliente, b.nome AS barbeiro,
                   s.nome AS servico, a.data_hora, a.status
            FROM Agendamento a
            INNER JOIN Cliente c ON a.id_cliente = c.id_cliente
            INNER JOIN Barbeiro b ON a.id_barbeiro = b.id_barbeiro
            INNER JOIN Servico s ON a.id_servico = s.id_servico
            ORDER BY a.data_hora DESC""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("id_agendamento"),
                    rs.getString("cliente"),
                    rs.getString("barbeiro"),
                    rs.getString("servico"),
                    rs.getTimestamp("data_hora").toString(),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta de historico: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> totalArrecadadoPorBarbeiro() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
            SELECT b.id_barbeiro, b.nome AS barbeiro,
                   COUNT(a.id_agendamento) AS total_atendimentos,
                   COALESCE(SUM(p.valor), 0) AS total_arrecadado
            FROM Barbeiro b
            LEFT JOIN Agendamento a ON b.id_barbeiro = a.id_barbeiro AND a.status = 'REALIZADO'
            LEFT JOIN Pagamento p ON a.id_agendamento = p.id_agendamento AND p.status_pagamento = 'PAGO'
            GROUP BY b.id_barbeiro, b.nome
            ORDER BY total_arrecadado DESC""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("id_barbeiro"),
                    rs.getString("barbeiro"),
                    rs.getString("total_atendimentos"),
                    String.format("%.2f", rs.getDouble("total_arrecadado"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta de arrecadacao: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> atendimentosPorBarbeiro() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
            SELECT b.id_barbeiro, b.nome AS barbeiro,
                   COUNT(a.id_agendamento) AS quantidade_atendimentos
            FROM Barbeiro b
            LEFT JOIN Agendamento a ON b.id_barbeiro = a.id_barbeiro
            GROUP BY b.id_barbeiro, b.nome
            ORDER BY quantidade_atendimentos DESC""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("id_barbeiro"),
                    rs.getString("barbeiro"),
                    rs.getString("quantidade_atendimentos")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta de atendimentos: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> mediaAvaliacoesPorBarbeiro() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
            SELECT b.id_barbeiro, b.nome AS barbeiro,
                   ROUND(AVG(av.nota), 2) AS media_avaliacoes,
                   COUNT(av.id_avaliacao) AS total_avaliacoes
            FROM Barbeiro b
            LEFT JOIN Avaliacao av ON b.id_barbeiro = av.id_barbeiro
            GROUP BY b.id_barbeiro, b.nome
            ORDER BY media_avaliacoes DESC""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("id_barbeiro"),
                    rs.getString("barbeiro"),
                    String.format("%.2f", rs.getDouble("media_avaliacoes")),
                    rs.getString("total_avaliacoes")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta de avaliacoes: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> clientesTopAgendamentos() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
            SELECT c.id_cliente, c.nome AS cliente, c.telefone,
                   COUNT(a.id_agendamento) AS total_agendamentos
            FROM Cliente c
            INNER JOIN Agendamento a ON c.id_cliente = a.id_cliente
            GROUP BY c.id_cliente, c.nome, c.telefone
            ORDER BY total_agendamentos DESC""";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("id_cliente"),
                    rs.getString("cliente"),
                    rs.getString("telefone"),
                    rs.getString("total_agendamentos")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta de clientes top: " + e.getMessage());
        }
        return lista;
    }
}