package br.com.acta.dao;

import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.Conexao;
import br.com.acta.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Executa a persistência de AdministradorGeral e converte consultas em objetos do modelo. */
public class AdministradorGeralDAO implements MetodosCrud<AdministradorGeral> {

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "nome", "nome",
            "email", "email",
            "telefone", "telefone");

    /** Lista os nomes das empresas ativas vinculadas ao administrador. */
    public List<String> buscarEmpresasAtivas(Long idAdministrador) {
        List<String> empresas = new ArrayList<>();
        String sql = """
                SELECT emp.nome
                FROM empresa_administrador_geral eag
                JOIN empresa emp ON emp.id_empresa = eag.id_empresa
                WHERE eag.id_adm_geral = ? AND emp.status = 'ATIVA'
                ORDER BY emp.nome;
                """;

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, idAdministrador);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    empresas.add(rs.getString("nome"));
                }
            }
            return empresas;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao consultar as empresas vinculadas ao administrador.", e);
        }
    }

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
    @Override
    public int inserir(AdministradorGeral administradorGeral) {
        String sql = """
                INSERT INTO administrador_geral (nome, senha, email, telefone)
                VALUES (?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, administradorGeral.getNome());
            pstmt.setString(2, PasswordUtils.hashSenha(administradorGeral.getSenha()));
            pstmt.setString(3, administradorGeral.getEmail());
            pstmt.setString(4, administradorGeral.getTelefone());

            if (pstmt.executeUpdate() > 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de administrador com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar administrador.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Busca pelo ID; retorna null quando o registro não existe. */
    @Override
    public AdministradorGeral buscar(Long id) {
        String sql =
                "SELECT id_adm_geral, nome, email, telefone, imagem FROM administrador_geral WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAdministradorGeral(rs);
                }
                return null;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Lista todos os registros disponíveis na consulta. */
    @Override
    public List<AdministradorGeral> buscar() {
        List<AdministradorGeral> administradoresGerais = new ArrayList<>();
        String sql = "SELECT id_adm_geral, nome, email, telefone, imagem FROM administrador_geral;";

        try (Connection conn = Conexao.conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                administradoresGerais.add(mapearAdministradorGeral(rs));
            }
            return administradoresGerais;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<AdministradorGeral> buscaFiltrada(String busca, String ordem, String direcao) {
        List<AdministradorGeral> administradoresGerais = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM administrador_geral WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();

        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
        if (temBusca) {
            sql.append(" AND (nome ILIKE ? OR email ILIKE ? OR telefone ILIKE ?)");
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
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    administradoresGerais.add(mapearAdministradorGeral(rs));
                }
            }
            return administradoresGerais;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Busca pelo e-mail e verifica a senha; devolve null se as credenciais não conferem. */
    public AdministradorGeral autenticar(String email, String senha) {
        String sql = "SELECT * FROM administrador_geral WHERE email = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AdministradorGeral administradorGeral = mapearAdministradorGeral(rs);
                    String hash = rs.getString("senha");

                    if (PasswordUtils.verificarSenha(senha, hash)) {
                        return administradorGeral;
                    }
                }
                return null;
            }
        } catch (Exception e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
    @Override
    public int atualizar(AdministradorGeral administradorGeral) {
        String sql =
                "UPDATE administrador_geral SET nome = ?, email = ?, telefone = ?, imagem = ? WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, administradorGeral.getNome());
            pstmt.setString(2, administradorGeral.getEmail());
            pstmt.setString(3, administradorGeral.getTelefone());
            pstmt.setString(4, administradorGeral.getImagem());
            pstmt.setLong(5, administradorGeral.getAdm_geral_id());

            if (pstmt.executeUpdate() > 0) {
                return 1;
            }

            throw new RegistroNaoEncontradoException("Registro de administrador não encontrado para atualização.");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de administrador com esses dados.", e);
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Referência inválida na atualização.", e);
            }

            throw new DaoException("Erro ao atualizar administrador.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Confere a senha atual antes de gravar o hash da nova senha. */
    public int atualizarSenha(String email, String senhaAntiga, String senhaNova) {
        String sqlBuscar = "SELECT senha FROM administrador_geral WHERE email = ?";
        String sqlAtualizar = "UPDATE administrador_geral SET senha = ? WHERE email = ?";

        try (Connection conn = Conexao.conectar()) {
            // buscar hash atual
            String hashAtual;
            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscar)) {
                pstmtBuscar.setString(1, email);
                try (ResultSet rs = pstmtBuscar.executeQuery()) {
                    if (rs.next()) {
                        hashAtual = rs.getString("senha");
                    } else {
                        throw new RegistroNaoEncontradoException(
                                "Administrador não encontrado para alteração de senha.");
                    }
                }
            }

            // conferir se a antiga senha iguala com o hash salvo
            if (!PasswordUtils.verificarSenha(senhaAntiga, hashAtual)) {
                throw new ValidacaoException("A senha atual não confere.");
            }

            // gera novo hash e atualiza
            try (PreparedStatement pstmtAtualizar = conn.prepareStatement(sqlAtualizar)) {
                pstmtAtualizar.setString(1, PasswordUtils.hashSenha(senhaNova));
                pstmtAtualizar.setString(2, email);

                if (pstmtAtualizar.executeUpdate() > 0) {
                    return 1;
                }
                throw new RegistroNaoEncontradoException("Administrador não encontrado para alteração de senha.");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de administrador com esses dados.", e);
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Referência inválida na atualização.", e);
            }

            throw new DaoException("Erro ao atualizar a senha do administrador.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Exclui pelo ID; informa ausência do registro ou vínculos que impedem a exclusão. */
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM administrador_geral WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) {
                return 1;
            }
            throw new RegistroNaoEncontradoException("Registro de administrador não encontrado para exclusão.");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de administrador porque existem registros vinculados.",
                        e);
            }

            throw new DaoException("Erro ao excluir administrador.", e);
        } catch (ClassNotFoundException e) {
            throw new DaoException("Driver do banco não encontrado.", e);
        }
    }

    /** Converte a linha atual do resultado em um objeto do modelo. */
    private static AdministradorGeral mapearAdministradorGeral(ResultSet rs) throws SQLException {
        AdministradorGeral administradorGeral = new AdministradorGeral();
        administradorGeral.setAdm_geral_id(rs.getLong("id_adm_geral"));
        administradorGeral.setNome(rs.getString("nome"));
        administradorGeral.setEmail(rs.getString("email"));
        administradorGeral.setTelefone(rs.getString("telefone"));
        administradorGeral.setImagem(rs.getString("imagem"));
        return administradorGeral;
    }
}
