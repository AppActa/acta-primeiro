
package br.com.acta.dao;

import java.util.List;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.acta.model.Empresa;
import br.com.acta.model.Status;
import br.com.acta.utils.Conexao;
public class EmpresaDAO {


    public int inserir(Empresa empresa) {

        String sql = "INSERT INTO empresa (nome,setor,cnpj,status,tamanho) VALUES (?,?,?,?,?)";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,empresa.getNome());
            pstmt.setString(2,empresa.getSetor());
            pstmt.setString(3,empresa.getCnpj());
            pstmt.setString(4,String.valueOf(empresa.getStatus()));
            pstmt.setString(5,empresa.getTamanho());

            return pstmt.executeUpdate() > 0 ? 1 : 0;

        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public Object buscar(Long id) {

        String sql = "SELECT * FROM empresa WHERE id_empresa = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setLong(1,id);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                mapearEmpresa(rs);
            }return null;
        }catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public List<Empresa> buscar() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresa";

        try(Connection conn = Conexao.conectar();
        Statement pstmt = conn.createStatement();
        ResultSet rs = pstmt.executeQuery(sql)){

            while (rs.next()){
                Empresa empresa = mapearEmpresa(rs);
                empresas.add(empresa);
            }
            return empresas;
        }catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

    }

    public int atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome = ?, setor = ?, cnpj = ?, status = ?, tamanho = ? WHERE id_empresa = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, empresa.getNome());
            pstmt.setString(2, empresa.getSetor());
            pstmt.setString(3, empresa.getCnpj());
            pstmt.setString(4,String.valueOf(empresa.getStatus()));
            pstmt.setString(5, empresa.getTamanho());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        }catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return -1;
        }

    }

    public int excluir(Long id) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1,id);

            if(pstmt.executeUpdate() > 0) return 1;
            else return 0;

        }catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            return -1;
        }

    }

    private static Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setId_empresa(rs.getLong("id_empresa"));
        empresa.setNome(rs.getString("nome"));
        empresa.setSetor(rs.getString("setor"));
        empresa.setCnpj(rs.getString("cnpj"));
        empresa.setStatus(Status.valueOf(rs.getString("status")));
        empresa.setTamanho(rs.getString("tamanho"));

        return empresa;
    }

}
