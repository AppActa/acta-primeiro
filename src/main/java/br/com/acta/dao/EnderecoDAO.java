package br.com.acta.dao;

import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.model.Endereco;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Executa a persistência de Endereco e converte consultas em objetos do modelo. */
public class EnderecoDAO implements MetodosCrud<Endereco> {

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_endereco",
            "rua", "rua",
            "bairro", "bairro",
            "cidade", "cidade",
            "estado", "estado",
            "cep", "cep",
            "empresa", "nome_empresa");

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
    @Override
    public int inserir(Endereco endereco) {
        String sql = """
                INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, id_empresa)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, endereco.getRua());
            pstmt.setString(2, endereco.getBairro());
            pstmt.setString(3, endereco.getCidade());
            pstmt.setString(4, endereco.getEstado());
            pstmt.setString(5, endereco.getCep());
            pstmt.setString(6, endereco.getNumero());
            pstmt.setString(7, endereco.getComplemento());
            pstmt.setString(8, endereco.getUnidade());
            pstmt.setLong(9, endereco.getId_empresa());

            if (pstmt.executeUpdate() > 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de endereço com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar endereço.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Busca pelo ID; retorna null quando o registro não existe. */
    @Override
    public Endereco buscar(Long id) {
        String sql = "SELECT * FROM vw_endereco WHERE id_endereco = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEndereco(rs);
                }
                return null;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista todos os registros disponíveis na consulta. */
    @Override
    public List<Endereco> buscar() {
        List<Endereco> enderecos = new ArrayList<>();
        String sql = "SELECT * FROM vw_endereco;";

        try (Connection conn = Conexao.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                enderecos.add(mapearEndereco(rs));
            }
            return enderecos;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<Endereco> buscaFiltrada(String busca, String estado, String cidade, String ordem, String direcao) {
        List<Endereco> enderecos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_endereco WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temEstado = estado != null && !estado.isBlank();
        boolean temCidade = cidade != null && !cidade.isBlank();

        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
        if (temBusca) {
            sql.append(" AND (rua ILIKE ? OR bairro ILIKE ? OR cep ILIKE ? OR nome_empresa ILIKE ?)");
        }
        if (temEstado) {
            sql.append(" AND estado = ?");
        }
        if (temCidade) {
            sql.append(" AND cidade = ?");
        }
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
                String documento = busca.trim();
                if (documento.matches("[0-9.()\\s-]+")) {
                    String limpo = documento.replaceAll("\\D", "");
                    if (!limpo.isEmpty()) {
                        documento = limpo;
                    }
                }
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, "%" + documento + "%");
                pstmt.setString(i++, termo);
            }
            if (temEstado) {
                pstmt.setString(i++, estado);
            }
            if (temCidade) {
                pstmt.setString(i++, cidade.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    enderecos.add(mapearEndereco(rs));
                }
            }
            return enderecos;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista as cidades cadastradas, sem repetição e em ordem alfabética. */
    public List<String> buscarCidade() {
        List<String> cidades = new ArrayList<>();
        String sql = "SELECT DISTINCT cidade FROM endereco ORDER BY cidade;";

        try (Connection conn = Conexao.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cidades.add(rs.getString("cidade"));
            }
            return cidades;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
    @Override
    public int atualizar(Endereco endereco) {
        String sql = "UPDATE endereco SET rua = ?, bairro = ?, cidade = ?, estado = ?, cep = ?, numero = ?, "
                + "complemento = ?, unidade = ?, id_empresa = ? WHERE id_endereco = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, endereco.getRua());
            pstmt.setString(2, endereco.getBairro());
            pstmt.setString(3, endereco.getCidade());
            pstmt.setString(4, endereco.getEstado());
            pstmt.setString(5, endereco.getCep());
            pstmt.setString(6, endereco.getNumero());
            pstmt.setString(7, endereco.getComplemento());
            pstmt.setString(8, endereco.getUnidade());
            pstmt.setLong(9, endereco.getId_empresa());
            pstmt.setLong(10, endereco.getId_endereco());

            if (pstmt.executeUpdate() > 0) {
                return 1;
            }

            throw new RegistroNaoEncontradoException("Registro de endereço não encontrado para atualização.");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de endereço com esses dados.", e);
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Referência inválida na atualização.", e);
            }

            throw new DaoException("Erro ao atualizar endereço.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Exclui pelo ID; informa ausência do registro ou vínculos que impedem a exclusão. */
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM endereco WHERE id_endereco = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) {
                return 1;
            }
            throw new RegistroNaoEncontradoException("Registro de endereço não encontrado para exclusão.");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de endereço porque existem registros vinculados.", e);
            }

            throw new DaoException("Erro ao excluir endereço.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Recupera a escrita já cadastrada da cidade, ignorando maiúsculas. */
    public String buscarCidadeExistente(String cidade) {
        String sql = "SELECT cidade FROM endereco WHERE LOWER(cidade) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cidade);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("cidade");
                }
                return null;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Converte a linha atual do resultado em um objeto do modelo. */
    private static Endereco mapearEndereco(ResultSet rs) throws SQLException {
        Endereco endereco = new Endereco();
        endereco.setId_endereco(rs.getLong("id_endereco"));
        endereco.setRua(rs.getString("rua"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setCidade(rs.getString("cidade"));
        endereco.setEstado(rs.getString("estado"));
        endereco.setCep(rs.getString("cep"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setComplemento(rs.getString("complemento"));
        endereco.setUnidade(rs.getString("unidade"));
        endereco.setNome_empresa(rs.getString("nome_empresa"));
        endereco.setId_empresa(rs.getLong("id_empresa"));
        return endereco;
    }
}
