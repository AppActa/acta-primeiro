package br.com.acta.servlet;

import br.com.acta.dao.TarefaDAO;
import br.com.acta.model.Tarefa;
import br.com.acta.enums.Intensidade;
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

@WebServlet(name = "TarefaServlet", value = "/tarefa-servlet")
public class TarefaServlet extends HttpServlet {
    private static final String PAGINA_TAREFA = "/tarefa.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final TarefaDAO DAO = new TarefaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar as tarefas");
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
        Tarefa tarefa = new Tarefa();

        tarefa.setTitulo(req.getParameter("titulo"));
        tarefa.setDescricao(req.getParameter("descricao"));
        tarefa.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));tarefa.setDt_entrega(Date.valueOf(req.getParameter("dt_entrega")));
        tarefa.setStatus(Situacao.valueOf(req.getParameter("status")));
        tarefa.setDt_inicio(Date.valueOf(req.getParameter("dt_inicio")));
        tarefa.setId_colaborador(Long.parseLong(req.getParameter("id_colaborador")));

        int resultado = DAO.inserir(tarefa);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A tarefa não pode ser cadastrada");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        Tarefa tarefa = DAO.buscar(Long.parseLong(id));

        if (tarefa == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        req.setAttribute("tarefaList", List.of(tarefa));
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-tarefa"));
        Tarefa tarefa = new Tarefa();

        tarefa.setId_tarefa(id);
        tarefa.setTitulo(req.getParameter("titulo"));
        tarefa.setDescricao(req.getParameter("descricao"));
        tarefa.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));
        tarefa.setDt_entrega(Date.valueOf(req.getParameter("dt_entrega")));
        tarefa.setStatus(Situacao.valueOf(req.getParameter("status")));
        tarefa.setDt_inicio(Date.valueOf(req.getParameter("dt_inicio")));
        tarefa.setId_colaborador(Long.parseLong(req.getParameter("id_colaborador")));

        int resultado = DAO.atualizar(tarefa);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A tarefa não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-tarefa"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A tarefa não pode ser excluída");
    }

    // UTILITARIOS
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Tarefa> tarefas = DAO.buscar();

        req.setAttribute("tarefaList", tarefas);
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }
}