package br.com.acta.dao;

import br.com.acta.model.PlanoAcao;
import br.com.acta.enums.Situacao;
import br.com.acta.enums.Prioridade;
import br.com.acta.utils.Conexao;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PlanoAcaoDAO implements MetodosCrud<PlanoAcao> {

    //INSERT
    @Override
    public int inserir(PlanoAcao planoAcao) {
        String sql = "INSERT INTO plano_acao (nome,descricao,status,prioridade,id_ciclo,id_criador) VALUES (?,?,?,?,?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, planoAcao.getNome());
            pstmt.setString(2, planoAcao.getDescricao());
            pstmt.setString(3, planoAcao.getStatus().name());
            pstmt.setString(4, planoAcao.getPrioridade().name());
            pstmt.setLong(5, planoAcao.getId_ciclo());
            pstmt.setLong(6, planoAcao.getId_criador());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException | ClassNotFoundException e) {
            return -1;
        }
    }

    @Override
    public PlanoAcao buscar(Long id) {
        String sql = "SELECT * FROM plano_acao WHERE id_plano_acao = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()){

            pstmt.setLong(1,id);

            if(rs.next()) {
                return mapearPlanoAcao(rs);
            } return null;

        }catch(SQLException | ClassNotFoundException e){
            throw  new RuntimeException(e);
        }

    }

    @Override
    public List<PlanoAcao> buscar() {
        List<PlanoAcao> planosAcoes = new ArrayList<>();
        String sql = "SELECT * FROM plano_acao";

        try(Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()) {
                planosAcoes.add(mapearPlanoAcao(rs));
            }return planosAcoes;

        } catch(SQLException | ClassNotFoundException e ) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int atualizar(PlanoAcao planoAcao) {

        String sql = "UPDATE plano_acao SET nome = ?, descricao = ?, status = ?, prioridade = ?, id_ciclo = ?, id_criador = ? WHERE id_plano_acao = ?";

        try(Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, planoAcao.getNome());
            pstmt.setString(2, planoAcao.getDescricao());
            pstmt.setString(3, planoAcao.getStatus().name());
            pstmt.setString(4, planoAcao.getPrioridade().name());
            pstmt.setLong(5, planoAcao.getId_ciclo());
            pstmt.setLong(6, planoAcao.getId_criador());
            pstmt.setLong(7,planoAcao.getId_plano_acao());

            if (pstmt.executeUpdate() > 0) return 1;
            return 0;
        }catch (SQLException | ClassNotFoundException e){
            return -1;
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM plano_acao WHERE id_plano_acao = ?";

        try(Connection conn = Conexao.conectar();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1,id);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException | ClassNotFoundException e) {
            return -1;
        }

    }


    private static PlanoAcao mapearPlanoAcao(ResultSet rs) throws SQLException{

        PlanoAcao planoAcao = new PlanoAcao();
        planoAcao.setId_plano_acao((rs.getLong("id_plano_acao")));
        planoAcao.setNome(rs.getString("nome"));
        planoAcao.setDescricao(rs.getString("descricao"));
        planoAcao.setStatus(Situacao.valueOf(rs.getString("status")));
        planoAcao.setPrioridade(Prioridade.valueOf(rs.getString("prioridade")));
        planoAcao.setId_ciclo((rs.getLong("id_ciclo")));
        planoAcao.setId_criador(rs.getLong("id_criador"));

        return planoAcao;
    }
}
