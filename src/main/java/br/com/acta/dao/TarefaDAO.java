package br.com.acta.dao;

import br.com.acta.model.Tarefa;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAO implements MetodosCrud<Tarefa> {

    // INSERT
    @Override
    public int inserir(Tarefa tarefa) {
        String sql = """
                INSERT INTO tarefa (titulo, descricao, prioridade, dt_entrega, status, dt_inicio, id_colaborador)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tarefa.getTitulo());
            pstmt.setString(2, tarefa.getDescricao());
            pstmt.setString(3, tarefa.getPrioridade().name());
            pstmt.setDate(4, tarefa.getDt_entrega());
            pstmt.setString(5, tarefa.getStatus().name());
            pstmt.setDate(6, tarefa.getDt_inicio());
            pstmt.setLong(7, tarefa.getId_colaborador());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // READ
    @Override
    public Tarefa buscar(Long id) {
        String sql = "SELECT * FROM vw_tarefa WHERE id_tarefa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearTarefa(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tarefa> buscar() {
        List<Tarefa> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_tarefa;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearTarefa(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Tarefa tarefa) {
        String sql = "UPDATE tarefa SET titulo = ?, descricao = ?, prioridade = ?, dt_entrega = ?, status = ?, dt_inicio = ?, id_colaborador = ? WHERE id_tarefa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tarefa.getTitulo());
            pstmt.setString(2, tarefa.getDescricao());
            pstmt.setString(3, tarefa.getPrioridade().name());
            pstmt.setDate(4, tarefa.getDt_entrega());
            pstmt.setString(5, tarefa.getStatus().name());
            pstmt.setDate(6, tarefa.getDt_inicio());
            pstmt.setLong(7, tarefa.getId_colaborador());
            pstmt.setLong(8, tarefa.getId_tarefa());

            if (pstmt.executeUpdate() > 0) return 1;
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // DELETE
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM tarefa WHERE id_tarefa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // SQLState "23xxx" = violação de integridade (e-mail/CPF duplicado, FK, NOT NULL).
    private static int codigoDeErro(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return 0;
        }
        return -1;
    }

    // Mapear tarefa para simplificar a busca
    private static Tarefa mapearTarefa(ResultSet rs) throws SQLException {
        Tarefa tarefa = new Tarefa();
        tarefa.setId_tarefa(rs.getLong("id_tarefa"));
        tarefa.setTitulo(rs.getString("titulo"));
        tarefa.setDescricao(rs.getString("descricao"));
        tarefa.setPrioridade(Intensidade.valueOf(rs.getString("prioridade")));
        tarefa.setDt_entrega(rs.getDate("dt_entrega"));
        tarefa.setStatus(Situacao.valueOf(rs.getString("status")));
        tarefa.setDt_inicio(rs.getDate("dt_inicio"));
        tarefa.setNome_colaborador(rs.getString("nome_colaborador"));
        return tarefa;
    }

}