package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.dao.PlanoAcaoDAO;
import br.com.acta.enums.Intensidade;
import br.com.acta.enums.Situacao;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.PlanoAcao;
import br.com.acta.utils.Validador;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/** Recebe as requisições de PlanoAcao e prepara os dados para as páginas JSP. */
@WebServlet(name = "PlanoAcao", value = "/planoAcao-servlet")
public class PlanoAcaoServlet extends HttpServlet {

    private static final String PAGINA_PLANOACAO = "/crud/planoAcao.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final PlanoAcaoDAO DAO = new PlanoAcaoDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar os planos de ação.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        PlanoAcao planoAcao = new PlanoAcao();

        planoAcao.setNome(Validador.texto(req.getParameter("nome"), "Nome", 100));
        planoAcao.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        planoAcao.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        planoAcao.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));
        planoAcao.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        planoAcao.setId_criador(Validador.id(req.getParameter("id_criador"), "Criador"));

        int resultado = DAO.inserir(planoAcao);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "O plano de ação não pode ser cadastrado");
        }
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        PlanoAcao planoAcao = DAO.buscar(Validador.id(id, "Id"));

        if (planoAcao == null) {
            throw new RegistroNaoEncontradoException("Registro de plano de ação não encontrado.");
        }

        req.setAttribute("planoAcaoList", List.of(planoAcao));
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String statusParam = req.getParameter("status");
        String prioridadeParam = req.getParameter("prioridade");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        // Filtro ausente permanece null e não restringe a consulta.
        Situacao status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Situacao.valueOf(statusParam);
        }

        Intensidade prioridade = null;
        if (prioridadeParam != null && !prioridadeParam.isBlank()) {
            prioridade = Intensidade.valueOf(prioridadeParam);
        }

        List<PlanoAcao> planosAcoes = DAO.buscaFiltrada(busca, status, prioridade, ordem, direcao);

        req.setAttribute("planoAcaoList", planosAcoes);
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-planoAcao"), "Id do plano de ação");
        PlanoAcao planoAcao = new PlanoAcao();

        planoAcao.setId_plano_acao(id);
        planoAcao.setNome(Validador.texto(req.getParameter("nome"), "Nome", 100));
        planoAcao.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        planoAcao.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        planoAcao.setPrioridade(Validador.opcao(Intensidade.class, req.getParameter("prioridade"), "Prioridade"));
        planoAcao.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));
        planoAcao.setId_criador(Validador.id(req.getParameter("id_criador"), "Criador"));

        int resultado = DAO.atualizar(planoAcao);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "O plano de ação não pode ser atualizado.");
        }
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-planoAcao"), "Id do plano de ação");
        int resultado = DAO.excluir(id);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "O plano de ação não pode ser excluído");
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
            List<PlanoAcao> planosAcoes = DAO.buscar();

            req.setAttribute("planoAcaoList", planosAcoes);
            req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
            req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("planoAcaoList", List.of());
            req.setAttribute("cicloOpcoes", List.of());
            req.setAttribute("colaboradorOpcoes", List.of());
        }
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(
            HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<PlanoAcao> planosAcoes = DAO.buscar();

        req.setAttribute("planoAcaoList", planosAcoes);
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }
}
