package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;
import br.com.acta.dao.MetaDAO;
import br.com.acta.dao.PlanoAcaoDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.StatusMeta;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.Meta;
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

/** Recebe as requisições de Meta e prepara os dados para as páginas JSP. */
@WebServlet(name = "MetaServlet", value = "/meta-servlet")
public class MetaServlet extends HttpServlet {

    private static final String PAGINA_META = "/crud/meta.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final MetaDAO DAO = new MetaDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            enviarErro(req, resp, "Não foi possível encontrar as metas.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Seleciona a ação do formulário e trata erros de validação e persistência. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            enviarErro(req, resp, "Erro interno ao acessar o banco de dados.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir a operação.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Valida os campos do formulário antes de cadastrar o registro. */
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Meta meta = new Meta();

        meta.setMeta(Validador.texto(req.getParameter("meta"), "Meta", 100));
        meta.setDescricao_meta(Validador.textoLongoOpcional(req.getParameter("descricao_meta"), "Descrição"));
        meta.setObjetivo(Validador.textoOpcional(req.getParameter("objetivo"), "Objetivo", 200));
        meta.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));
        meta.setPrazo(Validador.data(req.getParameter("prazo"), "Prazo"));
        meta.setStatus(Validador.opcao(StatusMeta.class, req.getParameter("status"), "Status"));
        meta.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        meta.setId_plano_acao(Validador.idOpcional(req.getParameter("id_plano_acao"), "Plano de ação"));

        int resultado = DAO.inserir(meta);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "A meta não pode ser cadastrada");
        }
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Meta meta = DAO.buscar(Validador.id(id, "Id"));

        if (meta == null) {
            throw new RegistroNaoEncontradoException("Registro de meta não encontrado.");
        }

        req.setAttribute("metaList", List.of(meta));
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
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

        // Filtro ausente permanece null e não restringe a consulta.
        Intensidade prioridade = null;
        if (prioridadeParam != null && !prioridadeParam.isBlank()) {
            prioridade = Intensidade.valueOf(prioridadeParam);
        }

        StatusMeta status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = StatusMeta.valueOf(statusParam);
        }

        Date dataInicial = Validador.dataOpcional(dataInicialParam, "Data inicial");

        Date dataFinal = Validador.dataOpcional(dataFinalParam, "Data final");

        Date criadoDe = Validador.dataOpcional(criadoDeParam, "Data inicial de criação");

        Date criadoAte = Validador.dataOpcional(criadoAteParam, "Data final de criação");

        Validador.periodo(dataInicial, dataFinal, "A data final");
        Validador.periodo(criadoDe, criadoAte, "A data final de criação");

        List<Meta> metas = DAO.buscaFiltrada(
                busca, prioridade, status, dataInicial, dataFinal, criadoDe, criadoAte, ordem, direcao);

        req.setAttribute("metaList", metas);
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-meta"), "Id da meta");
        Meta meta = new Meta();

        meta.setId_meta(id);
        meta.setMeta(Validador.texto(req.getParameter("meta"), "Meta", 100));
        meta.setDescricao_meta(Validador.textoLongoOpcional(req.getParameter("descricao_meta"), "Descrição"));
        meta.setObjetivo(Validador.textoOpcional(req.getParameter("objetivo"), "Objetivo", 200));
        meta.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));
        meta.setPrazo(Validador.data(req.getParameter("prazo"), "Prazo"));
        meta.setStatus(Validador.opcao(StatusMeta.class, req.getParameter("status"), "Status"));
        meta.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        meta.setId_plano_acao(Validador.idOpcional(req.getParameter("id_plano_acao"), "Plano de ação"));

        int resultado = DAO.atualizar(meta);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "A meta não pode ser atualizada");
        }
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-meta"), "Id da meta");
        int resultado = DAO.excluir(id);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "A meta não pode ser excluída");
        }
    }

    /** Usa HTTP 500 quando o chamador não informa um status. */
    private void enviarErro(
            HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        enviarErro(req, resp, mensagem, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /** Encaminha erros 404/500 à página de erro; os demais retornam à tela do cadastro. */
    private void enviarErro(
            HttpServletRequest req, HttpServletResponse resp, String mensagem, int status)
            throws ServletException, IOException {
        resp.setStatus(status);

        // Exibe a página de erro sem consultar o banco novamente.
        if (status == HttpServletResponse.SC_NOT_FOUND || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Mantém a mensagem na tela do cadastro.
        req.setAttribute("mensagemErro", mensagem);

        // Se a recarga falhar, exibe listas vazias e preserva a mensagem.
        try {
            List<Meta> metas = DAO.buscar();

            req.setAttribute("metaList", metas);
            req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
            req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("metaList", List.of());
            req.setAttribute("cicloOpcoes", List.of());
            req.setAttribute("planoAcaoOpcoes", List.of());
        }
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(
            HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Meta> metas = DAO.buscar();

        req.setAttribute("metaList", metas);
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("planoAcaoOpcoes", new PlanoAcaoDAO().buscar());
        req.getRequestDispatcher(PAGINA_META).forward(req, resp);
    }
}
