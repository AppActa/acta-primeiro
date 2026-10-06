package br.com.acta.servlet;

import br.com.acta.dao.MetaDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.model.Meta;
import br.com.acta.enums.StatusMeta;
import jakarta.servlet.RequestDispatcher;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar as metas");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
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
                case null:
                    enviarErro(req, resp, "Ação não informada");
                    break;
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir");
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
            enviarPaginaCerta(req, resp);
            return;
        }

        Meta meta = DAO.buscar(Long.parseLong(id));

        if (meta == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        req.setAttribute("metaList", List.of(meta));
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
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Meta> metas = DAO.buscar();

        req.setAttribute("metaList", metas);
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }
}