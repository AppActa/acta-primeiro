package br.com.acta.dao;

import br.com.acta.model.Ciclo;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Situacao;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.utils.Conexao;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CicloDAO implements MetodosCrud<Ciclo> {

    @Override
    public int inserir(Ciclo ciclo) {
        String sql = """
                INSERT INTO ciclo (nome, descricao, etapa_atual, dt_inicio, dt_fim, status, id_empresa, id_responsavel)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
        // criado_em NÃO entra aqui, o banco preenche sozinho com now()

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ciclo.getNome());
            pstmt.setString(2, ciclo.getDescricao());
            pstmt.setObject(3, ciclo.getEtapa_atual().name(), Types.OTHER);
            pstmt.setDate(4, ciclo.getDt_inicio());
            pstmt.setDate(5, ciclo.getDt_fim());
            pstmt.setObject(6, ciclo.getStatus().name(), Types.OTHER);
            pstmt.setLong(7, ciclo.getId_empresa());
            pstmt.setLong(8, ciclo.getId_responsavel());

            if (pstmt.executeUpdate() > 0) return 1;
            else return 0;

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um registro de ciclo com esses dados.", e);
            }
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException("Existe uma referência inválida no cadastro.", e);
            }
            throw new DaoException("Erro ao cadastrar ciclo.", e);

        } catch (ClassNotFoundException e) {
            throw new DaoException("Driver do banco de dados não encontrado.", e);
        }
    }

    @Override
    public Ciclo buscar(Long id) {
        String sql = "SELECT * FROM vw_ciclo WHERE id_ciclo = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCiclo(rs);
                }
                return null;
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public List<Ciclo> buscar() {
        List<Ciclo> ciclos = new ArrayList<>();
        String sql = "SELECT * FROM vw_ciclo;";

        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ciclos.add(mapearCiclo(rs));
            }
            return ciclos;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    public List<Ciclo> buscaFiltrada(String busca, EtapaCiclo etapa, Situacao status, Date dataInicial, Date dataFinal, Date criadoDe, Date criadoAte, String ordem, String direcao) {
        List<Ciclo> ciclos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_ciclo WHERE 1=1");

        boolean temBusca = busca != null && !busca.isBlank();
        boolean temEtapa = etapa != null;
        boolean temStatus = status != null;
        boolean temDataInicial = dataInicial != null;
        boolean temDataFinal = dataFinal != null;
        boolean temCriadoDe = criadoDe != null;
        boolean temCriadoAte = criadoAte != null;

        String colunaOrdem = ordem == null ? null : COLUNAS_ORDENACAO.get(ordem);
        boolean temOrdem = colunaOrdem != null;

        if (temBusca) sql.append(" AND (nome ILIKE ? OR nome_empresa ILIKE ? OR nome_responsavel ILIKE ?)");
        if (temEtapa) sql.append(" AND etapa_atual = ?");
        if (temStatus) sql.append(" AND status = ?");
        if (temDataInicial) sql.append(" AND dt_inicio >= ?");
        if (temDataFinal) sql.append(" AND dt_inicio <= ?");
        if (temCriadoDe) sql.append(" AND criado_em >= ?::date");
        if (temCriadoAte) sql.append(" AND criado_em < (?::date + 1)");
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
                pstmt.setString(i++, termo);
            }
            if (temEtapa) pstmt.setObject(i++, etapa.name(), Types.OTHER);
            if (temStatus) pstmt.setObject(i++, status.name(), Types.OTHER);
            if (temDataInicial) pstmt.setDate(i++, dataInicial);
            if (temDataFinal) pstmt.setDate(i++, dataFinal);
            if (temCriadoDe) pstmt.setDate(i++, criadoDe);
            if (temCriadoAte) pstmt.setDate(i++, criadoAte);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ciclos.add(mapearCiclo(rs));
                }
            }
            return ciclos;

        } catch (SQLException | ClassNotFoundException e) {
            throw new DaoException("Erro ao conectar no banco de dados.", e);
        }
    }

    @Override
    public int atualizar(Ciclo ciclo) {
        String sql = "UPDATE ciclo SET nome = ?, descricao = ?, etapa_atual = ?, dt_inicio = ?, dt_fim = ?, status = ?, id_empresa = ?, id_responsavel = ? WHERE id_ciclo = ?";
        //em nunca é atualizado porque o banco passa direto quando entra para data e local;

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ciclo.getNome());
            pstmt.setString(2, ciclo.getDescricao());
            pstmt.setObject(3, ciclo.getEtapa_atual().name(), Types.OTHER);
            pstmt.setDate(4, ciclo.getDt_inicio());
            pstmt.setDate(5, ciclo.getDt_fim());
            pstmt.setObject(6, ciclo.getStatus().name(), Types.OTHER);
            pstmt.setLong(7, ciclo.getId_empresa());
            pstmt.setLong(8, ciclo.getId_responsavel());
            pstmt.setLong(9, ciclo.getId_ciclo());

            if (pstmt.executeUpdate() > 0) return 1;

            throw new RegistroNaoEncontradoException("Registro de ciclo não encontrado para atualização.");

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "Já existe um registro de ciclo com esses dados.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Referência inválida na atualização.", e
                );
            }

            throw new DaoException(
                    "Erro ao atualizar ciclo.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
        }
    }

    @Override
    public int excluir(Long id) {
        String sql = "DELETE FROM ciclo WHERE id_ciclo = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            if (pstmt.executeUpdate() > 0) return 1;
            throw new RegistroNaoEncontradoException("Registro de ciclo não encontrado para exclusão.");

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IntegridadeReferencialException(
                        "Não é possível excluir o registro de ciclo porque existem registros vinculados.", e
                );
            }

            throw new DaoException(
                    "Erro ao excluir ciclo.", e
            );

        } catch (ClassNotFoundException e) {
            throw new DaoException(
                    "Erro ao conectar no banco de dados.", e
            );
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
        ciclo.setNome_empresa(rs.getString("nome_empresa"));
        ciclo.setNome_responsavel(rs.getString("nome_responsavel"));
        ciclo.setId_empresa(rs.getLong("id_empresa"));
        ciclo.setId_responsavel(rs.getLong("id_responsavel"));
        return ciclo;
    }

    private static final Map<String, String> COLUNAS_ORDENACAO = Map.of(
            "id", "id_ciclo",
            "nome", "nome",
            "etapa", "etapa_atual",
            "status", "status",
            "inicio", "dt_inicio",
            "fim", "dt_fim",
            "criado_em", "criado_em",
            "empresa", "nome_empresa",
            "responsavel", "nome_responsavel"
    );
}
