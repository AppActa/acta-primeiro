package br.com.acta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.acta.model.Administrador_geral;
import br.com.acta.utils.Conexao;

public class Administrador_geralDAO implements MetodosCrud<Administrador_geral> {

    //metodos DAO

    //inserir
    @Override
    public int inserir(Administrador_geral administradorGeral) {
        String sql = "INSERT INTO administrador_geral (nome, senha, email, telefone)  VALUEs (?,?,?,?)";

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
    public Administrador_geral buscar(int id) {
        String sql = "SELECT adm_geral_id,nome,senha,email,telefone FROM administrador_geral WHERE adm_geral_id = ? ";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1,id);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {

                Administrador_geral administradorGeral = new Administrador_geral();

                administradorGeral.setAdm_geral_id(rs.getInt("adm_geral_id"));
                administradorGeral.setNome(rs.getString("nome"));
                administradorGeral.setSenha(rs.getString("senha"));
                administradorGeral.setEmail(rs.getString("email"));
                administradorGeral.setTelefone(rs.getString("telefone"));

                return administradorGeral;
            }return null;

        } catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    //listar
    @Override
    public List<Administrador_geral> buscar() {
        List<Administrador_geral> administradorGerals = new ArrayList<>();
        String sql = "SELECT adm_geral_id,nome,senha,email,telefone FROM administrador_geral";

        try(Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()){
                Administrador_geral administradorGeral = new Administrador_geral();
                administradorGeral.setAdm_geral_id(rs.getInt("adm_geral_id"));
                administradorGeral.setNome(rs.getString("nome"));
                administradorGeral.setSenha(rs.getString("senha"));
                administradorGeral.setEmail(rs.getString("email"));
                administradorGeral.setTelefone(rs.getString("telefone"));

                administradorGerals.add(administradorGeral);
            }
            return administradorGerals;
        } catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

    }

    //atualizar
    @Override
    public int atualizar(Administrador_geral administradorGeral) {
        String sql = "UPDATE administrador_geral SET nome = ?, senha = ?, email = ?, telefone = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2, administradorGeral.getSenha());
            pstmt.setString(3,administradorGeral.getEmail());
            pstmt.setString(4, administradorGeral.getTelefone());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

    }

    //excluir

    @Override
    public int excluir(int id) {
        String sql = "DELETE FROM administrador_geral WHERE adm_geral_id = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1,id);

            if(pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch(SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
