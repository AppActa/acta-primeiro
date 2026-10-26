package br.com.acta.servlet;

import br.com.acta.dao.LicoesAprendidasDAO;
import br.com.acta.model.LicoesAprendidas;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Intensidade;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LicoesAprendidasServlet", value = "/licoes-aprendidas-servlet")
public class LicoesAprendidasServlet extends HttpServlet {
    private static final String PAGINA_LICOES_APRENDIDAS = "/licoes-aprendidas.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final LicoesAprendidasDAO DAO = new LicoesAprendidasDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar as lições aprendidas");
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
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LicoesAprendidas licoesAprendidas = new LicoesAprendidas();

        licoesAprendidas.setTitulo(req.getParameter("titulo"));
        licoesAprendidas.setArea(normalizarArea(req.getParameter("area")));
        licoesAprendidas.setAprendizado(req.getParameter("aprendizado"));
        licoesAprendidas.setCategoria(normalizarCategoria(req.getParameter("categoria")));
        licoesAprendidas.setDescricao(req.getParameter("descricao"));
        licoesAprendidas.setFase_origem(EtapaCiclo.valueOf(req.getParameter("fase_origem")));
        licoesAprendidas.setSeveridade(Intensidade.valueOf(req.getParameter("severidade")));
        licoesAprendidas.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));

        int resultado = DAO.inserir(licoesAprendidas);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser cadastrada");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        LicoesAprendidas licoesAprendidas = DAO.buscar(Long.parseLong(id));

        if (licoesAprendidas == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        req.setAttribute("licoesAprendidasList", List.of(licoesAprendidas));
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String faseParam = req.getParameter("fase");
        String severidadeParam = req.getParameter("severidade");
        String area = req.getParameter("area");
        String categoria = req.getParameter("categoria");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        EtapaCiclo fase = null;
        if (faseParam != null && !faseParam.isBlank()) fase = EtapaCiclo.valueOf(faseParam);

        Intensidade severidade = null;
        if (severidadeParam != null && !severidadeParam.isBlank()) severidade = Intensidade.valueOf(severidadeParam);

        List<LicoesAprendidas> licoesAprendidas = DAO.buscaFiltrada(busca, fase, severidade, area, categoria, ordem, direcao);

        req.setAttribute("licoesAprendidasList", licoesAprendidas);
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-licoesAprendidas"));
        LicoesAprendidas licoesAprendidas = new LicoesAprendidas();

        licoesAprendidas.setId_licao(id);
        licoesAprendidas.setTitulo(req.getParameter("titulo"));
        licoesAprendidas.setArea(normalizarArea(req.getParameter("area")));
        licoesAprendidas.setAprendizado(req.getParameter("aprendizado"));
        licoesAprendidas.setCategoria(normalizarCategoria(req.getParameter("categoria")));
        licoesAprendidas.setDescricao(req.getParameter("descricao"));
        licoesAprendidas.setFase_origem(EtapaCiclo.valueOf(req.getParameter("fase_origem")));
        licoesAprendidas.setSeveridade(Intensidade.valueOf(req.getParameter("severidade")));
        licoesAprendidas.setId_ciclo(Long.parseLong(req.getParameter("id_ciclo")));

        int resultado = DAO.atualizar(licoesAprendidas);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-licoesAprendidas"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser excluída");
    }

    // UTILITARIOS
    private String normalizarArea(String area) {
        if (area == null || area.isBlank()) return null;

        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return null;

        String limpa = categoria.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCategoriaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<LicoesAprendidas> licoesAprendidas = DAO.buscar();

        req.setAttribute("licoesAprendidasList", licoesAprendidas);
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }
}