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

/** Executa a persistência de LicoesAprendidas e converte consultas em objetos do modelo. */
public class LicoesAprendidasDAO implements MetodosCrud<LicoesAprendidas> {

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
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
            pstmt.setObject(6, licoesAprendidas.getFase_origem().name(), Types.OTHER);
            pstmt.setObject(7, licoesAprendidas.getSeveridade().name(), Types.OTHER);
            pstmt.setLong(8, licoesAprendidas.getId_ciclo());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
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

    /** Busca pelo ID; retorna null quando o registro não existe. */
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

    /** Lista todos os registros disponíveis na consulta. */
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

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<LicoesAprendidas> buscaFiltrada(String busca, EtapaCiclo fase, Intensidade severidade, String area, String categoria, String ordem, String direcao) {
        List<LicoesAprendidas> licoesAprendidas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_licoes_aprendidas WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temFase = fase != null;
        boolean temSeveridade = severidade != null;
        boolean temArea = area != null && !area.isBlank();
        boolean temCategoria = categoria != null && !categoria.isBlank();

        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
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

            // Os parâmetros seguem a mesma ordem das condições adicionadas ao SQL.
            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
            }
            if (temFase) pstmt.setObject(i++, fase.name(), Types.OTHER);
            if (temSeveridade) pstmt.setObject(i++, severidade.name(), Types.OTHER);
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

    /** Lista as áreas cadastradas, sem repetição e em ordem alfabética. */
    public List<String> buscarArea() {
        List<String> areas = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM licoes_aprendidas ORDER BY area;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                areas.add(rs.getString("area"));
            }
            return areas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Lista as categorias cadastradas, sem repetição e em ordem alfabética. */
    public List<String> buscarCategoria() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM licoes_aprendidas ORDER BY categoria;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(rs.getString("categoria"));
            }
            return categorias;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
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
            pstmt.setObject(6, licoesAprendidas.getFase_origem().name(), Types.OTHER);
            pstmt.setObject(7, licoesAprendidas.getSeveridade().name(), Types.OTHER);
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

    /** Exclui pelo ID; informa ausência do registro ou vínculos que impedem a exclusão. */
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


    /** Converte a linha atual do resultado em um objeto do modelo. */
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
        licoesAprendidas.setId_ciclo(rs.getLong("id_ciclo"));
        return licoesAprendidas;
    }

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_licao",
            "titulo", "titulo",
            "area", "area",
            "categoria", "categoria",
            "fase", "fase_origem",
            "severidade", "severidade",
            "ciclo", "nome_ciclo"
    );

    /** Recupera a escrita já cadastrada da área, ignorando maiúsculas. */
    public String buscarAreaExistente(String area) {
        String sql = "SELECT area FROM licoes_aprendidas WHERE LOWER(area) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, area);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs. next()) {
                    return rs.getString("area");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Recupera a escrita já cadastrada da categoria, ignorando maiúsculas. */
    public String buscarCategoriaExistente(String categoria) {
        String sql = "SELECT categoria FROM licoes_aprendidas WHERE LOWER(categoria) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs. next()) {
                    return rs.getString("categoria");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
