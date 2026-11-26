package dao;

import database.DatabaseConnection;
import model.Agendamento;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoDAO {

    private static final LocalTime HORA_INICIO = LocalTime.parse(
            DatabaseConnection.getConfigProperty("agenda.hora_inicio", "08:00"));
    private static final LocalTime HORA_FIM = LocalTime.parse(
            DatabaseConnection.getConfigProperty("agenda.hora_fim", "18:00"));
    private static final int INTERVALO_MINUTOS = Integer.parseInt(
            DatabaseConnection.getConfigProperty("agenda.intervalo_minutos", "30"));

    public void inserir(Agendamento agendamento) {
        String sql = "INSERT INTO Agendamento (id_cliente, id_barbeiro, id_servico, data_hora, status, observacoes) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, agendamento.getIdCliente());
            stmt.setInt(2, agendamento.getIdBarbeiro());
            stmt.setInt(3, agendamento.getIdServico());
            stmt.setTimestamp(4, Timestamp.valueOf(agendamento.getDataHora()));
            stmt.setString(5, agendamento.getStatus() != null ? agendamento.getStatus() : "AGENDADO");
            stmt.setString(6, agendamento.getObservacoes());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                agendamento.setIdAgendamento(rs.getInt(1));
            }
            System.out.println("Agendamento inserido com sucesso!");
        } catch (SQLException e) {
            // O trigger do banco é a autoridade: a mensagem precisa chegar ao usuário.
            throw new IllegalStateException(
                    "Erro ao inserir agendamento: " + e.getMessage(), e);
        }
    }

    public boolean temConflito(int idBarbeiro, LocalDateTime dataHora, int duracaoMinutos) {
        LocalDateTime fim = dataHora.plusMinutes(duracaoMinutos);
        String sql = "SELECT COUNT(*) FROM Agendamento a "
                + "INNER JOIN Servico s ON a.id_servico = s.id_servico "
                + "WHERE a.id_barbeiro = ? AND a.status NOT IN ('CANCELADO') "
                + "AND a.data_hora < ? "
                + "AND (a.data_hora + (s.duracao_minutos * INTERVAL '1 minute')) > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBarbeiro);
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            stmt.setTimestamp(3, Timestamp.valueOf(dataHora));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar conflito: " + e.getMessage());
        }
        return false;
    }

    public List<String[]> listarHorariosDisponiveis(int idBarbeiro, LocalDate data) {
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new ArrayList<>();
        }
        List<String[]> horarios = new ArrayList<>();
        LocalTime atual = HORA_INICIO;
        String sql = "SELECT a.data_hora, s.duracao_minutos FROM Agendamento a "
                + "INNER JOIN Servico s ON a.id_servico = s.id_servico "
                + "WHERE a.id_barbeiro = ? AND a.status NOT IN ('CANCELADO') "
                + "AND a.data_hora::date = ? "
                + "ORDER BY a.data_hora";
        List<LocalDateTime> ocupados = new ArrayList<>();
        List<Integer> duracoes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBarbeiro);
            stmt.setDate(2, Date.valueOf(data));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ocupados.add(rs.getTimestamp("data_hora").toLocalDateTime());
                duracoes.add(rs.getInt("duracao_minutos"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar horarios: " + e.getMessage());
        }
        while (atual.isBefore(HORA_FIM)) {
            LocalDateTime slot = LocalDateTime.of(data, atual);
            boolean disponivel = true;
            for (int i = 0; i < ocupados.size(); i++) {
                LocalDateTime inicioOcupado = ocupados.get(i);
                LocalDateTime fimOcupado = inicioOcupado.plusMinutes(duracoes.get(i));
                if (!slot.isBefore(inicioOcupado) && slot.isBefore(fimOcupado)
                        || !slot.plusMinutes(INTERVALO_MINUTOS).isBefore(inicioOcupado) && slot.plusMinutes(INTERVALO_MINUTOS).isBefore(fimOcupado)
                        || !slot.isAfter(inicioOcupado) && slot.plusMinutes(INTERVALO_MINUTOS).isAfter(inicioOcupado)) {
                    disponivel = false;
                    break;
                }
            }
            if (disponivel) {
                horarios.add(new String[]{atual.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))});
            }
            atual = atual.plusMinutes(INTERVALO_MINUTOS);
        }
        return horarios;
    }

    public void atualizar(Agendamento agendamento) {
        String sql = "UPDATE Agendamento SET id_cliente = ?, id_barbeiro = ?, id_servico = ?, "
                + "data_hora = ?, status = ?, observacoes = ? WHERE id_agendamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agendamento.getIdCliente());
            stmt.setInt(2, agendamento.getIdBarbeiro());
            stmt.setInt(3, agendamento.getIdServico());
            stmt.setTimestamp(4, Timestamp.valueOf(agendamento.getDataHora()));
            stmt.setString(5, agendamento.getStatus());
            stmt.setString(6, agendamento.getObservacoes());
            stmt.setInt(7, agendamento.getIdAgendamento());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Agendamento atualizado com sucesso!");
            } else {
                System.out.println("Agendamento nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar agendamento: " + e.getMessage());
        }
    }

    public void excluir(Integer id) {
        String sql = "DELETE FROM Agendamento WHERE id_agendamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Agendamento excluido com sucesso!");
            } else {
                System.out.println("Agendamento nao encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir agendamento: " + e.getMessage());
        }
    }

    public Agendamento buscarPorId(Integer id) {
        String sql = "SELECT * FROM Agendamento WHERE id_agendamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar agendamento: " + e.getMessage());
        }
        return null;
    }

    public List<Agendamento> listarTodos() {
        String sql = "SELECT * FROM Agendamento ORDER BY id_agendamento";
        List<Agendamento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar agendamentos: " + e.getMessage());
        }
        return lista;
    }

    public List<Agendamento> listarHistoricoComNomes() {
        String sql = "SELECT a.id_agendamento, a.id_cliente, a.id_barbeiro, a.id_servico, "
                + "a.data_hora, a.status, a.observacoes, "
                + "c.nome AS nome_cliente, b.nome AS nome_barbeiro, s.nome AS nome_servico "
                + "FROM Agendamento a "
                + "INNER JOIN Cliente c ON a.id_cliente = c.id_cliente "
                + "INNER JOIN Barbeiro b ON a.id_barbeiro = b.id_barbeiro "
                + "INNER JOIN Servico s ON a.id_servico = s.id_servico "
                + "ORDER BY a.data_hora DESC";
        List<Agendamento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Agendamento a = mapear(rs);
                a.setNomeCliente(rs.getString("nome_cliente"));
                a.setNomeBarbeiro(rs.getString("nome_barbeiro"));
                a.setNomeServico(rs.getString("nome_servico"));
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar historico: " + e.getMessage());
        }
        return lista;
    }

    private Agendamento mapear(ResultSet rs) throws SQLException {
        Agendamento a = new Agendamento();
        a.setIdAgendamento(rs.getInt("id_agendamento"));
        a.setIdCliente(rs.getInt("id_cliente"));
        a.setIdBarbeiro(rs.getInt("id_barbeiro"));
        a.setIdServico(rs.getInt("id_servico"));
        Timestamp ts = rs.getTimestamp("data_hora");
        if (ts != null) {
            a.setDataHora(ts.toLocalDateTime());
        }
        a.setStatus(rs.getString("status"));
        a.setObservacoes(rs.getString("observacoes"));
        return a;
    }
}
