package br.com.acta.dao;

import br.com.acta.enums.Intensidade;
import br.com.acta.model.Problema;
import br.com.acta.enums.StatusProblema;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemaDAO implements MetodosCrud<Problema> {

    // INSERT
    @Override
    public int inserir(Problema problema) {
        String sql = """
                INSERT INTO problema (titulo, descricao, peso, solucao, status, origem, encontrado_em, id_ciclo, id_plano_acao, id_colaborador)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, problema.getTitulo());
            pstmt.setString(2, problema.getDescricao());
            pstmt.setString(3, problema.getPeso().name());
            pstmt.setString(4, problema.getSolucao());
            pstmt.setString(5, problema.getStatus().name());
            pstmt.setString(6, problema.getOrigem());
            pstmt.setDate(7, problema.getEncontrado_em());
            pstmt.setLong(8, problema.getId_ciclo());
            pstmt.setLong(9, problema.getId_plano_acao());
            pstmt.setLong(10, problema.getId_colaborador());

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
    public Problema buscar(Long id) {
        String sql = "SELECT * FROM problema WHERE id_problema = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProblema(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Problema> buscar() {
        List<Problema> lista = new ArrayList<>();
        String sql = "SELECT * FROM problema;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearProblema(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Problema problema) {
        String sql = "UPDATE problema SET titulo = ?, descricao = ?, peso = ?, solucao = ?, status = ?, origem = ?, encontrado_em = ?, id_ciclo = ?, id_plano_acao = ?, id_colaborador = ? WHERE id_problema = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, problema.getTitulo());
            pstmt.setString(2, problema.getDescricao());
            pstmt.setString(3, problema.getPeso().name());
            pstmt.setString(4, problema.getSolucao());
            pstmt.setString(5, problema.getStatus().name());
            pstmt.setString(6, problema.getOrigem());
            pstmt.setDate(7, problema.getEncontrado_em());
            pstmt.setLong(8, problema.getId_ciclo());
            pstmt.setLong(9, problema.getId_plano_acao());
            pstmt.setLong(10, problema.getId_colaborador());
            pstmt.setLong(11, problema.getId_problema());

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
        String sql = "DELETE FROM problema WHERE id_problema = ?;";

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

    // Mapear problemas para simplificar a busca
    private static Problema mapearProblema(ResultSet rs) throws SQLException {
        Problema problema = new Problema();
        problema.setId_problema(rs.getLong("id_problema"));
        problema.setTitulo(rs.getString("titulo"));
        problema.setDescricao(rs.getString("descricao"));
        problema.setPeso(Intensidade.valueOf(rs.getString("peso")));
        problema.setSolucao(rs.getString("solucao"));
        problema.setStatus(StatusProblema.valueOf(rs.getString("status")));
        problema.setOrigem(rs.getString("origem"));
        problema.setEncontrado_em(rs.getDate("encontrado_em"));
        problema.setNome_ciclo(rs.getString("nome_ciclo"));
        problema.setNome_plano_acao(rs.getString("nome_plano_acao"));
        problema.setNome_ciclo(rs.getString("nome_ciclo"));
        return problema;
    }

}