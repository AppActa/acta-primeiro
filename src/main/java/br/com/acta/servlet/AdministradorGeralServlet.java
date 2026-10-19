package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.model.AdministradorGeral;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdministradorGeralServlet", value = "/administrador-geral-servlet")
public class AdministradorGeralServlet extends HttpServlet {
    private static final String PAGINA_ADMINISTRADOR_GERAL = "/administrador-geral.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO DAO = new AdministradorGeralDAO();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os administradores gerais");
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
                case "atualizarSenha":
                    atualizarSenha(req, resp);
                    break;
                case "excluir":
                    excluir(req, resp);
                    break;
                case "autenticar":
                    autenticar(req, resp);
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
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setNome(req.getParameter("nome"));
        administradorGeral.setSenha(req.getParameter("senha"));
        administradorGeral.setEmail(req.getParameter("email"));
        administradorGeral.setTelefone(req.getParameter("telefone"));

        int resultado = DAO.inserir(administradorGeral);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        AdministradorGeral administradorGeral = DAO.buscar(Long.parseLong(id));

        if (administradorGeral == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        req.setAttribute("administradorGeralList", List.of(administradorGeral));
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-adm-geral"));
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setAdm_geral_id(id);
        administradorGeral.setNome(req.getParameter("nome"));
        administradorGeral.setEmail(req.getParameter("email"));
        administradorGeral.setTelefone(req.getParameter("telefone"));

        int resultado = DAO.atualizar(administradorGeral);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser atualizado");
    }

    private void atualizarSenha(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String senhaAntiga = req.getParameter("senha_antiga");
        String senhaNova = req.getParameter("senha_nova");

        int resultado = DAO.atualizarSenha(email, senhaAntiga, senhaNova);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A senha não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-adm-geral"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser excluído");
    }

    // AUTENTICACAO
    private void autenticar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");

        AdministradorGeral administradorGeral = DAO.autenticar(email, senha);

        if (administradorGeral == null) {
            enviarErro(req, resp, "E-mail ou senha inválidos");
            return;
        }

        // não guarda o hash da senha na sessão
        administradorGeral.setSenha(null);

        HttpSession sessao = req.getSession(true);
        sessao.setAttribute("administradorGeralLogado", administradorGeral);

        enviarPaginaCerta(req, resp);
    }

    // UTILITARIOS
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<AdministradorGeral> administradoresGerais = DAO.buscar();

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }
}