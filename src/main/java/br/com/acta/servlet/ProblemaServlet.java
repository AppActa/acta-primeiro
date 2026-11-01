package br.com.acta.servlet;

import br.com.acta.dao.PlanoAcaoDAO;

import br.com.acta.dao.ColaboradorDAO;

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

/** Recebe as requisições de Problema e prepara os dados para as páginas JSP. */
@WebServlet(name = "ProblemaServlet", value = "/problema-servlet")
public class ProblemaServlet extends HttpServlet {
    private static final String PAGINA_PROBLEMA = "/crud/problema.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ProblemaDAO DAO = new ProblemaDAO();
    private final CicloDAO CICLO_DAO = new CicloDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            buscar(req, resp);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);

        } catch (ValidacaoException | IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro de consulta inválido.", HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro ao consultar o banco de dados.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os problemas.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Seleciona a ação do formulário e trata erros de validação e persistência. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada.", HttpServletResponse.SC_BAD_REQUEST);
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
                    enviarErro(req, resp, "Ação não existente.", HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (ValidacaoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);

        } catch (DuplicidadeException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);

        } catch (IntegridadeReferencialException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);

        } catch (IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro inválido.", HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro interno ao acessar o banco de dados.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir a operação.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Valida os campos do formulário antes de cadastrar o registro. */
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Problema problema = new Problema();

        problema.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        problema.setDescricao(Validador.textoLongo(req.getParameter("descricao"), "Descrição"));
        problema.setPeso(Validador.opcao(Intensidade.class, req.getParameter("peso"), "Peso"));
        problema.setSolucao(Validador.textoOpcional(req.getParameter("solucao"), "Solução", 200));
        problema.setStatus(Validador.opcao(StatusProblema.class, req.getParameter("status"), "Status"));
        problema.setOrigem(normalizarOrigem(Validador.textoLongoOpcional(req.getParameter("origem"), "Origem")));
        problema.setEncontrado_em(Validador.dataNaoFutura(req.getParameter("encontrado_em"), "Data em que foi encontrado"));
        problema.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        problema.setId_plano_acao(Validador.idOpcional(req.getParameter("id_plano_acao"), "Plano de ação"));
        problema.setId_colaborador(Validador.idOpcional(req.getParameter("id_colaborador"), "Colaborador"));

        int resultado = DAO.inserir(problema);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser cadastrado");
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Problema problema = DAO.buscar(Validador.id(id, "Id"));

        if (problema == null) {
            throw new RegistroNaoEncontradoException("Registro de problema não encontrado.");
        }

        req.setAttribute("problemaList", List.of(problema));
        req.setAttribute("origemList", DAO.buscarOrigem());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String pesoParam = req.getParameter("peso");
        String statusParam = req.getParameter("status");
        String origem = req.getParameter("origem");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        // Filtro ausente permanece null e não restringe a consulta.
        Intensidade peso = null;
        if (pesoParam != null && !pesoParam.isBlank()) peso = Intensidade.valueOf(pesoParam);

        StatusProblema status = null;
        if (statusParam != null && !statusParam.isBlank()) status = StatusProblema.valueOf(statusParam);

        Date dataInicial = Validador.dataOpcional(dataInicialParam, "Data inicial");

        Date dataFinal = Validador.dataOpcional(dataFinalParam, "Data final");

        Validador.periodo(dataInicial, dataFinal, "A data final");

        List<Problema> problemas = DAO.buscaFiltrada(busca, peso, status, origem, dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("problemaList", problemas);
        req.setAttribute("origemList", DAO.buscarOrigem());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-problema"), "Id do problema");
        Problema problema = new Problema();

        problema.setId_problema(id);
        problema.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        problema.setDescricao(Validador.textoLongo(req.getParameter("descricao"), "Descrição"));
        problema.setPeso(Validador.opcao(Intensidade.class, req.getParameter("peso"), "Peso"));
        problema.setSolucao(Validador.textoOpcional(req.getParameter("solucao"), "Solução", 200));
        problema.setStatus(Validador.opcao(StatusProblema.class, req.getParameter("status"), "Status"));
        problema.setOrigem(normalizarOrigem(Validador.textoLongoOpcional(req.getParameter("origem"), "Origem")));
        problema.setEncontrado_em(Validador.dataNaoFutura(req.getParameter("encontrado_em"), "Data em que foi encontrado"));
        problema.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        problema.setId_plano_acao(Validador.idOpcional(req.getParameter("id_plano_acao"), "Plano de ação"));
        problema.setId_colaborador(Validador.idOpcional(req.getParameter("id_colaborador"), "Colaborador"));

        int resultado = DAO.atualizar(problema);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser atualizado");
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-problema"), "Id do problema");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O problema não pode ser excluído");
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarOrigem(String origem) {
        if (origem == null || origem.isBlank()) return null;

        String limpa = origem.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarOrigemExistente(limpa);
        return existente != null ? existente : limpa;
    }

    /** Usa HTTP 500 quando o chamador não informa um status. */
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    /** Encaminha erros 404/500 à página de erro; os demais retornam à tela do cadastro. */
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);

        // Exibe a página de erro sem consultar o banco novamente.
        if (status == HttpServletResponse.SC_NOT_FOUND
                || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Mantém a mensagem na tela do cadastro.
        req.setAttribute("mensagemErro", mensagem);

        // Se a recarga falhar, exibe listas vazias e preserva a mensagem.
        try {
            List<Problema> problemas = DAO.buscar();

            req.setAttribute("problemaList", problemas);
            req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
            req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
            req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
            req.setAttribute("origemList", DAO.buscarOrigem());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("problemaList", List.of());
            req.setAttribute("cicloOpcoes", List.of());
            req.setAttribute("planoAcaoOpcoes", List.of());
            req.setAttribute("colaboradorOpcoes", List.of());
            req.setAttribute("origemList", List.of());
        }
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Problema> problemas = DAO.buscar();

        req.setAttribute("problemaList", problemas);
        req.setAttribute("origemList", DAO.buscarOrigem());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PROBLEMA).forward(req, resp);
    }

}
