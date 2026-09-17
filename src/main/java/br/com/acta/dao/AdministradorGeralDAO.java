package br.com.acta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.Conexao;

public class AdministradorGeralDAO implements MetodosCrud<AdministradorGeral> {

    //metodos DAO

    //inserir
    @Override
    public int inserir(AdministradorGeral administradorGeral) {
        String sql = "INSERT INTO administrador_geral (nome, senha, email, telefone)  VALUES (?,?,?,?)";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2,administradorGeral.getSenha());
            pstmt.setString(3,administradorGeral.getEmail());
            pstmt.setString(4, administradorGeral.getTelefone());

            return pstmt.executeUpdate() > 0 ? 1 : 0;
        } catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return -1;
        }

    }

    //buscar com id
    @Override
    public AdministradorGeral buscar(Long id) {
        String sql = "SELECT * FROM administrador_geral WHERE id_adm_geral = ? ";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setLong(1,id);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                mapearAdministradorGeral(rs);
            }return null;

        } catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    //listar
    @Override
    public List<AdministradorGeral> buscar() {
        List<AdministradorGeral> administradoresGerais = new ArrayList<>();
        String sql = "SELECT * FROM administrador_geral";

        try(Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()){
                AdministradorGeral administradorGeral = mapearAdministradorGeral(rs);
                administradoresGerais.add(administradorGeral);
            }
            return administradoresGerais;
        } catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

    }

    //atualizar
    @Override
    public int atualizar(AdministradorGeral administradorGeral) {
        String sql = "UPDATE administrador_geral SET nome = ?, senha = ?, email = ?, telefone = ? WHERE id_adm_geral = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2, administradorGeral.getSenha());
            pstmt.setString(3,administradorGeral.getEmail());
            pstmt.setString(4, administradorGeral.getTelefone());

            pstmt.setLong(5,administradorGeral.getAdm_geral_id());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch(SQLException | ClassNotFoundException e){
            return -1;
        }

    }

    //excluir

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM administrador_geral WHERE id_adm_geral = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1,id);

            if(pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch(SQLException | ClassNotFoundException e) {
            return -1;
        }
    }

    private static AdministradorGeral mapearAdministradorGeral(ResultSet rs) throws SQLException {
        AdministradorGeral administradorGeral = new AdministradorGeral();
        administradorGeral.setAdm_geral_id(rs.getLong("id_adm_geral"));
        administradorGeral.setNome(rs.getString("nome"));
        administradorGeral.setSenha(rs.getString("senha"));
        administradorGeral.setEmail(rs.getString("email"));
        administradorGeral.setTelefone(rs.getString("telefone"));
        return administradorGeral;
    }
}
