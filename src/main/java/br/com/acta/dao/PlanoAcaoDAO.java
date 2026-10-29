package br.com.acta.dao;

import br.com.acta.model.PlanoAcao;
import br.com.acta.enums.Situacao;
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
public class PlanoAcaoDAO implements MetodosCrud<PlanoAcao> {

    //INSERT
    @Override
    public int inserir(PlanoAcao planoAcao) {
        String sql = """
                INSERT INTO plano_acao (nome, descricao, status, prioridade, id_ciclo, id_criador)
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, planoAcao.getNome());
            pstmt.setString(2, planoAcao.getDescricao());
            pstmt.setObject(3, planoAcao.getStatus().name(), Types.OTHER);
            pstmt.setObject(4, planoAcao.getPrioridade().name(), Types.OTHER);
            pstmt.setLong(5, planoAcao.getId_ciclo());
            pstmt.setLong(6, planoAcao.getId_criador());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de plano de ação com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar plano de ação.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public PlanoAcao buscar(Long id) {
        String sql = "SELECT * FROM vw_plano_acao WHERE id_plano_acao = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPlanoAcao(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public List<PlanoAcao> buscar() {
        List<PlanoAcao> planosAcao = new ArrayList<>();
        String sql = "SELECT * FROM vw_plano_acao";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                planosAcao.add(mapearPlanoAcao(rs));
            }
            return planosAcao;

        } catch(SQLException | ClassNotFoundException e ) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }

    }

    public List<PlanoAcao> buscaFiltrada(String busca, Situacao status, Intensidade prioridade, String ordem, String direcao) {
        List<PlanoAcao> planosAcao = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_plano_acao WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temStatus = status != null;
        boolean temPrioridade = prioridade != null;

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (nome ILIKE ? OR nome_ciclo ILIKE ? OR nome_colaborador ILIKE ?)");
        if (temStatus) sql.append(" AND status = ?");
        if (temPrioridade) sql.append(" AND prioridade = ?");
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
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temPrioridade) pstmt.setObject(i++, prioridade.name(), Types.OTHER);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    planosAcao.add(mapearPlanoAcao(rs));
                }
            }
            return planosAcao;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public int atualizar(PlanoAcao planoAcao) {
        String sql = "UPDATE plano_acao SET nome = ?, descricao = ?, status = ?, prioridade = ?, id_ciclo = ?, id_criador = ? WHERE id_plano_acao = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, planoAcao.getNome());
            pstmt.setString(2, planoAcao.getDescricao());
            pstmt.setObject(3, planoAcao.getStatus().name(), Types.OTHER);
            pstmt.setObject(4, planoAcao.getPrioridade().name(), Types.OTHER);
            pstmt.setLong(5, planoAcao.getId_ciclo());
            pstmt.setLong(6, planoAcao.getId_criador());
            pstmt.setLong(7,planoAcao.getId_plano_acao());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de plano de ação não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de plano de ação com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar plano de ação.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM plano_acao WHERE id_plano_acao = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de plano de ação não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de plano de ação porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir plano de ação.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }

    }


    private static PlanoAcao mapearPlanoAcao(ResultSet rs) throws SQLException {
        PlanoAcao planoAcao = new PlanoAcao();
        planoAcao.setId_plano_acao((rs.getLong("id_plano_acao")));
        planoAcao.setNome(rs.getString("nome"));
        planoAcao.setDescricao(rs.getString("descricao"));
        planoAcao.setStatus(Situacao.valueOf(rs.getString("status")));
        planoAcao.setPrioridade(Intensidade.valueOf(rs.getString("prioridade")));
        planoAcao.setNome_ciclo(rs.getString("nome_ciclo"));
        planoAcao.setNome_criador(rs.getString("nome_colaborador"));
        planoAcao.setId_ciclo(rs.getLong("id_ciclo"));
        planoAcao.setId_criador(rs.getLong("id_criador"));
        return planoAcao;
    }

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_plano_acao",
            "nome", "nome",
            "status", "status",
            "prioridade", "prioridade",
            "ciclo", "nome_ciclo",
            "criador", "nome_colaborador"
    );
}
