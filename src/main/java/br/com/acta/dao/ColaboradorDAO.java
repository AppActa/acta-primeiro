package br.com.acta.dao;

import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
import br.com.acta.utils.Conexao;
import br.com.acta.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            pstmt.setString(4, colaborador.getStatus().name());
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
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // READ
    @Override
    public Colaborador buscar(Long id) {
        String sql = "SELECT * FROM colaborador WHERE id_colaborador = ?;";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Colaborador> buscar() {
        List<Colaborador> colaboradores = new ArrayList<>();
        String sql = "SELECT * FROM colaborador;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                colaboradores.add(mapearColaborador(rs));
            }
            return colaboradores;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Colaborador autenticar(String email, String senha) {
        String sql = "SELECT * FROM colaborador WHERE email = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Colaborador colaborador = mapearColaborador(rs);

                    if (PasswordUtils.verificarSenha(senha, colaborador.getSenha())) {
                        return colaborador;
                    }
                }
                return null;
            }

        } catch (Exception e) {
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
            pstmt.setString(4, colaborador.getStatus().name());
            pstmt.setString(5, colaborador.getArea());
            pstmt.setString(6, colaborador.getCargo());
            pstmt.setDate(7, colaborador.getDt_contratacao());
            pstmt.setString(8, colaborador.getEmail());
            pstmt.setString(9, colaborador.getTelefone());
            pstmt.setString(10, colaborador.getCpf());
            pstmt.setLong(11, colaborador.getId_empresa());
            pstmt.setLong(12, colaborador.getId_colaborador());

            if (pstmt.executeUpdate() > 0) return 1;
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int atualizarSenha(String email, String senhaAntiga, String senhaNova) {
        String sqlBuscar = "SELECT senha FROM colaborador WHERE email = ?;";
        String sqlAtualizar = "UPDATE colaborador SET senha = ? WHERE email = ?;;";

        try (Connection conn = Conexao.conectar()) {
            // buscar hash atual
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

            // conferir se a senha antiga bate com o hash salvo
            if (!PasswordUtils.verificarSenha(senhaAntiga, hashAtual)) {
                return 0;
            }

            // gera novo hash e atualiza
            try (PreparedStatement pstmtAtualizar = conn.prepareStatement(sqlAtualizar)) {
                pstmtAtualizar.setString(1, PasswordUtils.hashSenha(senhaNova));
                pstmtAtualizar.setString(2, email);

                if (pstmtAtualizar.executeUpdate() > 0) return 1;
                else return 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
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
            else return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return codigoDeErro(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // SQLState "23xxx" = violação de integridade (e-mail/CPF duplicado, FK, NOT NULL).
    private static int codigoDeErro(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return 0;
        }
        return -1;
    }

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
        colaborador.setSenha(rs.getString("senha"));
        colaborador.setTelefone(rs.getString("telefone"));
        colaborador.setCpf(rs.getString("cpf"));
        colaborador.setId_empresa(rs.getLong("id_empresa"));
        return colaborador;
    }

}