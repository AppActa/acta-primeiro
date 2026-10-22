package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;
import br.com.acta.model.Ciclo;
import br.com.acta.enums.EtapaCiclo;
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

@WebServlet(name = "CicloServlet", value = "/ciclo-servlet")
public class CicloServlet extends HttpServlet {
    private static final String PAGINA_CICLO = "/ciclo.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final CicloDAO cicloDAO = new CicloDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar os ciclos.",
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

        int resultado = cicloDAO.inserir(ciclo);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Ciclo ciclo = cicloDAO.buscar(Long.parseLong(id));

        if (ciclo == null) {
            throw new RegistroNaoEncontradoException("Registro de ciclo não encontrado.");
        }

        req.setAttribute("cicloList", List.of(ciclo));
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String etapaParam = req.getParameter("etapa");
        String statusParam = req.getParameter("status");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String criadoDeParam = req.getParameter("criadoDe");
        String criadoAteParam = req.getParameter("criadoAte");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        EtapaCiclo etapa = null;
        if (etapaParam != null && !etapaParam.isBlank()) etapa = EtapaCiclo.valueOf(etapaParam);

        Situacao status = null;
        if (statusParam != null && !statusParam.isBlank()) status = Situacao.valueOf(statusParam);

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) dataInicial = Date.valueOf(dataInicialParam);

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) dataFinal = Date.valueOf(dataFinalParam);

        Date criadoDe = null;
        if (criadoDeParam != null && !criadoDeParam.isBlank()) criadoDe = Date.valueOf(criadoDeParam);

        Date criadoAte = null;
        if (criadoAteParam != null && !criadoAteParam.isBlank()) criadoAte = Date.valueOf(criadoAteParam);

        List<Ciclo> ciclos = cicloDAO.buscaFiltrada(busca, etapa, status, dataInicial, dataFinal, criadoDe, criadoAte, ordem, direcao);

        req.setAttribute("cicloList", ciclos);
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
        // criado_em também não entra aqui porque o banco faz automatico.

        int resultado = cicloDAO.atualizar(ciclo);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-ciclo"));
        int resultado = cicloDAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser excluído");
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
            List<Ciclo> ciclos = cicloDAO.buscar();

            req.setAttribute("cicloList", ciclos);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("cicloList", List.of());
        }
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
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
        List<Ciclo> ciclos = cicloDAO.buscar();

        req.setAttribute("cicloList", ciclos);
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }
}