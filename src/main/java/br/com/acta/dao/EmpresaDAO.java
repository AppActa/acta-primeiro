
package br.com.acta.dao;

import java.util.List;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import br.com.acta.model.Empresa;
import br.com.acta.utils.Conexao;
public class EmpresaDAO implements MetodosCrud<Empresa> {

    //INSERT
    @Override
    public int inserir(Empresa empresa) {

        String sql = "INSERT INTO empresa (nome,setor,cnpj,status,tamanho) VALUES (?,?,?,?,?)";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,empresa.getNome());
            pstmt.setString(2,empresa.getSetor());
            pstmt.setString(3,empresa.getCnpj());
            pstmt.setString(4, empresa.getStatus().name());
            pstmt.setString(5,empresa.getTamanho().name());

            return pstmt.executeUpdate() > 0 ? 1 : 0;

        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Empresa buscar(Long id) {

        String sql = "SELECT * FROM empresa WHERE id_empresa = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()){

            pstmt.setLong(1,id);


            if(rs.next()) {return mapearEmpresa(rs);
            }
            return null;
        }catch(SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    @Override
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

    @Override
    public int atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome = ?, setor = ?, cnpj = ?, status = ?, tamanho = ? WHERE id_empresa = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNome());
            pstmt.setString(2, empresa.getSetor());
            pstmt.setString(3, empresa.getCnpj());
            pstmt.setString(4, empresa.getStatus().name());
            pstmt.setString(5, empresa.getTamanho().name());

            //localizando o id
            pstmt.setLong(6, empresa.getId_empresa());

            if (pstmt.executeUpdate() > 0) return 1;

            return 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setLong(1,id);

            if(pstmt.executeUpdate() > 0) return 1;
            else return 0;
        }catch (SQLException | ClassNotFoundException e){
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
        empresa.setTamanho(TamanhoEmpresa.valueOf(rs.getString("tamanho")));

        return empresa;
    }

}
