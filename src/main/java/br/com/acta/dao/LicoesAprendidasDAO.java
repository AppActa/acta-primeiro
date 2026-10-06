package br.com.acta.dao;

import br.com.acta.model.LicoesAprendidas;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Intensidade;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LicoesAprendidasDAO implements MetodosCrud<LicoesAprendidas> {

    // INSERT
    @Override
    public int inserir(LicoesAprendidas licoesAprendidas) {
        String sql = """
                INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, id_ciclo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, licoesAprendidas.getTitulo());
            pstmt.setString(2, licoesAprendidas.getArea());
            pstmt.setString(3, licoesAprendidas.getAprendizado());
            pstmt.setString(4, licoesAprendidas.getCategoria());
            pstmt.setString(5, licoesAprendidas.getDescricao());
            pstmt.setString(6, licoesAprendidas.getFase_origem().name());
            pstmt.setString(7, licoesAprendidas.getSeveridade().name());
            pstmt.setLong(8, licoesAprendidas.getId_ciclo());

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
    public LicoesAprendidas buscar(Long id) {
        String sql = "SELECT * FROM vw_licoes_aprendidas WHERE id_licao = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLicoesAprendidas(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LicoesAprendidas> buscar() {
        List<LicoesAprendidas> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_licoes_aprendidas;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearLicoesAprendidas(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(LicoesAprendidas licoesAprendidas) {
        String sql = "UPDATE licoes_aprendidas SET titulo = ?, area = ?, aprendizado = ?, categoria = ?, descricao = ?, fase_origem = ?, severidade = ?, id_ciclo = ? WHERE id_licao = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, licoesAprendidas.getTitulo());
            pstmt.setString(2, licoesAprendidas.getArea());
            pstmt.setString(3, licoesAprendidas.getAprendizado());
            pstmt.setString(4, licoesAprendidas.getCategoria());
            pstmt.setString(5, licoesAprendidas.getDescricao());
            pstmt.setString(6, licoesAprendidas.getFase_origem().name());
            pstmt.setString(7, licoesAprendidas.getSeveridade().name());
            pstmt.setLong(8, licoesAprendidas.getId_ciclo());
            pstmt.setLong(9, licoesAprendidas.getId_licao());

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
        String sql = "DELETE FROM licoes_aprendidas WHERE id_licao = ?;";

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

    // Mapear licoesAprendidas para simplificar a busca
    private static LicoesAprendidas mapearLicoesAprendidas(ResultSet rs) throws SQLException {
        LicoesAprendidas licoesAprendidas = new LicoesAprendidas();
        licoesAprendidas.setId_licao(rs.getLong("id_licao"));
        licoesAprendidas.setTitulo(rs.getString("titulo"));
        licoesAprendidas.setArea(rs.getString("area"));
        licoesAprendidas.setAprendizado(rs.getString("aprendizado"));
        licoesAprendidas.setCategoria(rs.getString("categoria"));
        licoesAprendidas.setDescricao(rs.getString("descricao"));
        licoesAprendidas.setFase_origem(EtapaCiclo.valueOf(rs.getString("fase_origem")));
        licoesAprendidas.setSeveridade(Intensidade.valueOf(rs.getString("severidade")));
        licoesAprendidas.setNome_ciclo(rs.getString("nome_ciclo"));
        return licoesAprendidas;
    }

}