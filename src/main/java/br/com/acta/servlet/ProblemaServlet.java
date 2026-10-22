package br.com.acta.servlet;

import br.com.acta.dao.ProblemaDAO;
import br.com.acta.dao.CicloDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.model.Problema;
import br.com.acta.enums.StatusProblema;
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

@WebServlet(name = "ProblemaServlet", value = "/problema-servlet")
public class ProblemaServlet extends HttpServlet {
    private static final String PAGINA_PROBLEMA = "/problema.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ProblemaDAO DAO = new ProblemaDAO();
    private final CicloDAO CICLO_DAO = new CicloDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar os problemas.",
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
        Problema problema = new Problema();

        problema.setTitulo(req.getParameter("titulo"));
        problema.setDescricao(req.getParameter("descricao"));
        problema.setPeso(Intensidade.valueOf(req.getParameter("peso")));
        problema.setSolucao(req.getParameter("solucao"));
        problema.setStatus(StatusProblema.valueOf(req.getParameter("status")));
        problema.setOrigem(req.getParameter("origem"));
        problema.setEncontrado_em(Date.valueOf(req.getParameter("encontrado_em")));
        problema.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        problema.setId_plano_acao(Long.parseLong(req.getParameter("id_plano_acao")));
        problema.setId_colaborador(Long.parseLong(req.getParameter("id_colaborador")));

        int resultado = DAO.inserir(problema);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Problema problema = DAO.buscar(Long.parseLong(id));

        if (problema == null) {
            throw new RegistroNaoEncontradoException("Registro de problema não encontrado.");
        }

        req.setAttribute("problemaList", List.of(problema));
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String pesoParam = req.getParameter("peso");
        String statusParam = req.getParameter("status");
        String origem = req.getParameter("origem");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Intensidade peso = null;
        if (pesoParam != null && !pesoParam.isBlank()) peso = Intensidade.valueOf(pesoParam);

        StatusProblema status = null;
        if (statusParam != null && !statusParam.isBlank()) status = StatusProblema.valueOf(statusParam);

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) dataInicial = Date.valueOf(dataInicialParam);

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) dataFinal = Date.valueOf(dataFinalParam);

        List<Problema> problemas = DAO.buscaFiltrada(busca, peso, status, origem, dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("problemaList", problemas);
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-problema"));
        Problema problema = new Problema();

        problema.setId_problema(id);
        problema.setTitulo(req.getParameter("titulo"));
        problema.setDescricao(req.getParameter("descricao"));
        problema.setPeso(Intensidade.valueOf(req.getParameter("peso")));
        problema.setSolucao(req.getParameter("solucao"));
        problema.setStatus(StatusProblema.valueOf(req.getParameter("status")));
        problema.setOrigem(req.getParameter("origem"));
        problema.setEncontrado_em(Date.valueOf(req.getParameter("encontrado_em")));
        problema.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));
        problema.setId_plano_acao(Long.parseLong(req.getParameter("id_plano_acao")));
        problema.setId_colaborador(Long.parseLong(req.getParameter("id_colaborador")));

        int resultado = DAO.atualizar(problema);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-problema"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser excluído");
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
            List<Problema> problemas = DAO.buscar();

            req.setAttribute("problemaList", problemas);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("problemaList", List.of());
        }
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
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
        List<Problema> problemas = DAO.buscar();

        req.setAttribute("problemaList", problemas);
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }
}