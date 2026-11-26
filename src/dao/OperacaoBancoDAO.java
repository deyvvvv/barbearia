package dao;

import database.DatabaseConnection;
import model.ResultadoOperacao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Chamadas JDBC para Procedure, Functions e tabela de auditoria.
 * Mantém o SQL visível para facilitar a explicação no seminário.
 */
public class OperacaoBancoDAO {

    public ResultadoOperacao finalizarAtendimento(int idAgendamento, String formaPagamento) {
        String sql = "CALL finalizar_atendimento(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // A procedure faz COMMIT/ROLLBACK internamente: não pode ser chamada
            // dentro de uma transação aberta pelo cliente JDBC.
            conn.setAutoCommit(true);

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, idAgendamento);
                stmt.setString(2, formaPagamento);
                stmt.registerOutParameter(3, Types.VARCHAR);
                stmt.registerOutParameter(4, Types.VARCHAR);
                stmt.execute();

                String resultado = stmt.getString(3);
                String mensagem = stmt.getString(4);
                return new ResultadoOperacao("OK".equalsIgnoreCase(resultado), mensagem);
            }

        } catch (SQLException e) {
            String mensagem = "Erro ao chamar a procedure: " + e.getMessage();
            System.err.println(mensagem);
            return new ResultadoOperacao(false, mensagem);
        }
    }

    public BigDecimal precoServico(int idServico) {
        String sql = "SELECT fn_preco_servico(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idServico);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro na function escalar: " + e.getMessage());
        }
        return null;
    }

    public List<String[]> faturamentoPorBarbeiro(Integer idBarbeiro) {
        String sql = "SELECT id_barbeiro, nome, atendimentos, faturamento "
                + "FROM fn_faturamento_por_barbeiro(?)";
        List<String[]> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idBarbeiro == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, idBarbeiro);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id_barbeiro")),
                        rs.getString("nome"),
                        String.valueOf(rs.getLong("atendimentos")),
                        String.format("%.2f", rs.getBigDecimal("faturamento"))
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro na function tabela: " + e.getMessage());
        }
        return lista;
    }

    public List<String[]> listarAuditoriaPagamentos() {
        String sql = "SELECT id_auditoria, id_pagamento, status_anterior, status_novo, registrado_em "
                + "FROM AuditoriaPagamento ORDER BY id_auditoria DESC";
        List<String[]> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_auditoria")),
                    String.valueOf(rs.getInt("id_pagamento")),
                    rs.getString("status_anterior") == null ? "-" : rs.getString("status_anterior"),
                    rs.getString("status_novo"),
                    rs.getTimestamp("registrado_em").toString()
                });
            }

        } catch (SQLException e) {
            System.err.println("Erro na consulta de auditoria: " + e.getMessage());
        }
        return lista;
    }
}
