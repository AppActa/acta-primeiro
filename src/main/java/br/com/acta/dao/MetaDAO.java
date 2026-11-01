package br.com.acta.dao;

import br.com.acta.enums.Intensidade;
import br.com.acta.model.Meta;
import br.com.acta.enums.StatusMeta;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Executa a persistência de Meta e converte consultas em objetos do modelo. */
public class MetaDAO implements MetodosCrud<Meta> {

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
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
            pstmt.setObject(4, meta.getPrioridade().name(), Types.OTHER);
            pstmt.setDate(5, meta.getPrazo());
            pstmt.setObject(6, meta.getStatus().name(), Types.OTHER);
            pstmt.setLong(7, meta.getId_ciclo());
            pstmt.setObject(8, meta.getId_plano_acao(), java.sql.Types.BIGINT);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de meta com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar meta.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Busca pelo ID; retorna null quando o registro não existe. */
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
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista todos os registros disponíveis na consulta. */
    @Override
    public List<Meta> buscar() {
        List<Meta> metas = new ArrayList<>();
        String sql = "SELECT * FROM vw_meta;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                metas.add(mapearMeta(rs));
            }
            return metas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<Meta> buscaFiltrada(String busca, Intensidade prioridade, StatusMeta status, Date dataInicial, Date dataFinal, Date criadoDe, Date criadoAte, String ordem, String direcao) {
        List<Meta> metas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_meta WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temPrioridade = prioridade != null;
        boolean temStatus = status != null;
        boolean temDataInicial = dataInicial != null;
        boolean temDataFinal = dataFinal != null;
        boolean temCriadoDe = criadoDe != null;
        boolean temCriadoAte = criadoAte != null;

        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
        if (temBusca) sql.append(" AND (meta ILIKE ? OR objetivo ILIKE ? OR nome_ciclo ILIKE ? or nome_plano_acao ILIKE ?)");
        if (temPrioridade) sql.append(" AND prioridade = ?");
        if (temStatus) sql.append(" AND status = ?");
        if (temDataInicial) sql.append(" AND prazo >= ?");
        if (temDataFinal) sql.append(" AND prazo <= ?");
        if (temCriadoDe) sql.append(" AND criado_em >= ?::date");
        // O limite exclusivo do dia seguinte inclui todo o último dia do filtro.
        if (temCriadoAte) sql.append(" AND criado_em < (?::date + 1)");
        if (temOrdem) {
            String direcaoSQL = "DESC".equalsIgnoreCase(direcao) ? " DESC" : " ASC";
            sql.append(" ORDER BY ").append(colunaOrdem).append(direcaoSQL);
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Os parâmetros seguem a mesma ordem das condições adicionadas ao SQL.
            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
            }
            if (temPrioridade) pstmt.setObject(i++, prioridade.name(), Types.OTHER);
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temDataInicial) pstmt.setDate(i++, dataInicial);
            if (temDataFinal) pstmt.setDate(i++, dataFinal);
            if (temCriadoDe) pstmt.setDate(i++, criadoDe);
            if (temCriadoAte) pstmt.setDate(i++, criadoAte);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    metas.add(mapearMeta(rs));
                }
            }
            return metas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
    @Override
    public int atualizar(Meta meta) {
        String sql = "UPDATE meta SET meta = ?, descricao_meta = ?, objetivo = ?, prioridade = ?, prazo = ?, status = ?, id_ciclo = ?, id_plano_acao = ? WHERE id_meta = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meta.getMeta());
            pstmt.setString(2, meta.getDescricao_meta());
            pstmt.setString(3, meta.getObjetivo());
            pstmt.setObject(4, meta.getPrioridade().name(), Types.OTHER);
            pstmt.setDate(5, meta.getPrazo());
            pstmt.setObject(6, meta.getStatus().name(), Types.OTHER);
            pstmt.setLong(7, meta.getId_ciclo());
            pstmt.setObject(8, meta.getId_plano_acao(), java.sql.Types.BIGINT);
            pstmt.setLong(9, meta.getId_meta());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de meta não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de meta com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar meta.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    /** Exclui pelo ID; informa ausência do registro ou vínculos que impedem a exclusão. */
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM meta WHERE id_meta = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de meta não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de meta porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir meta.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }


    /** Converte a linha atual do resultado em um objeto do modelo. */
    private static Meta mapearMeta(ResultSet rs) throws SQLException {
        Meta meta = new Meta();
        meta.setId_meta(rs.getLong("id_meta"));
        meta.setMeta(rs.getString("meta"));
        meta.setDescricao_meta(rs.getString("descricao_meta"));
        meta.setObjetivo(rs.getString("objetivo"));
        meta.setPrioridade(Intensidade.valueOf(rs.getString("prioridade")));
        meta.setPrazo(rs.getDate("prazo"));
        meta.setStatus(StatusMeta.valueOf(rs.getString("status")));
        meta.setCriando_em(rs.getObject("criado_em", OffsetDateTime.class));
        meta.setNome_ciclo(rs.getString("nome_ciclo"));
        meta.setNome_plano_acao(rs.getString("nome_plano_acao"));
        meta.setId_ciclo(rs.getLong("id_ciclo"));
        // wasNull preserva o vínculo ausente como null, em vez de zero.
        long idPlanoAcao = rs.getLong("id_plano_acao");
        meta.setId_plano_acao(rs.wasNull() ? null : idPlanoAcao);
        return meta;
    }

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_meta",
            "meta", "meta",
            "prioridade", "prioridade",
            "prazo", "prazo",
            "status", "status",
            "criado_em", "criado_em",
            "ciclo", "nome_ciclo",
            "plano_acao", "nome_plano_acao"
    );

}
