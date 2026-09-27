package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;
import br.com.acta.dao.EmpresaDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.Ciclo;
import br.com.acta.model.Empresa;
import br.com.acta.model.Colaborador;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Situacao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "CicloServlet", value = "/ciclo-servlet")
public class CicloServlet extends HttpServlet {
    private static final String PAGINA_CICLO = "/ciclo.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final CicloDAO DAO = new CicloDAO();
    private final EmpresaDAO EMPRESA_DAO = new EmpresaDAO();
    private final ColaboradorDAO COLABORADOR_DAO = new ColaboradorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os ciclos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (Exception e) {
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    // CRUD
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Ciclo ciclo = new Ciclo();

        ciclo.setNome(req.getParameter("nome"));
        ciclo.setDescricao(req.getParameter("descricao"));
        ciclo.setEtapa_atual(EtapaCiclo.valueOf(req.getParameter("etapa_atual")));
        ciclo.setDt_inicio(Date.valueOf(req.getParameter("dt_inicio")));
        ciclo.setDt_fim(Date.valueOf(req.getParameter("dt_fim")));
        ciclo.setStatus(Situacao.valueOf(req.getParameter("status")));
        ciclo.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));
        ciclo.setId_responsavel(Long.parseLong(req.getParameter("id_responsavel")));
        // criado_em não é setado aqui — quem preenche é o banco (now())

        int resultado = DAO.inserir(ciclo);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            preencherNomesFk(List.of(ciclo));
            req.setAttribute("cicloList", List.of(ciclo));
            req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
        }
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        Ciclo ciclo = DAO.buscar(Long.parseLong(id));

        if (ciclo == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        preencherNomesFk(List.of(ciclo));
        req.setAttribute("cicloList", List.of(ciclo));
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-ciclo"));
        Ciclo ciclo = new Ciclo();

        ciclo.setId_ciclo(id);
        ciclo.setNome(req.getParameter("nome"));
        ciclo.setDescricao(req.getParameter("descricao"));
        ciclo.setEtapa_atual(EtapaCiclo.valueOf(req.getParameter("etapa_atual")));
        ciclo.setDt_inicio(Date.valueOf(req.getParameter("dt_inicio")));
        ciclo.setDt_fim(Date.valueOf(req.getParameter("dt_fim")));
        ciclo.setStatus(Situacao.valueOf(req.getParameter("status")));
        ciclo.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));
        ciclo.setId_responsavel(Long.parseLong(req.getParameter("id_responsavel")));
        // criado_em também não entra aqui — nunca é atualizado

        int resultado = DAO.atualizar(ciclo);
        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-ciclo"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser excluído");
    }

    // UTILITARIOS
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Ciclo> ciclos = DAO.buscar();
        preencherNomesFk(ciclos);

        req.setAttribute("cicloList", ciclos);
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    private void preencherNomesFk(List<Ciclo> ciclos) {
        for (Ciclo ciclo : ciclos) {
            Empresa empresa = EMPRESA_DAO.buscar(ciclo.getId_empresa());
            if (empresa != null) ciclo.setNome(empresa.getNome());

            Colaborador responsavel = COLABORADOR_DAO.buscar(ciclo.getId_responsavel());
            if (responsavel != null) ciclo.setNomeResponsavel(responsavel.getNome());
        }
    }
}