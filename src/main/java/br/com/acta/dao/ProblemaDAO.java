package br.com.acta.dao;

import br.com.acta.enums.Intensidade;
import br.com.acta.model.Problema;
import br.com.acta.enums.StatusProblema;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Executa a persistência de Problema e converte consultas em objetos do modelo. */
public class ProblemaDAO implements MetodosCrud<Problema> {

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
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
            pstmt.setObject(3, problema.getPeso().name(), Types.OTHER);
            pstmt.setString(4, problema.getSolucao());
            pstmt.setObject(5, problema.getStatus().name(), Types.OTHER);
            pstmt.setString(6, problema.getOrigem());
            pstmt.setDate(7, problema.getEncontrado_em());
            pstmt.setLong(8, problema.getId_ciclo());
            pstmt.setObject(9, problema.getId_plano_acao(), java.sql.Types.BIGINT);
            pstmt.setObject(10, problema.getId_colaborador(), java.sql.Types.BIGINT);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de problema com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar problema.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Busca pelo ID; retorna null quando o registro não existe. */
    @Override
    public Problema buscar(Long id) {
        String sql = "SELECT * FROM vw_problema WHERE id_problema = ?;";

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
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista todos os registros disponíveis na consulta. */
    @Override
    public List<Problema> buscar() {
        List<Problema> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_problema;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearProblema(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<Problema> buscaFiltrada(String busca, Intensidade peso, StatusProblema status, String origem, Date dataInicial, Date dataFinal, String ordem, String direcao) {
        List<Problema> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_problema WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temPeso = peso != null;
        boolean temStatus = status != null;
        boolean temOrigem = origem != null && !origem.isBlank();
        boolean temDataInicial = dataInicial != null;
        boolean temDataFinal = dataFinal != null;

        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
        if (temBusca) sql.append(" AND (titulo ILIKE ? OR nome_ciclo ILIKE ? OR nome_plano_acao ILIKE ? OR nome_colaborador ILIKE ?)");
        if (temPeso) sql.append(" AND peso = ?");
        if (temStatus) sql.append(" AND status = ?");
        if (temOrigem) sql.append(" AND origem = ?");
        if (temDataInicial) sql.append(" AND encontrado_em >= ?");
        if (temDataFinal) sql.append(" AND encontrado_em <= ?");
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
            if (temPeso) pstmt.setObject(i++, peso.name(), Types.OTHER);
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temOrigem) pstmt.setString(i++, origem.trim());
            if (temDataInicial) pstmt.setDate(i++, dataInicial);
            if (temDataFinal) pstmt.setDate(i++, dataFinal);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProblema(rs));
                }
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista as origens cadastradas, sem repetição e em ordem alfabética. */
    public List<String> buscarOrigem() {
        List<String> origens = new ArrayList<>();
        String sql = "SELECT DISTINCT origem FROM problema ORDER BY origem;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                origens.add(rs.getString("origem"));
            }
            return origens;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
    @Override
    public int atualizar(Problema problema) {
        String sql = "UPDATE problema SET titulo = ?, descricao = ?, peso = ?, solucao = ?, status = ?, origem = ?, encontrado_em = ?, id_ciclo = ?, id_plano_acao = ?, id_colaborador = ? WHERE id_problema = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, problema.getTitulo());
            pstmt.setString(2, problema.getDescricao());
            pstmt.setObject(3, problema.getPeso().name(), Types.OTHER);
            pstmt.setString(4, problema.getSolucao());
            pstmt.setObject(5, problema.getStatus().name(), Types.OTHER);
            pstmt.setString(6, problema.getOrigem());
            pstmt.setDate(7, problema.getEncontrado_em());
            pstmt.setLong(8, problema.getId_ciclo());
            pstmt.setObject(9, problema.getId_plano_acao(), java.sql.Types.BIGINT);
            pstmt.setObject(10, problema.getId_colaborador(), java.sql.Types.BIGINT);
            pstmt.setLong(11, problema.getId_problema());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de problema não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de problema com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar problema.", e
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
        String sql = "DELETE FROM problema WHERE id_problema = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de problema não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de problema porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir problema.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }


    /** Converte a linha atual do resultado em um objeto do modelo. */
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
        problema.setNome_colaborador(rs.getString("nome_colaborador"));
        problema.setId_ciclo(rs.getLong("id_ciclo"));
        // wasNull preserva o vínculo ausente como null, em vez de zero.
        long idPlanoAcao = rs.getLong("id_plano_acao");
        problema.setId_plano_acao(rs.wasNull() ? null : idPlanoAcao);
        long idColaborador = rs.getLong("id_colaborador");
        problema.setId_colaborador(rs.wasNull() ? null : idColaborador);
        return problema;
    }

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_problema",
            "titulo", "titulo",
            "peso", "peso",
            "status", "status",
            "origem", "origem",
            "encontrado_em", "encontrado_em",
            "ciclo", "nome_ciclo",
            "plano_acao", "nome_plano_acao"
    );

    /** Recupera a escrita já cadastrada da origem, ignorando maiúsculas. */
    public String buscarOrigemExistente(String origem) {
        String sql = "SELECT origem FROM problema WHERE LOWER(origem) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, origem);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs. next()) {
                    return rs.getString("origem");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
