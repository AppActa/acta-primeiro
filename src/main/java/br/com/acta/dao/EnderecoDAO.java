package br.com.acta.dao;

import br.com.acta.model.Endereco;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnderecoDAO implements MetodosCrud<Endereco> {

    // INSERT
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
    public Endereco buscar(Long id) {
        String sql = "SELECT * FROM endereco WHERE id_endereco = ?;";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Endereco> buscar() {
        List<Endereco> lista = new ArrayList<>();
        String sql = "SELECT * FROM endereco;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearEndereco(rs));
            }
            return lista;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE
    @Override
    public int atualizar(Endereco endereco) {
        String sql = "UPDATE endereco SET rua = ?, bairro = ?, cidade = ?, estado = ?, cep = ?, numero = ?, complemento = ?, unidade = ?, id_empresa = ? WHERE id_endereco = ?;";

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

    // DELETE
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM endereco WHERE id_endereco = ?;";

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

    // SQLState "23xxx" = violação de integridade (FK, NOT NULL, duplicidade).
    private static int codigoDeErro(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return 0;
        }
        return -1;
    }

    // Mapear endereco para simplificar a busca
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
        endereco.setId_empresa(rs.getLong("id_empresa"));
        return endereco;
    }

}