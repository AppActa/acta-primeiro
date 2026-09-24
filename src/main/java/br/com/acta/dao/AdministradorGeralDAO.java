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
import br.com.acta.utils.PasswordUtils;
import com.password4j.Password;

public class AdministradorGeralDAO  {

    //metodos DAO

    //inserir

    public int inserir(AdministradorGeral administradorGeral) {
        String sql = "INSERT INTO administrador_geral (nome, senha, email, telefone)  VALUES (?,?,?,?)";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2,PasswordUtils.hashSenha(administradorGeral.getSenha()));
            pstmt.setString(3,administradorGeral.getEmail());
            pstmt.setString(4,administradorGeral.getTelefone());

            return pstmt.executeUpdate() > 0 ? 1 : 0;
        } catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return -1;
        }

    }

    //buscar com id

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

    //autenticacao
    public AdministradorGeral autenticar(String email, String senha) {
        String sql = "SELECT * FROM administrador_geral WHERE email = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                AdministradorGeral adm = mapearAdministradorGeral(rs);
                if(PasswordUtils.verificarSenha(adm.getSenha(), senha)) {
                    return adm;//login valido
                }
            }
            return null;//email não existe ou senha errada
        }catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    //atualizar

    public int atualizar(AdministradorGeral administradorGeral) {
        String sql = "UPDATE administrador_geral SET nome = ?, email = ?, telefone = ? WHERE id_adm_geral = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,administradorGeral.getNome());
            pstmt.setString(2,administradorGeral.getEmail());
            pstmt.setString(3, administradorGeral.getTelefone());
            pstmt.setLong(4,administradorGeral.getAdm_geral_id());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch(SQLException | ClassNotFoundException e){
            return -1;
        }
    }

    public int atualizarSenha(String email, String senhaAntiga, String senhaNova){
        String sqlBuscar = "SELECT senha FROM administrador_geral WHERE email = ?";
        String sqlAtualizar = "UPDATE administrador_geral SET senha = ? WHERE email = ?";

        try (Connection conn = Conexao.conectar()){
            //buscar hash atual
            String hashAtual = null;
            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscar)){
                pstmtBuscar.setString(1, email);
                ResultSet rs = pstmtBuscar.executeQuery();
                if (rs.next()) {
                    hashAtual = rs.getString("senha");
                }else{
                    return -1;//usuario não encontrado
                }
            }

            //conferir se a antiga senha iguala com o hash salvo
            if(!PasswordUtils.verificarSenha(senhaAntiga,hashAtual)){
                return 0;
            }

            //gera novo hash e atualiza
            try(PreparedStatement pstmtAtualizar = conn.prepareStatement(sqlAtualizar)){
                pstmtAtualizar.setString(1, PasswordUtils.hashSenha(senhaNova));
                pstmtAtualizar.setString(2, email);
                return pstmtAtualizar.executeUpdate() > 0 ? 1 : 0;
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return -1;
        }
    }

    //excluir

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
