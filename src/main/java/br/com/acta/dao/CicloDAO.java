package br.com.acta.dao;

import br.com.acta.model.Ciclo;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Situacao;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class CicloDAO implements MetodosCrud<Ciclo> {

    @Override
    public int inserir(Ciclo ciclo) {
        String sql = "INSERT INTO ciclo (nome, descricao, etapa_atual, dt_inicio, dt_fim, status, id_empresa, id_responsavel) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // criado_em NÃO entra aqui, o banco preenche sozinho com now()

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ciclo.getNome());
            pstmt.setString(2, ciclo.getDescricao());
            pstmt.setString(3, ciclo.getEtapa_atual().name());
            pstmt.setDate(4, ciclo.getDt_inicio());
            pstmt.setDate(5, ciclo.getDt_fim());
            pstmt.setString(6, ciclo.getStatus().name());
            pstmt.setLong(7, ciclo.getId_empresa());
            pstmt.setLong(8, ciclo.getId_responsavel());

            return pstmt.executeUpdate() > 0 ? 1 : 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Ciclo buscar(Long id) {
        String sql = "SELECT * FROM ciclo WHERE id_ciclo = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCiclo(rs);
            }
            return null;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Ciclo> buscar() {
        List<Ciclo> ciclos = new ArrayList<>();
        String sql = "SELECT * FROM ciclo";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ciclos.add(mapearCiclo(rs));
            }
            return ciclos;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int atualizar(Ciclo ciclo) {
        String sql = "UPDATE ciclo SET nome = ?, descricao = ?, etapa_atual = ?, dt_inicio = ?, dt_fim = ?, status = ?, id_empresa = ?, id_responsavel = ? " +
                "WHERE id_ciclo = ?";
        //em nunca é atilizado porque o banco passa direto quando entra para data e local;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ciclo.getNome());
            pstmt.setString(2, ciclo.getDescricao());
            pstmt.setString(3, ciclo.getEtapa_atual().name());
            pstmt.setDate(4, ciclo.getDt_inicio());
            pstmt.setDate(5, ciclo.getDt_fim());
            pstmt.setString(6, ciclo.getStatus().name());
            pstmt.setLong(7, ciclo.getId_empresa());
            pstmt.setLong(8, ciclo.getId_responsavel());
            pstmt.setLong(9, ciclo.getId_ciclo());

            return pstmt.executeUpdate() > 0 ? 1 : 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM ciclo WHERE id_ciclo = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            return pstmt.executeUpdate() > 0 ? 1 : 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static Ciclo mapearCiclo(ResultSet rs) throws SQLException {
        Ciclo ciclo = new Ciclo();
        ciclo.setId_ciclo(rs.getLong("id_ciclo"));
        ciclo.setNome(rs.getString("nome"));
        ciclo.setDescricao(rs.getString("descricao"));
        ciclo.setEtapa_atual(EtapaCiclo.valueOf(rs.getString("etapa_atual")));
        ciclo.setDt_inicio(rs.getDate("dt_inicio"));
        ciclo.setDt_fim(rs.getDate("dt_fim"));
        ciclo.setStatus(Situacao.valueOf(rs.getString("status")));
        ciclo.setCriado_em(rs.getObject("criado_em", OffsetDateTime.class));
        ciclo.setId_empresa(rs.getLong("id_empresa"));
        ciclo.setId_responsavel(rs.getLong("id_responsavel"));
        return ciclo;
    }
}