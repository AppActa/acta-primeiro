package br.com.acta.dao;

import br.com.acta.model.LicoesAprendidas;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Intensidade;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de lição aprendida com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar lição aprendida.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
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
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public List<LicoesAprendidas> buscar() {
        List<LicoesAprendidas> licoesAprendidas = new ArrayList<>();
        String sql = "SELECT * FROM vw_licoes_aprendidas;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                licoesAprendidas.add(mapearLicoesAprendidas(rs));
            }
            return licoesAprendidas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<LicoesAprendidas> buscaFiltrada(String busca, EtapaCiclo fase, Intensidade severidade, String area, String categoria, String ordem, String direcao) {
        List<LicoesAprendidas> licoesAprendidas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_licoes_aprendidas WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temFase = fase != null;
        boolean temSeveridade = severidade != null;
        boolean temArea = area != null && !area.isBlank();
        boolean temCategoria = categoria != null && !categoria.isBlank();

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (titulo ILIKE ? OR aprendizado ILIKE ? OR nome_ciclo ILIKE ?)");
        if (temFase) sql.append(" AND fase_origem = ?");
        if (temSeveridade) sql.append(" AND severidade = ?");
        if (temArea) sql.append(" AND area = ?");
        if (temCategoria) sql.append(" AND categoria = ?");
        if (temOrdem) {
            String direcaoSQL = "DESC".equalsIgnoreCase(direcao) ? " DESC" : " ASC";
            sql.append(" ORDER BY ").append(colunaOrdem).append(direcaoSQL);
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
            }
            if (temFase) pstmt.setString(i++, fase.name());
            if (temSeveridade) pstmt.setString(i++, severidade.name());
            if (temArea) pstmt.setString(i++, area.trim());
            if (temCategoria) pstmt.setString(i++, categoria.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    licoesAprendidas.add(mapearLicoesAprendidas(rs));
                }
            }
            return licoesAprendidas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
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

            throw new RegistroNaoEncontradoException("Registro de lição aprendida não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de lição aprendida com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar lição aprendida.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
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
            throw new RegistroNaoEncontradoException("Registro de lição aprendida não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de lição aprendida porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir lição aprendida.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    // SQLState "23xxx" = violação de integridade (e-mail/CPF duplicado, FK, NOT NULL).

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

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "titulo", "titulo",
            "area", "area",
            "categoria", "categoria",
            "fase", "fase_origem",
            "severidade", "severidade",
            "ciclo", "nome_ciclo"
    );

}