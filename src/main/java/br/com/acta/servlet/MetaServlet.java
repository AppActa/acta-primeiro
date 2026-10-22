package br.com.acta.servlet;

import br.com.acta.dao.MetaDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.model.Meta;
import br.com.acta.enums.StatusMeta;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "MetaServlet", value = "/meta-servlet")
public class MetaServlet extends HttpServlet {
    private static final String PAGINA_META = "/meta.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final MetaDAO DAO = new MetaDAO();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            buscar(req, resp);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_NOT_FOUND);

        } catch (ValidacaoException | IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro de consulta inválido.",
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro ao consultar o banco de dados.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar as metas.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada.",
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            switch (acao) {
                case "inserir":
                    inserir(req, resp);
                    break;

                case "atualizar":
                    atualizar(req, resp);
                    break;

                case "excluir":
                    excluir(req, resp);
                    break;

                default:
                    enviarErro(req, resp, "Ação não existente.",
                            HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (ValidacaoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_NOT_FOUND);

        } catch (DuplicidadeException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_CONFLICT);

        } catch (IntegridadeReferencialException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_CONFLICT);

        } catch (IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro inválido.",
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro interno ao acessar o banco de dados.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir a operação.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Meta meta = new Meta();

        meta.setMeta(req.getParameter("meta"));
        meta.setDescricao_meta(req.getParameter("descricao_meta"));
        meta.setObjetivo(req.getParameter("objetivo"));
        meta.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));
        meta.setPrazo(Date.valueOf(req.getParameter("prazo")));
        meta.setStatus(StatusMeta.valueOf(req.getParameter("status")));
        meta.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        meta.setId_plano_acao(Long.parseLong(req.getParameter("id_plano_acao")));

        int resultado = DAO.inserir(meta);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A meta não pode ser cadastrada");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Meta meta = DAO.buscar(Long.parseLong(id));

        if (meta == null) {
            throw new RegistroNaoEncontradoException("Registro de meta não encontrado.");
        }

        req.setAttribute("metaList", List.of(meta));
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String prioridadeParam = req.getParameter("prioridade");
        String statusParam = req.getParameter("status");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String criadoDeParam = req.getParameter("criadoDe");
        String criadoAteParam = req.getParameter("criadoAte");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Intensidade prioridade = null;
        if (prioridadeParam != null && !prioridadeParam.isBlank()) prioridade = Intensidade.valueOf(prioridadeParam);

        StatusMeta status = null;
        if (statusParam != null && !statusParam.isBlank()) status = StatusMeta.valueOf(statusParam);

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) dataInicial = Date.valueOf(dataInicialParam);

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) dataFinal = Date.valueOf(dataFinalParam);

        Date criadoDe = null;
        if (criadoDeParam != null && !criadoDeParam.isBlank()) criadoDe = Date.valueOf(criadoDeParam);

        Date criadoAte = null;
        if (criadoAteParam != null && !criadoAteParam.isBlank()) criadoAte = Date.valueOf(criadoAteParam);

        List<Meta> metas = DAO.buscaFiltrada(busca, prioridade, status, dataInicial, dataFinal, criadoDe, criadoAte, ordem, direcao);

        req.setAttribute("metaList", metas);
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-meta"));
        Meta meta = new Meta();

        meta.setId_meta(id);
        meta.setMeta(req.getParameter("meta"));
        meta.setDescricao_meta(req.getParameter("descricao_meta"));
        meta.setObjetivo(req.getParameter("objetivo"));
        meta.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));
        meta.setPrazo(Date.valueOf(req.getParameter("prazo")));
        meta.setStatus(StatusMeta.valueOf(req.getParameter("status")));
        meta.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        meta.setId_plano_acao(Long.parseLong(req.getParameter("id_plano_acao")));

        int resultado = DAO.atualizar(meta);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A meta não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-meta"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A meta não pode ser excluída");
    }

    // UTILITARIOS
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);
        req.setAttribute("mensagemErro", mensagem);

        // Não pode depender do banco: se recarregar a lista falhar, mostra a tela vazia com a mensagem
        try {
            List<Meta> metas = DAO.buscar();

            req.setAttribute("metaList", metas);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("metaList", List.of());
        }
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Meta> metas = DAO.buscar();

        req.setAttribute("metaList", metas);
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }
}