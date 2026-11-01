
package br.com.acta.dao;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;

import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.model.Empresa;
import br.com.acta.utils.Conexao;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;

/** Executa a persistência de Empresa e converte consultas em objetos do modelo. */
public class EmpresaDAO implements MetodosCrud<Empresa> {

    /** Insere o registro; retorna 1 se houve inclusão ou 0 se nenhuma linha foi afetada. */
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
            pstmt.setObject(4, empresa.getStatus().name(), Types.OTHER);
            pstmt.setObject(5,empresa.getTamanho().name(), Types.OTHER);

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            // SQLState 23505 indica duplicidade; 23503 indica uma referência inválida.
            if ("23505".equals(e.getSQLState())){
                throw new DuplicidadeException("Já existe um registro com esse dados.",e);
            }
            if ("23503".equals(e.getSQLState())){
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.",e);
            }
            throw new DaoException("Erro ao cadrastrar empresa.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Driver do banco de dados não encontrado.", e);
        }
    }

    /** Busca pelo ID; retorna null quando o registro não existe. */
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

    /** Lista todos os registros disponíveis na consulta. */
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

    /** Aplica apenas os filtros preenchidos e a ordenação permitida. */
    public List<Empresa> buscaFiltrada(String busca, String setor, Status status, TamanhoEmpresa tamanho, String ordem, String direcao) {
        List<Empresa> empresas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM empresa WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temSetor = setor != null && !setor.isBlank();
        boolean temStatus = status != null;
        boolean temTamanho = tamanho != null;
        // A coluna vem de uma lista permitida, nunca diretamente do formulário.
        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        // Acrescenta condições apenas para os filtros presentes.
        if (temBusca) sql.append(" AND (nome ILIKE ? OR cnpj ILIKE ?)");
        if (temSetor) sql.append(" AND setor ILIKE ?");
        if (temStatus) sql.append(" AND status = ?");
        if  (temTamanho) sql.append(" AND tamanho = ?");
        if (temOrdem) {
            String direcaoSQL = "DESC".equalsIgnoreCase(direcao) ? " DESC" : " ASC";
            sql.append(" ORDER BY ").append(colunaOrdem).append(direcaoSQL);
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Os parâmetros seguem a mesma ordem das condições adicionadas ao SQL.
            int i = 1;
            if (temBusca) {
                String termo = "%" + busca.trim() + "%";
                String documento = busca.trim();
                if (documento.matches("[A-Za-z0-9./\\s-]+")) {
                    String limpo = documento.replaceAll("[^A-Za-z0-9]", "").toUpperCase(java.util.Locale.ROOT);
                    if (!limpo.isEmpty()) documento = limpo;
                }
                pstmt.setString(i++, termo);
                pstmt.setString(i++, "%" + documento + "%");
            }
            if (temSetor) pstmt.setString(i++, setor);
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temTamanho) pstmt.setObject(i++, tamanho.name(), Types.OTHER);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    empresas.add(mapearEmpresa(rs));
                }
            } return empresas;

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Lista os setores cadastrados, sem repetição e em ordem alfabética. */
    public List<String> buscarSetor() {
        List<String> setores = new ArrayList<>();
        String sql = "SELECT DISTINCT setor FROM empresa ORDER BY setor;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                setores.add(rs.getString("setor"));
            }
            return setores;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados",e);
        }
    }

    /** Atualiza pelo ID; informa quando nenhum registro foi encontrado. */
    @Override
    public int atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome = ?, setor = ?, cnpj = ?, status = ?, tamanho = ? WHERE id_empresa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNome());
            pstmt.setString(2, empresa.getSetor());
            pstmt.setString(3, empresa.getCnpj());
            pstmt.setObject(4, empresa.getStatus().name(), Types.OTHER);
            pstmt.setObject(5, empresa.getTamanho().name(), Types.OTHER);
            pstmt.setLong(6, empresa.getId_empresa()); //localizando o id

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Empresa não encontrada para atualização");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe uma empresa com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar empresa.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    /** Localiza pelo CNPJ atual e salva os dados, incluindo o novo CNPJ do modelo. */
    public int atualizarPorCnpj(Empresa empresa, String cnpjAtual) {
        String sql = "UPDATE empresa SET nome = ?, setor = ?, cnpj = ?, status = ?, tamanho = ? WHERE cnpj = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empresa.getNome());
            pstmt.setString(2, empresa.getSetor());
            pstmt.setString(3, empresa.getCnpj());
            pstmt.setObject(4, empresa.getStatus().name(), Types.OTHER);
            pstmt.setObject(5, empresa.getTamanho().name(), Types.OTHER);
            pstmt.setString(6, cnpjAtual);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException(
                    "Empresa não encontrada para atualização pelo CNPJ informado."
            );

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe uma empresa com esse CNPJ.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização da empresa.", e
                );
            }
            throw new DaoException("Erro ao atualizar empresa pelo CNPJ.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Exclui pelo ID; informa ausência do registro ou vínculos que impedem a exclusão. */
    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException(
                    "Empresa não encontrada para exclusão"
            );

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir a empresa porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir empresa.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Driver do banco não encontrado.", e
            );
        }
    }

    /** Exclui uma empresa usando o CNPJ único como identificador. */
    public int excluirPorCnpj(String cnpj) {
        String sql = "DELETE FROM empresa WHERE cnpj = ?;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnpj);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException(
                    "Empresa não encontrada para exclusão pelo CNPJ informado."
            );

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir a empresa porque existem registros vinculados.", e
                );
            }
            throw new DaoException("Erro ao excluir empresa pelo CNPJ.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    /** Converte a linha atual do resultado em um objeto do modelo. */
    private static Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setId_empresa(rs.getLong("id_empresa"));
        empresa.setNome(rs.getString("nome"));
        empresa.setSetor(rs.getString("setor"));
        empresa.setCnpj(rs.getString("cnpj"));
        empresa.setStatus(Status.valueOf(rs.getString("status")));
        String tamanho = rs.getString("tamanho");
        empresa.setTamanho(tamanho == null ? null : TamanhoEmpresa.valueOf(tamanho));
        return empresa;
    }

    // Relaciona as opções de ordenação às colunas autorizadas do banco.
    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_empresa",
            "nome", "nome",
            "setor", "setor",
            "status", "status",
            "tamanho", "tamanho"
    );

    /** Recupera a escrita já cadastrada do setor, ignorando maiúsculas. */
    public String buscarSetorExistente(String setor) {
        String sql = "SELECT setor FROM empresa WHERE LOWER(setor) = LOWER(?) LIMIT 1;";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, setor);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs. next()) {
                    return rs.getString("setor");
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.",e);
        }
    }
}
