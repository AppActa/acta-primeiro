package br.com.acta.servlet;

import br.com.acta.dao.PlanoAcaoDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
import br.com.acta.model.PlanoAcao;
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
import java.util.List;

@WebServlet(name = "PlanoAcao", value = "/planoAcao-servlet")
public class PlanoAcaoServlet extends HttpServlet {
    private static final String PAGINA_PLANOACAO = "/planoAcao.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final PlanoAcaoDAO DAO = new PlanoAcaoDAO();

    //DoGet e DoPost
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
            enviarErro(req, resp, "Não foi possível encontrar os planos de ação.",
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


    //CRUD
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PlanoAcao planoAcao = new PlanoAcao();

        planoAcao.setNome(req.getParameter("nome"));
        planoAcao.setDescricao(req.getParameter("descricao"));
        planoAcao.setStatus(Situacao.valueOf(req.getParameter("status")));
        planoAcao.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));
        planoAcao.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        planoAcao.setId_criador(Long.parseLong(req.getParameter("id_criador")));

        int resultado = DAO.inserir(planoAcao);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O plano de ação não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        PlanoAcao planoAcao = DAO.buscar(Long.parseLong(id));

        if (planoAcao == null) {
            throw new RegistroNaoEncontradoException("Registro de plano de ação não encontrado.");
        }

        req.setAttribute("planoAcaoList", List.of(planoAcao));
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String statusParam = req.getParameter("status");
        String prioridadeParam = req.getParameter("prioridade");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Situacao status = null;
        if (statusParam != null && !statusParam.isBlank()) status = Situacao.valueOf(statusParam);

        Intensidade prioridade = null;
        if (prioridadeParam != null && !prioridadeParam.isBlank()) prioridade = Intensidade.valueOf(prioridadeParam);

        List<PlanoAcao> planosAcoes = DAO.buscaFiltrada(busca, status, prioridade, ordem, direcao);

        req.setAttribute("planoAcaoList", planosAcoes);
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        Long id = Long.parseLong(req.getParameter("id-planoAcao"));
        PlanoAcao planoAcao = new PlanoAcao();

        planoAcao.setId_plano_acao(id);
        planoAcao.setNome(req.getParameter("nome"));
        planoAcao.setDescricao(req.getParameter("descricao"));
        planoAcao.setStatus(Situacao.valueOf(req.getParameter("status")));
        planoAcao.setPrioridade(Intensidade.valueOf(req.getParameter("prioridade")));
        planoAcao.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        planoAcao.setId_criador(Long.parseLong(req.getParameter("id_criador")));

        int resultado = DAO.atualizar(planoAcao);

        if (resultado == 1) enviarPaginaCerta(req,resp);
        else enviarErro(req,resp,"O plano de ação não pode ser atualizado.");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-planoAcao"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O plano de ação não pode ser excluído");
    }
    //UTILITARIOS
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
            List<PlanoAcao> planosAcoes = DAO.buscar();

            req.setAttribute("planoAcaoList", planosAcoes);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("planoAcaoList", List.of());
        }
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
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
        List<PlanoAcao> planosAcoes = DAO.buscar();

        req.setAttribute("planoAcaoList", planosAcoes);
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }
}