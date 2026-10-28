package br.com.acta.dao;

import br.com.acta.model.Tarefa;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TarefaDAO implements MetodosCrud<Tarefa> {

    // INSERT
    @Override
    public int inserir(Tarefa tarefa) {
        String sql = """
                INSERT INTO tarefa (titulo, descricao, prioridade, dt_entrega, status, dt_inicio, id_colaborador, id_plano_acao)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tarefa.getTitulo());
            pstmt.setString(2, tarefa.getDescricao());
            pstmt.setObject(3, tarefa.getPrioridade().name(), Types.OTHER);
            pstmt.setDate(4, tarefa.getDt_entrega());
            pstmt.setObject(5, tarefa.getStatus().name(), Types.OTHER);
            pstmt.setDate(6, tarefa.getDt_inicio());
            pstmt.setLong(7, tarefa.getId_colaborador());
            pstmt.setLong(8, tarefa.getId_plano_acao());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de tarefa com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar tarefa.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
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
            throw new DaoException("Erro ao conectar no banco de dados.", e);
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
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<Tarefa> buscaFiltrada(String busca, Intensidade prioridade, Situacao status, Date dataInicial, Date dataFinal, String ordem, String direcao) {
        List<Tarefa> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_tarefa WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temPrioridade = prioridade != null;
        boolean temStatus = status != null;
        boolean temDataInicial = dataInicial != null;
        boolean temDataFinal = dataFinal != null;

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (titulo ILIKE ? OR nome_colaborador ILIKE ? OR nome_plano_acao ILIKE ?)");
        if (temPrioridade) sql.append(" AND prioridade = ?");
        if (temStatus) sql.append(" AND status = ?");
        if (temDataInicial) sql.append(" AND dt_entrega >= ?");
        if (temDataFinal) sql.append(" AND dt_entrega <= ?");
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
            if (temPrioridade) pstmt.setObject(i++, prioridade.name(), Types.OTHER);
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temDataInicial) pstmt.setDate(i++, dataInicial);
            if (temDataFinal) pstmt.setDate(i++, dataFinal);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTarefa(rs));
                }
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Tarefa tarefa) {
        String sql = "UPDATE tarefa SET titulo = ?, descricao = ?, prioridade = ?, dt_entrega = ?, status = ?, dt_inicio = ?, id_colaborador = ?, id_plano_acao = ? WHERE id_tarefa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tarefa.getTitulo());
            pstmt.setString(2, tarefa.getDescricao());
            pstmt.setObject(3, tarefa.getPrioridade().name(), Types.OTHER);
            pstmt.setDate(4, tarefa.getDt_entrega());
            pstmt.setObject(5, tarefa.getStatus().name(), Types.OTHER);
            pstmt.setDate(6, tarefa.getDt_inicio());
            pstmt.setLong(7, tarefa.getId_colaborador());
            pstmt.setLong(8, tarefa.getId_plano_acao());
            pstmt.setLong(9, tarefa.getId_tarefa());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de tarefa não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de tarefa com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar tarefa.", e
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
        String sql = "DELETE FROM tarefa WHERE id_tarefa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de tarefa não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de tarefa porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir tarefa.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    // SQLState "23xxx" = violação de integridade (e-mail/CPF duplicado, FK, NOT NULL).

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
        tarefa.setNome_plano_acao(rs.getString("nome_plano_acao"));
        return tarefa;
    }

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "titulo", "titulo",
            "prioridade", "prioridade",
            "entrega", "dt_entrega",
            "status", "status",
            "inicio", "dt_inicio",
            "colaborador", "nome_colaborador",
            "plano_acao", "nome_plano_acao"
    );
}
