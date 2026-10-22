package br.com.acta.servlet;

import br.com.acta.dao.TarefaDAO;
import br.com.acta.model.Tarefa;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
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

@WebServlet(name = "TarefaServlet", value = "/tarefa-servlet")
public class TarefaServlet extends HttpServlet {
    private static final String PAGINA_TAREFA = "/tarefa.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final TarefaDAO DAO = new TarefaDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar as tarefas.",
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
            buscaFiltrada(req, resp);
            return;
        }

        Tarefa tarefa = DAO.buscar(Long.parseLong(id));

        if (tarefa == null) {
            throw new RegistroNaoEncontradoException("Registro de tarefa não encontrado.");
        }

        req.setAttribute("tarefaList", List.of(tarefa));
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String prioridadeParam = req.getParameter("prioridade");
        String statusParam = req.getParameter("status");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Intensidade prioridade = null;
        if (prioridadeParam != null && !prioridadeParam.isBlank()) prioridade = Intensidade.valueOf(prioridadeParam);

        Situacao status = null;
        if (statusParam != null && !statusParam.isBlank()) status = Situacao.valueOf(statusParam);

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) dataInicial = Date.valueOf(dataInicialParam);

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) dataFinal = Date.valueOf(dataFinalParam);

        List<Tarefa> tarefas = DAO.buscaFiltrada(busca, prioridade, status, dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("tarefaList", tarefas);
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
            List<Tarefa> tarefas = DAO.buscar();

            req.setAttribute("tarefaList", tarefas);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("tarefaList", List.of());
        }
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
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
        List<Tarefa> tarefas = DAO.buscar();

        req.setAttribute("tarefaList", tarefas);
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }
}