package br.com.acta.servlet;

import br.com.acta.dao.PlanoAcaoDAO;

import br.com.acta.dao.ColaboradorDAO;

import br.com.acta.dao.TarefaDAO;
import br.com.acta.model.Tarefa;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.utils.Validador;
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
    private static final String PAGINA_TAREFA = "/crud/tarefa.jsp";
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

        tarefa.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        tarefa.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        tarefa.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));tarefa.setDt_entrega(Validador.dataOpcional(req.getParameter("dt_entrega"), "Data de entrega"));
        tarefa.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        tarefa.setDt_inicio(Validador.data(req.getParameter("dt_inicio"), "Data de início"));
        tarefa.setId_colaborador(Validador.id(req.getParameter("id_colaborador"), "Colaborador"));
        tarefa.setId_plano_acao(Validador.id(req.getParameter("id_plano_acao"), "Plano de ação"));

        Validador.periodo(tarefa.getDt_inicio(), tarefa.getDt_entrega(), "A data de entrega");
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

        Tarefa tarefa = DAO.buscar(Validador.id(id, "Id"));

        if (tarefa == null) {
            throw new RegistroNaoEncontradoException("Registro de tarefa não encontrado.");
        }

        req.setAttribute("tarefaList", List.of(tarefa));
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
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

        Date dataInicial = Validador.dataOpcional(dataInicialParam, "Data inicial");

        Date dataFinal = Validador.dataOpcional(dataFinalParam, "Data final");

        Validador.periodo(dataInicial, dataFinal, "A data final");

        List<Tarefa> tarefas = DAO.buscaFiltrada(busca, prioridade, status, dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("tarefaList", tarefas);
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-tarefa"), "Id da tarefa");
        Tarefa tarefa = new Tarefa();

        tarefa.setId_tarefa(id);
        tarefa.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        tarefa.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        tarefa.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));
        tarefa.setDt_entrega(Validador.dataOpcional(req.getParameter("dt_entrega"), "Data de entrega"));
        tarefa.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        tarefa.setDt_inicio(Validador.data(req.getParameter("dt_inicio"), "Data de início"));
        tarefa.setId_colaborador(Validador.id(req.getParameter("id_colaborador"), "Colaborador"));
        tarefa.setId_plano_acao(Validador.id(req.getParameter("id_plano_acao"), "Plano de ação"));

        Validador.periodo(tarefa.getDt_inicio(), tarefa.getDt_entrega(), "A data de entrega");
        int resultado = DAO.atualizar(tarefa);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A tarefa não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-tarefa"), "Id da tarefa");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A tarefa não pode ser excluída");
    }

    // UTILITARIOS
    // SOBRECARGA 1 (3 parâmetros): erro sem status definido.
    // Assume 500 (erro grave), então o usuário é levado para a página de erro.
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    // SOBRECARGA 2 (4 parâmetros): quem realmente trata o erro. O destino depende do status:
    //  - 404 (registro não encontrado) e 500 (erro de banco/inesperado): vai para a página de erro (/erro.jsp)
    //  - 400 (dado inválido) e 409 (duplicado/em uso): volta para a tela da entidade, mostrando a mensagem
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);

        // Erros graves: página de erro (não consulta o banco, então funciona mesmo com ele fora do ar)
        if (status == HttpServletResponse.SC_NOT_FOUND
                || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Erros do usuário: volta para a tela com a mensagem
        req.setAttribute("mensagemErro", mensagem);

        // Não pode depender do banco: se recarregar a lista falhar, mostra a tela vazia com a mensagem
        try {
            List<Tarefa> tarefas = DAO.buscar();

            req.setAttribute("tarefaList", tarefas);
            req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
            req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("tarefaList", List.of());
            req.setAttribute("colaboradorOpcoes", List.of());
            req.setAttribute("planoAcaoOpcoes", List.of());
        }
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Tarefa> tarefas = DAO.buscar();

        req.setAttribute("tarefaList", tarefas);
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.getRequestDispatcher(PAGINA_TAREFA).forward(req, resp);
    }
}
