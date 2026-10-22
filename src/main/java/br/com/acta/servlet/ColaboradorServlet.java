package br.com.acta.servlet;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
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

@WebServlet(name = "ColaboradorServlet", value = "/colaborador-servlet")
public class ColaboradorServlet extends HttpServlet {
    private static final String PAGINA_COLABORADORES = "/colaborador.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ColaboradorDAO DAO = new ColaboradorDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar os colaboradores.",
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
    // Retorno do DAO: 1 = certo, 0 = errado, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Colaborador colaborador = new Colaborador();

        colaborador.setNome(req.getParameter("nome"));
        colaborador.setSobrenome(req.getParameter("sobrenome"));
        colaborador.setPermissao_gestor(Boolean.parseBoolean(req.getParameter("permissao_gestor")));
        colaborador.setStatus(Status.valueOf(req.getParameter("status")));
        colaborador.setArea(normalizarArea(req.getParameter("area")));
        colaborador.setCargo(req.getParameter("cargo"));
        colaborador.setDt_contratacao(Date.valueOf(req.getParameter("dt_contratacao")));
        colaborador.setEmail(req.getParameter("email"));
        colaborador.setSenha(req.getParameter("senha"));
        colaborador.setTelefone(req.getParameter("telefone"));
        colaborador.setCpf(req.getParameter("cpf"));
        colaborador.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));

        int resultado = DAO.inserir(colaborador);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null || id.isBlank()) {
            buscaFiltrada(req, resp);
            return;
        }

        Colaborador colaborador = DAO.buscar(Long.parseLong(id));

        if (colaborador == null) {
            throw new RegistroNaoEncontradoException("Registro de colaborador não encontrado.");
        }

        req.setAttribute("colaboradorList", List.of(colaborador));
        req.setAttribute("areaList", DAO.buscarAreas());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String statusParam = req.getParameter("status");
        String area = req.getParameter("area");
        String gestorParam = req.getParameter("gestor");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Status status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Status.valueOf(statusParam);
        }

        Boolean permissaoGestor = null;
        if (gestorParam != null && !gestorParam.isBlank()) {
            permissaoGestor = Boolean.parseBoolean(gestorParam);
        }

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) {
            dataInicial = Date.valueOf(dataInicialParam);
        }

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) {
            dataFinal = Date.valueOf(dataFinalParam);
        }

        List<Colaborador> colaboradores = DAO.buscaFiltrada(busca, status, area, permissaoGestor,
                dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-colaborador"));
        Colaborador colaborador = new Colaborador();

        colaborador.setId_colaborador(id);
        colaborador.setNome(req.getParameter("nome"));
        colaborador.setSobrenome(req.getParameter("sobrenome"));
        colaborador.setPermissao_gestor(Boolean.parseBoolean(req.getParameter("permissao_gestor")));
        colaborador.setStatus(Status.valueOf(req.getParameter("status")));
        colaborador.setArea(normalizarArea(req.getParameter("area")));
        colaborador.setCargo(req.getParameter("cargo"));
        colaborador.setDt_contratacao(Date.valueOf(req.getParameter("dt_contratacao")));
        colaborador.setEmail(req.getParameter("email"));
        colaborador.setTelefone(req.getParameter("telefone"));
        colaborador.setCpf(req.getParameter("cpf"));
        colaborador.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));

        int resultado = DAO.atualizar(colaborador);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-colaborador"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser excluído");
    }

    // UTILITARIOS
    private String normalizarArea(String area) {
        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

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
            List<Colaborador> colaboradores = DAO.buscar();

            req.setAttribute("colaboradorList", colaboradores);
            req.setAttribute("areaList", DAO.buscarAreas());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("colaboradorList", List.of());
            req.setAttribute("areaList", List.of());
        }
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
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
        List<Colaborador> colaboradores = DAO.buscar();

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }
}