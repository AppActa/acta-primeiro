package br.com.acta.dao;

import br.com.acta.enums.Intensidade;
import br.com.acta.model.Meta;
import br.com.acta.enums.StatusMeta;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MetaDAO implements MetodosCrud<Meta> {

    // INSERT
    @Override
    public int inserir(Meta meta) {
        String sql = """
                INSERT INTO meta (meta, descricao_meta, objetivo, prioridade, prazo, status, id_ciclo, id_plano_acao)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meta.getMeta());
            pstmt.setString(2, meta.getDescricao_meta());
            pstmt.setString(3, meta.getObjetivo());
            pstmt.setString(4, meta.getPrioridade().name());
            pstmt.setDate(5, meta.getPrazo());
            pstmt.setString(6, meta.getStatus().name());
            pstmt.setLong(7, meta.getId_ciclo());
            pstmt.setLong(8, meta.getId_plano_acao());

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
    public Meta buscar(Long id) {
        String sql = "SELECT * FROM vw_meta WHERE id_meta = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMeta(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Meta> buscar() {
        List<Meta> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_meta;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearMeta(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Meta meta) {
        String sql = "UPDATE meta SET meta = ?, descricao_meta = ?, objetivo = ?, prioridade = ?, prazo = ?, status = ?, id_ciclo = ?, id_plano_acao = ? WHERE id_meta = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meta.getMeta());
            pstmt.setString(2, meta.getDescricao_meta());
            pstmt.setString(3, meta.getObjetivo());
            pstmt.setString(4, meta.getPrioridade().name());
            pstmt.setDate(5, meta.getPrazo());
            pstmt.setString(6, meta.getStatus().name());
            pstmt.setLong(7, meta.getId_ciclo());
            pstmt.setLong(8, meta.getId_plano_acao());
            pstmt.setLong(9, meta.getId_meta());

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
        String sql = "DELETE FROM meta WHERE id_meta = ?;";

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

    // Mapear meta para simplificar a busca
    private static Meta mapearMeta(ResultSet rs) throws SQLException {
        Meta meta = new Meta();
        meta.setId_meta(rs.getLong("id_meta"));
        meta.setMeta(rs.getString("meta"));
        meta.setDescricao_meta(rs.getString("descricao_meta"));
        meta.setObjetivo(rs.getString("objetivo"));
        meta.setPrioridade(Intensidade.valueOf(rs.getString("prioridade")));
        meta.setPrazo(rs.getDate("prazo"));
        meta.setStatus(StatusMeta.valueOf(rs.getString("status")));
        meta.setCriando_em(rs.getObject("criando_em", OffsetDateTime.class));
        meta.setNome_ciclo(rs.getString("nome_ciclo"));
        meta.setNome_plano_acao(rs.getString("nome_plano_acao"));
        return meta;
    }

}