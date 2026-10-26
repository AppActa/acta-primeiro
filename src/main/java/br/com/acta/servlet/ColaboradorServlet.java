package br.com.acta.servlet;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
import jakarta.servlet.RequestDispatcher;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os colaboradores");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
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
                case null:
                    enviarErro(req, resp, "Ação não informada");
                    break;
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir");
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
        colaborador.setCargo(normalizarCargo(req.getParameter("cargo")));
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
            enviarPaginaCerta(req, resp);
            return;
        }

        req.setAttribute("colaboradorList", List.of(colaborador));
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String statusParam = req.getParameter("status");
        String area = req.getParameter("area");
        String cargo = req.getParameter("cargo");
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

        List<Colaborador> colaboradores = DAO.buscaFiltrada(busca, status, area, cargo, permissaoGestor, dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
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
        colaborador.setCargo(normalizarCargo(req.getParameter("cargo")));
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

    private String normalizarCargo(String cargo) {
        String limpa = cargo.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCargoExistente(limpa);
        return existente != null ? existente : limpa;
    }

    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Colaborador> colaboradores = DAO.buscar();

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }
}