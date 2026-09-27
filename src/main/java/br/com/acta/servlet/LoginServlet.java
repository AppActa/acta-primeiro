package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login-servlet")
public class LoginServlet extends HttpServlet {
    private static final String PAGINA_LOGIN = "/login.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ColaboradorDAO COLABORADOR_DAO = new ColaboradorDAO();
    private final AdministradorGeralDAO ADMINISTRADOR_DAO = new AdministradorGeralDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String acao = req.getParameter("acao");

        try {
            switch (acao) {
                case "login":
                    login(req, resp);
                    break;
                case null:
                    enviarErro(req, resp, "Ação não informada");
                default:
                    enviarErro(req, resp, "Ação não existente");
            }
        } catch (Exception e) {
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");

        AdministradorGeral administradorGeral = ADMINISTRADOR_DAO.autenticar(email, senha);
        if (administradorGeral != null) {
            req.getSession().setAttribute("usuarioLogado", administradorGeral);
            req.getSession().setAttribute("tipoUsuario", "ADMINISTRADOR_GERAL");
            resp.sendRedirect(req.getContextPath() + "/home-servlet");
            return;
        }

        Colaborador colaborador = COLABORADOR_DAO.autenticar(email, senha);
        if (colaborador != null) {
            req.getSession().setAttribute("usuarioLogado", colaborador);
            req.getSession().setAttribute("tipoUsuario", "COLABORADOR");
            resp.sendRedirect(req.getContextPath() + "/home-servlet");
            return;
        }

        enviarErroLogin(req, resp, "Email ou senha inválidos");
    }

    // erro específico do login
    private void enviarErroLogin(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute("mensagemErro", mensagem);
        req.getRequestDispatcher(PAGINA_LOGIN).forward(req, resp);
    }

    // erro genérico
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }
}