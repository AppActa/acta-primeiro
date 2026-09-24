package br.com.acta.servlet;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.dao.EmpresaDAO;
import br.com.acta.model.Colaborador;
import br.com.acta.model.Empresa;
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
    private final EmpresaDAO EMPRESA_DAO = new EmpresaDAO();

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
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (Exception e) {
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    // CRUD
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Colaborador colaborador = new Colaborador();

        colaborador.setNome(req.getParameter("nome"));
        colaborador.setSobrenome(req.getParameter("sobrenome"));
        colaborador.setPermissao_gestor(Boolean.parseBoolean(req.getParameter("permissao_gestor")));
        colaborador.setArea(req.getParameter("area"));
        colaborador.setCargo(req.getParameter("cargo"));
        colaborador.setDt_contratacao(Date.valueOf(req.getParameter("dt_contratacao")));
        colaborador.setEmail(req.getParameter("email"));
        colaborador.setSenha(req.getParameter("senha"));
        colaborador.setTelefone(req.getParameter("telefone"));
        colaborador.setCpf(req.getParameter("cpf"));
        colaborador.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));

        int resultado = DAO.inserir(colaborador);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            preencherNomesFk(List.of(colaborador));
            req.setAttribute("colaboradorList", List.of(colaborador));
            req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
        }
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        Colaborador colaborador = DAO.buscar(Long.parseLong(id));

        if (colaborador == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        preencherNomesFk(List.of(colaborador));
        req.setAttribute("colaboradorList", List.of(colaborador));
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-colaborador"));
        Colaborador colaborador = new Colaborador();

        colaborador.setId_colaborador(id);
        colaborador.setNome(req.getParameter("nome"));
        colaborador.setSobrenome(req.getParameter("sobrenome"));
        colaborador.setPermissao_gestor(Boolean.parseBoolean(req.getParameter("permissao_gestor")));
        colaborador.setArea(req.getParameter("area"));
        colaborador.setCargo(req.getParameter("cargo"));
        colaborador.setDt_contratacao(Date.valueOf(req.getParameter("dt_contratacao")));
        colaborador.setEmail(req.getParameter("email"));
        colaborador.setSenha(req.getParameter("senha"));
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
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Colaborador> colaboradores = DAO.buscar();
        preencherNomesFk(colaboradores);

        req.setAttribute("colaboradorList", colaboradores);
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    private void preencherNomesFk(List<Colaborador> colaboradores) {
        for (Colaborador colaborador : colaboradores) {
            Long idEmpresa = colaborador.getId_empresa();
            Empresa empresa = EMPRESA_DAO.buscar(idEmpresa);

            if (empresa != null) {
                colaborador.setNomeEmpresa(empresa.getNome());
            }
        }
    }
}