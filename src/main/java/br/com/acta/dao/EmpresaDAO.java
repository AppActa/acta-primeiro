
package br.com.acta.dao;

import java.util.List;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import br.com.acta.model.Empresa;
import br.com.acta.utils.Conexao;
public class EmpresaDAO implements MetodosCrud<Empresa> {

    //INSERT
    @Override
    public int inserir(Empresa empresa) {

        String sql = """
                INSERT INTO empresa (nome, setor, cnpj, status, tamanho)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection conn = Conexao.conectar();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1,empresa.getNome());
            pstmt.setString(2,empresa.getSetor());
            pstmt.setString(3,empresa.getCnpj());
            pstmt.setString(4, empresa.getStatus().name());
            pstmt.setString(5,empresa.getTamanho().name());

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

    @Override
    public Empresa buscar(Long id) {
        String sql = "SELECT * FROM empresa WHERE id_empresa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Empresa> buscar() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresa";

        try (Connection conn = Conexao.conectar();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()){
                empresas.add(mapearEmpresa(rs));
            }
            return empresas;

        } catch(SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Empresa> buscaFiltrada(String busca, String setor, Status status, TamanhoEmpresa tamanho, String ordem, String direcao) {
        List<Empresa> empresas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM empresa WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temSetor = setor != null && !busca.isBlank();
        boolean temStatus = status != null;
        boolean temTamanho = tamanho != null;
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND nome ILIKE ?");
        if (temSetor) sql.append(" AND setor ILIKE ?");
        if (temStatus) sql.append(" AND status = ?");
        if  (temTamanho) sql.append(" AND tamanho = ?");
        if (temOrdem) {
            String direcaoSQL = "DESC".equalsIgnoreCase(direcao) ? " DESC" : " ASC";
            sql.append(" ORDER BY ").append(colunaOrdem).append(direcaoSQL);
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                pstmt.setString(i++, termo);
                pstmt.setString(i++, termo);
            }
            if (temStatus) pstmt.setString(i++, status.name());
            if (temTamanho) pstmt.setString(i++, tamanho.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    empresas.add(mapearEmpresa(rs));
                }
            } return empresas;

        } catch(SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome = ?, setor = ?, cnpj = ?, status = ?, tamanho = ? WHERE id_empresa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNome());
            pstmt.setString(2, empresa.getSetor());
            pstmt.setString(3, empresa.getCnpj());
            pstmt.setString(4, empresa.getStatus().name());
            pstmt.setString(5, empresa.getTamanho().name());
            pstmt.setLong(6, empresa.getId_empresa()); //localizando o id

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

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?;";

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

    private static int codigoDeErro(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return 0;
        }
        return -1;
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

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "nome", "nome",
            "setor", "setor",
            "status", "status",
            "tamanho", "tamanho"
    );
}