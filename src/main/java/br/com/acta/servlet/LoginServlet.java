package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.excecoes.DaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.utils.Validador;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login-servlet")
public class LoginServlet extends HttpServlet {
    private static final String PAGINA_LOGIN = "/login.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO ADMINISTRADOR_DAO = new AdministradorGeralDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada");
                return;
            }

            switch (acao) {
                case "login":
                    login(req, resp);
                    break;
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (ValidacaoException e) {
            enviarErroLogin(req, resp, e.getMessage());
        } catch (DaoException e) {
            e.printStackTrace(); // ou logger
            enviarErroLogin(req, resp, "Erro ao acessar o banco de dados. Tente novamente.");
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validador.obrigatorio(req.getParameter("email"), "E-mail");
        String senha = Validador.obrigatorio(req.getParameter("senha"), "Senha");

        AdministradorGeral adm = ADMINISTRADOR_DAO.autenticar(email, senha);
        if (adm == null) {
            enviarErroLogin(req, resp, "Email ou senha inválidos");
            return;
        }

        adm.setSenha(null);
        req.getSession(true);
        req.changeSessionId();
        req.getSession().setAttribute("usuarioLogado", adm);
        resp.sendRedirect(req.getContextPath() + "/empresa-servlet"); // tela que existe
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
