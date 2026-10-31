package br.com.acta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;
import br.com.acta.utils.PasswordUtils;

public class AdministradorGeralDAO implements MetodosCrud<AdministradorGeral> {

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
                while (rs.next()) empresas.add(rs.getString("nome"));
            }
            return empresas;
        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao consultar as empresas vinculadas ao administrador.", e);
        }
    }

    //metodos DAO

    //inserir
    @Override
    public int inserir(AdministradorGeral administradorGeral) {
        String sql = """
                INSERT INTO administrador_geral (nome, senha, email, telefone)
                VALUES (?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2,PasswordUtils.hashSenha(administradorGeral.getSenha()));
            pstmt.setString(3,administradorGeral.getEmail());
            pstmt.setString(4,administradorGeral.getTelefone());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

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

    //buscar com id
    @Override
    public AdministradorGeral buscar(Long id) {
        String sql = "SELECT id_adm_geral, nome, email, telefone FROM administrador_geral WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1,id);

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

    //listar
    @Override
    public List<AdministradorGeral> buscar() {
        List<AdministradorGeral> administradoresGerais = new ArrayList<>();
        String sql = "SELECT id_adm_geral, nome, email, telefone FROM administrador_geral;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()){
                administradoresGerais.add(mapearAdministradorGeral(rs));
            }
            return administradoresGerais;

        } catch(SQLException | ClassNotFoundException e){
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }

    }

    public List<AdministradorGeral> buscaFiltrada(String busca, String ordem, String direcao) {
        List<AdministradorGeral> administradoresGerais = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM administrador_geral WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (nome ILIKE ? OR email ILIKE ? OR telefone ILIKE ?)");
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

    //autenticacao
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

    //atualizar
    @Override
    public int atualizar(AdministradorGeral administradorGeral) {
        String sql = "UPDATE administrador_geral SET nome = ?, email = ?, telefone = ? WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2,administradorGeral.getEmail());
            pstmt.setString(3, administradorGeral.getTelefone());
            pstmt.setLong(4,administradorGeral.getAdm_geral_id());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de administrador não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de administrador com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar administrador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    public int atualizarSenha(String email, String senhaAntiga, String senhaNova){
        String sqlBuscar = "SELECT senha FROM administrador_geral WHERE email = ?";
        String sqlAtualizar = "UPDATE administrador_geral SET senha = ? WHERE email = ?";

        try (Connection conn = Conexao.conectar()){
            //buscar hash atual
            String hashAtual;
            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscar)) {
                pstmtBuscar.setString(1, email);
                try (ResultSet rs = pstmtBuscar.executeQuery()) {
                    if (rs.next()) {
                        hashAtual = rs.getString("senha");
                    } else {
                        return 0; // usuário não encontrado
                    }
                }
            }

            //conferir se a antiga senha iguala com o hash salvo
            if (!PasswordUtils.verificarSenha(senhaAntiga, hashAtual)) {
                return 0;
            }

            //gera novo hash e atualiza
            try (PreparedStatement pstmtAtualizar = conn.prepareStatement(sqlAtualizar)) {
                pstmtAtualizar.setString(1, PasswordUtils.hashSenha(senhaNova));
                pstmtAtualizar.setString(2, email);

                if (pstmtAtualizar.executeUpdate() > 0) return 1;
                else return 0;
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de administrador com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar a senha do administrador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    //excluir
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM administrador_geral WHERE id_adm_geral = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de administrador não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de administrador porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir administrador.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Driver do banco não encontrado.", e
            );
        }
    }


    private static AdministradorGeral mapearAdministradorGeral(ResultSet rs) throws SQLException {
        AdministradorGeral administradorGeral = new AdministradorGeral();
        administradorGeral.setAdm_geral_id(rs.getLong("id_adm_geral"));
        administradorGeral.setNome(rs.getString("nome"));
        administradorGeral.setEmail(rs.getString("email"));
        administradorGeral.setTelefone(rs.getString("telefone"));
        return administradorGeral;
    }

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "nome", "nome",
            "email", "email",
            "telefone", "telefone"
    );

}
