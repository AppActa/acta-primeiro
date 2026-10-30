package br.com.acta.dao;

import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;
import br.com.acta.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColaboradorDAO implements MetodosCrud<Colaborador> {

    // INSERT
    @Override
    public int inserir(Colaborador colaborador) {
        String sql = """
                INSERT INTO colaborador (nome, sobrenome, permissao_gestor, status, area, cargo, dt_contratacao, email, senha, telefone, cpf, id_empresa)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, colaborador.getNome());
            pstmt.setString(2, colaborador.getSobrenome());
            pstmt.setBoolean(3, colaborador.getPermissao_gestor());
            pstmt.setObject(4, colaborador.getStatus().name(), Types.OTHER);
            pstmt.setString(5, colaborador.getArea());
            pstmt.setString(6, colaborador.getCargo());
            pstmt.setDate(7, colaborador.getDt_contratacao());
            pstmt.setString(8, colaborador.getEmail());
            pstmt.setString(9, PasswordUtils.hashSenha(colaborador.getSenha()));
            pstmt.setString(10, colaborador.getTelefone());
            pstmt.setString(11, colaborador.getCpf());
            pstmt.setLong(12, colaborador.getId_empresa());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de colaborador com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar colaborador.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    // READ
    @Override
    public Colaborador buscar(Long id) {
        String sql = "SELECT * FROM vw_colaborador WHERE id_colaborador = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearColaborador(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public List<Colaborador> buscar() {
        List<Colaborador> colaboradores = new ArrayList<>();
        String sql = "SELECT * FROM vw_colaborador;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                colaboradores.add(mapearColaborador(rs));
            }
            return colaboradores;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<Colaborador> buscaFiltrada(String busca, Status status, String area, String cargo, Boolean permissaoGestor, Date dataInicial, Date dataFinal, String ordem, String direcao) {
        List<Colaborador> colaboradores = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_colaborador WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temStatus = status != null;
        boolean temArea = area != null && !area.isBlank();
        boolean temCargo = cargo != null && !cargo.isBlank();
        boolean temGestor = permissaoGestor != null;
        boolean temDataInicial = dataInicial != null;
        boolean temDataFinal = dataFinal != null;

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (nome ILIKE ? OR sobrenome ILIKE ? OR CONCAT_WS(' ', nome, sobrenome) ILIKE ? OR email ILIKE ? OR nome_empresa ILIKE ? OR cpf ILIKE ? OR telefone ILIKE ?)");
        if (temStatus) sql.append(" AND status = ?");
        if (temArea) sql.append(" AND area = ?");
        if (temCargo) sql.append(" AND cargo = ?");
        if (temGestor) sql.append(" AND permissao_gestor = ?");
        if (temDataInicial) sql.append(" AND dt_contratacao >= ?");
        if (temDataFinal) sql.append(" AND dt_contratacao <= ?");
        if (temOrdem) {
            String direcaoSQL = "DESC".equalsIgnoreCase(direcao) ? " DESC" : " ASC";
            sql.append(" ORDER BY ").append(colunaOrdem).append(direcaoSQL);
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                String documento = busca.trim();
                if (documento.matches("[0-9.()\\s-]+")) {
                    String limpo = documento.replaceAll("\\D", "");
                    if (!limpo.isEmpty()) documento = limpo;
                }
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
                pstmt.setString(i++, "%" + documento + "%");
                pstmt.setString(i++, "%" + documento + "%");
            }
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temArea) pstmt.setString(i++, area);
            if (temCargo) pstmt.setString(i++, cargo);
            if (temGestor) pstmt.setBoolean(i++, permissaoGestor);
            if (temDataInicial) pstmt.setDate(i++, dataInicial);
            if (temDataFinal) pstmt.setDate(i++, dataFinal);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    colaboradores.add(mapearColaborador(rs));
                }
            } return colaboradores;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<String> buscarAreas() {
        List<String> areas = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM colaborador ORDER BY area;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                areas.add(rs.getString("area"));
            }
            return areas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<String> buscarCargos() {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM colaborador ORDER BY cargo;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cargos.add(rs.getString("cargo"));
            }
            return cargos;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Colaborador colaborador) {
        String sql = "UPDATE colaborador SET nome = ?, sobrenome = ?, permissao_gestor = ?, status = ?, area = ?, cargo = ?, dt_contratacao = ?, email = ?, telefone = ?, cpf = ?, id_empresa = ? WHERE id_colaborador = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, colaborador.getNome());
            pstmt.setString(2, colaborador.getSobrenome());
            pstmt.setBoolean(3, colaborador.getPermissao_gestor());
            pstmt.setObject(4, colaborador.getStatus().name(), Types.OTHER);
            pstmt.setString(5, colaborador.getArea());
            pstmt.setString(6, colaborador.getCargo());
            pstmt.setDate(7, colaborador.getDt_contratacao());
            pstmt.setString(8, colaborador.getEmail());
            pstmt.setString(9, colaborador.getTelefone());
            pstmt.setString(10, colaborador.getCpf());
            pstmt.setLong(11, colaborador.getId_empresa());
            pstmt.setLong(12, colaborador.getId_colaborador());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de colaborador não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de colaborador com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar colaborador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    /**
     * Atualiza os dados de um colaborador localizando o registro pelo CPF atual.
     * O parâmetro cpfAtual identifica o registro antes da alteração; o CPF novo
     * vem de colaborador.getCpf().
     */
    public int atualizarPorCpf(Colaborador colaborador, String cpfAtual) {
        String sql = "UPDATE colaborador SET nome = ?, sobrenome = ?, permissao_gestor = ?, status = ?, area = ?, cargo = ?, dt_contratacao = ?, email = ?, telefone = ?, cpf = ?, id_empresa = ? WHERE cpf = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, colaborador.getNome());
            pstmt.setString(2, colaborador.getSobrenome());
            pstmt.setBoolean(3, colaborador.getPermissao_gestor());
            pstmt.setObject(4, colaborador.getStatus().name(), Types.OTHER);
            pstmt.setString(5, colaborador.getArea());
            pstmt.setString(6, colaborador.getCargo());
            pstmt.setDate(7, colaborador.getDt_contratacao());
            pstmt.setString(8, colaborador.getEmail());
            pstmt.setString(9, colaborador.getTelefone());
            pstmt.setString(10, colaborador.getCpf());
            pstmt.setLong(11, colaborador.getId_empresa());
            pstmt.setString(12, cpfAtual);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException(
                    "Colaborador não encontrado para atualização pelo CPF informado."
            );

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um colaborador com esse e-mail ou CPF.", e
                );
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização do colaborador.", e
                );
            }
            throw new DaoException("Erro ao atualizar colaborador pelo CPF.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    // DELETE
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM colaborador WHERE id_colaborador = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de colaborador não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de colaborador porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir colaborador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    /** Exclui um colaborador usando o e-mail único como identificador. */
    public int excluirPorEmail(String email) {
        String sql = "DELETE FROM colaborador WHERE email = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException(
                    "Colaborador não encontrado para exclusão pelo e-mail informado."
            );

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o colaborador porque existem registros vinculados.", e
                );
            }
            throw new DaoException("Erro ao excluir colaborador pelo e-mail.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    // PASSWORD
    public Colaborador autenticar(String email, String senha) {
        String sql = "SELECT * FROM vw_colaborador WHERE email = ?;";
        String sqlSenha = "SELECT senha FROM colaborador WHERE email = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             PreparedStatement pstmtSenha = conn.prepareStatement(sqlSenha)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Colaborador colaborador = mapearColaborador(rs);

                    pstmtSenha.setString(1, email);

                    try (ResultSet rsSenha = pstmtSenha.executeQuery()) {
                        if (rsSenha.next()) {
                            String senhaBanco = rsSenha.getString("senha");

                            if (PasswordUtils.verificarSenha(senha, senhaBanco)) {
                                return colaborador;
                            }
                        }
                    }
                }
                return null;
            }

        } catch (Exception e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public int atualizarSenha(String email, String senhaAntiga, String senhaNova) {
        String sqlBuscar = "SELECT senha FROM colaborador WHERE email = ?;";
        String sqlAtualizar = "UPDATE colaborador SET senha = ? WHERE email = ?;";

        try (Connection conn = Conexao.conectar()) {
            // Buscar hash atual
            String hashAtual;
            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscar)) {
                pstmtBuscar.setString(1, email);
                try (ResultSet rs = pstmtBuscar.executeQuery()) {
                    if (rs.next()) {
                        hashAtual = rs.getString("senha");
                    } else {
                        return 0; // Usuário não encontrado
                    }
                }
            }

            // Conferir se a senha antiga bate com o hash salvo
            if (!PasswordUtils.verificarSenha(senhaAntiga, hashAtual)) {
                return 0;
            }

            // Gera novo hash e atualiza
            try (PreparedStatement pstmtAtualizar = conn.prepareStatement(sqlAtualizar)) {
                pstmtAtualizar.setString(1, PasswordUtils.hashSenha(senhaNova));
                pstmtAtualizar.setString(2, email);

                if (pstmtAtualizar.executeUpdate() > 0) return 1;
                else return 0;
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de colaborador com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar a senha do colaborador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    // UTILITÁRIOS

    // SQLState "23xxx" = violação de integridade (e-mail/CPF duplicado, FK, NOT NULL).

    // Mapear colaborador para simplificar busca
    private static Colaborador mapearColaborador(ResultSet rs) throws SQLException {
        Colaborador colaborador = new Colaborador();
        colaborador.setId_colaborador(rs.getLong("id_colaborador"));
        colaborador.setNome(rs.getString("nome"));
        colaborador.setSobrenome(rs.getString("sobrenome"));
        colaborador.setPermissao_gestor(rs.getBoolean("permissao_gestor"));
        colaborador.setStatus(Status.valueOf(rs.getString("status")));
        colaborador.setArea(rs.getString("area"));
        colaborador.setCargo(rs.getString("cargo"));
        colaborador.setDt_contratacao(rs.getDate("dt_contratacao"));
        colaborador.setEmail(rs.getString("email"));
        colaborador.setTelefone(rs.getString("telefone"));
        colaborador.setCpf(rs.getString("cpf"));
        colaborador.setNome_empresa(rs.getString("nome_empresa"));
        colaborador.setId_empresa(rs.getLong("id_empresa"));
        return colaborador;
    }

    // Whitelist de ordenação
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_colaborador",
            "nome", "nome",
            "sobrenome", "sobrenome",
            "email", "email",
            "area", "area",
            "cargo", "cargo",
            "status", "status",
            "permissao", "permissao_gestor",
            "contratacao", "dt_contratacao",
            "empresa", "nome_empresa"
    );

    // Identifica áreas repetidas
    public String buscarAreaExistente(String area) {
        String sql = "SELECT area FROM colaborador WHERE LOWER(area) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, area);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("area");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public String buscarCargoExistente(String cargo) {
        String sql = "SELECT cargo FROM colaborador WHERE LOWER(cargo) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cargo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("cargo");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
