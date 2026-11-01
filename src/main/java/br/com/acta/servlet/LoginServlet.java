package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.CsrfToken;
import br.com.acta.utils.Validador;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Autentica administradores e limita tentativas por endereço de origem. */
@WebServlet(name = "LoginServlet", value = "/login-servlet")
public class LoginServlet extends HttpServlet {

    // Até cinco falhas por IP em uma janela de quinze minutos.
    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_MS = 15 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Tentativas> TENTATIVAS = new ConcurrentHashMap<>();
    private static final String PAGINA_LOGIN = "/login.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO ADMINISTRADOR_DAO = new AdministradorGeralDAO();

    /** Verifica o token CSRF e encaminha a ação de login. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token de segurança inválido. Atualize a página.");
            return;
        }
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
            e.printStackTrace();
            enviarErroLogin(req, resp, "Erro ao acessar o banco de dados. Tente novamente.");
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    /** Confere credenciais e inicia a sessão do administrador autenticado. */
    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String chaveTentativa = req.getRemoteAddr();
        Tentativas tentativas = TENTATIVAS.computeIfAbsent(chaveTentativa, ignorado -> new Tentativas());
        // Protege a consulta ao contador contra requisições simultâneas.
        synchronized (tentativas) {
            tentativas.resetarSeExpirou();
            if (tentativas.quantidade >= MAX_TENTATIVAS) {
                resp.setStatus(429);
                req.setAttribute("mensagemErro", "Muitas tentativas. Aguarde 15 minutos e tente novamente.");
                req.getRequestDispatcher(PAGINA_LOGIN).forward(req, resp);
                return;
            }
        }
        String email = Validador.obrigatorio(req.getParameter("email"), "E-mail");
        String senha = Validador.obrigatorio(req.getParameter("senha"), "Senha");

        AdministradorGeral adm = ADMINISTRADOR_DAO.autenticar(email, senha);
        if (adm == null) {
            synchronized (tentativas) {
                tentativas.registrarFalha();
            }
            enviarErroLogin(req, resp, "Email ou senha inválidos");
            return;
        }
        TENTATIVAS.remove(chaveTentativa);

        // Evita manter o hash da senha no objeto salvo na sessão.
        adm.setSenha(null);
        req.getSession(true);
        // Renova o identificador após a autenticação.
        req.changeSessionId();
        req.getSession().setAttribute("usuarioLogado", adm);
        resp.sendRedirect(req.getContextPath() + "/empresa-servlet");
    }

    /** Volta à página de login com a mensagem para o usuário. */
    private void enviarErroLogin(
            HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute("mensagemErro", mensagem);
        req.getRequestDispatcher(PAGINA_LOGIN).forward(req, resp);
    }

    /** Exibe a página de erro com status HTTP 500. */
    private void enviarErro(
            HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    /** Mantém a contagem de falhas e o início da janela de quinze minutos. */
    private static final class Tentativas {
        private int quantidade;
        private long inicio = Instant.now().toEpochMilli();

        /** Zera a contagem quando a janela de tentativas expira. */
        private void resetarSeExpirou() {
            if (Instant.now().toEpochMilli() - inicio >= JANELA_MS) {
                quantidade = 0;
                inicio = Instant.now().toEpochMilli();
            }
        }

        /** Renova a janela se necessário e contabiliza uma tentativa inválida. */
        private void registrarFalha() {
            resetarSeExpirou();
            quantidade++;
        }
    }
}
