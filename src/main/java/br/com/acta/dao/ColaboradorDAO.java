package br.com.acta.dao;

import br.com.acta.model.Colaborador;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDAO implements MetodosCrud<Colaborador> {

    // INSERT
    @Override
    public int inserir(Colaborador colaborador) {
        String sql = """
                INSERT INTO colaborador (nome, sobrenome, permissao_gestor, area, cargo, dt_contratacao, email, senha, telefone, cpf, id_empresa)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, colaborador.getNome());
            pstmt.setString(2, colaborador.getSobrenome());
            pstmt.setBoolean(3, colaborador.getPermissao_gestor());
            pstmt.setString(4, colaborador.getArea());
            pstmt.setString(5, colaborador.getCargo());
            pstmt.setDate(6, colaborador.getDt_contratacao());
            pstmt.setString(7, colaborador.getEmail());
            pstmt.setString(8, colaborador.getSenha());
            pstmt.setString(9, colaborador.getTelefone());
            pstmt.setString(10, colaborador.getCpf());
            pstmt.setLong(11, colaborador.getId_empresa());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException | ClassNotFoundException e) {
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
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                mapearColaborador(rs);
            } return null;

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
            } return colaboradores;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Colaborador colaborador) {
        String sql = "UPDATE colaborador SET nome = ?, sobrenome = ?, permissao_gestor = ?, area = ?, cargo = ?, dt_contratacao = ?, email = ?, senha = ?, telefone = ?, cpf = ?, id_empresa = ? WHERE id_colaborador = ?;";

        try (Connection conn = Conexao.conectar();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, colaborador.getNome());
            pstmt.setString(2, colaborador.getSobrenome());
            pstmt.setBoolean(3, colaborador.getPermissao_gestor());
            pstmt.setString(4, colaborador.getArea());
            pstmt.setString(5, colaborador.getCargo());
            pstmt.setDate(6, colaborador.getDt_contratacao());
            pstmt.setString(7, colaborador.getEmail());
            pstmt.setString(8, colaborador.getSenha());
            pstmt.setString(9, colaborador.getTelefone());
            pstmt.setString(10, colaborador.getCpf());
            pstmt.setLong(11, colaborador.getId_empresa());
            pstmt.setLong(12, colaborador.getId_colaborador());

            if (pstmt.executeUpdate() > 0) return 1;
            return 0;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM colaborador WHERE id_colaborador = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static Colaborador mapearColaborador(ResultSet rs) throws SQLException {
        Colaborador colaborador = new Colaborador();
        colaborador.setId_colaborador(rs.getLong("id_colaborador"));
        colaborador.setNome(rs.getString("nome"));
        colaborador.setSobrenome(rs.getString("sobrenome"));
        colaborador.setPermissao_gestor(rs.getBoolean("permissao_gestor"));
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
