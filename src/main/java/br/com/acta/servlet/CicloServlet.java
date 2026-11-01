package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;

import br.com.acta.dao.ColaboradorDAO;

import br.com.acta.dao.CicloDAO;
import br.com.acta.model.Ciclo;
import br.com.acta.enums.EtapaCiclo;
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

/** Recebe as requisições de Ciclo e prepara os dados para as páginas JSP. */
@WebServlet(name = "CicloServlet", value = "/ciclo-servlet")
public class CicloServlet extends HttpServlet {
    private static final String PAGINA_CICLO = "/crud/ciclo.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final CicloDAO cicloDAO = new CicloDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
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

    /** Seleciona a ação do formulário e trata erros de validação e persistência. */
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

    /** Valida os campos do formulário antes de cadastrar o registro. */
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Ciclo ciclo = new Ciclo();

        ciclo.setNome(Validador.texto(req.getParameter("nome"), "Nome", 100));
        ciclo.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        ciclo.setEtapa_atual(Validador.opcao(EtapaCiclo.class, req.getParameter("etapa_atual"), "Etapa"));
        ciclo.setDt_inicio(Validador.data(req.getParameter("dt_inicio"), "Data de início"));
        ciclo.setDt_fim(Validador.dataOpcional(req.getParameter("dt_fim"), "Data de fim"));
        ciclo.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        ciclo.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));
        ciclo.setId_responsavel(Validador.id(req.getParameter("id_responsavel"), "Responsável"));
        // A data de criação é preenchida pelo banco.

        Validador.periodo(ciclo.getDt_inicio(), ciclo.getDt_fim(), "A data de fim");
        int resultado = cicloDAO.inserir(ciclo);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser cadastrado");
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Ciclo ciclo = cicloDAO.buscar(Validador.id(id, "Id"));

        if (ciclo == null) {
            throw new RegistroNaoEncontradoException("Registro de ciclo não encontrado.");
        }

        req.setAttribute("cicloList", List.of(ciclo));
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
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

        // Filtro ausente permanece null e não restringe a consulta.
        EtapaCiclo etapa = null;
        if (etapaParam != null && !etapaParam.isBlank()) etapa = EtapaCiclo.valueOf(etapaParam);

        Situacao status = null;
        if (statusParam != null && !statusParam.isBlank()) status = Situacao.valueOf(statusParam);

        Date dataInicial = Validador.dataOpcional(dataInicialParam, "Data inicial");

        Date dataFinal = Validador.dataOpcional(dataFinalParam, "Data final");

        Date criadoDe = Validador.dataOpcional(criadoDeParam, "Data inicial de criação");

        Date criadoAte = Validador.dataOpcional(criadoAteParam, "Data final de criação");

        Validador.periodo(dataInicial, dataFinal, "A data final");
        Validador.periodo(criadoDe, criadoAte, "A data final de criação");

        List<Ciclo> ciclos = cicloDAO.buscaFiltrada(busca, etapa, status, dataInicial, dataFinal, criadoDe, criadoAte, ordem, direcao);

        req.setAttribute("cicloList", ciclos);
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-ciclo"), "Id do ciclo");
        Ciclo ciclo = new Ciclo();

        ciclo.setId_ciclo(id);
        ciclo.setNome(Validador.texto(req.getParameter("nome"), "Nome", 100));
        ciclo.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        ciclo.setEtapa_atual(Validador.opcao(EtapaCiclo.class, req.getParameter("etapa_atual"), "Etapa"));
        ciclo.setDt_inicio(Validador.data(req.getParameter("dt_inicio"), "Data de início"));
        ciclo.setDt_fim(Validador.dataOpcional(req.getParameter("dt_fim"), "Data de fim"));
        ciclo.setStatus(Validador.opcao(Situacao.class, req.getParameter("status"), "Situação"));
        ciclo.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));
        ciclo.setId_responsavel(Validador.id(req.getParameter("id_responsavel"), "Responsável"));
        // A atualização preserva a data original de criação.

        Validador.periodo(ciclo.getDt_inicio(), ciclo.getDt_fim(), "A data de fim");
        int resultado = cicloDAO.atualizar(ciclo);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser atualizado");
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-ciclo"), "Id do ciclo");
        int resultado = cicloDAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O ciclo não pode ser excluído");
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
            List<Ciclo> ciclos = cicloDAO.buscar();

            req.setAttribute("cicloList", ciclos);
            req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
            req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("cicloList", List.of());
            req.setAttribute("empresaOpcoes", List.of());
            req.setAttribute("colaboradorOpcoes", List.of());
        }
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Ciclo> ciclos = cicloDAO.buscar();

        req.setAttribute("cicloList", ciclos);
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.setAttribute("colaboradorOpcoes", new ColaboradorDAO().buscar());
        req.getRequestDispatcher(PAGINA_CICLO).forward(req, resp);
    }

}
