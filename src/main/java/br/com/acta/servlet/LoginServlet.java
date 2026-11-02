package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.enums.Status;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
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

/** Autentica as contas existentes e limita tentativas por endereço de origem. */
@WebServlet(name = "LoginServlet", value = "/login-servlet")
public class LoginServlet extends HttpServlet {

    // Até cinco falhas por IP em uma janela de quinze minutos.
    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_MS = 15 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Tentativas> TENTATIVAS = new ConcurrentHashMap<>();
    private static final String PAGINA_LOGIN = "/autenticacao/login.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO ADMINISTRADOR_DAO = new AdministradorGeralDAO();
    private final ColaboradorDAO COLABORADOR_DAO = new ColaboradorDAO();

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

    /** Identifica a conta pelas credenciais verificadas, nunca por parâmetros de permissão. */
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
        req.setAttribute("campoErro", "email");
        String email = Validador.email(req.getParameter("email"));
        req.setAttribute("campoErro", "senha");
        String senha = req.getParameter("senha");
        if (senha == null || senha.isEmpty() || senha.length() > 100) {
            throw new ValidacaoException("Informe o e-mail e a senha.");
        }

        req.removeAttribute("campoErro");
        AdministradorGeral adm = ADMINISTRADOR_DAO.autenticar(email, senha);
        Colaborador colaborador = adm == null ? COLABORADOR_DAO.autenticar(email, senha) : null;
        if (adm == null && (colaborador == null || colaborador.getStatus() != Status.ATIVA)) {
            synchronized (tentativas) {
                tentativas.registrarFalha();
            }
            req.setAttribute("campoErro", "senha");
            enviarErroLogin(req, resp, "Email ou senha inválidos");
            return;
        }
        TENTATIVAS.remove(chaveTentativa);

        // Evita manter o hash da senha no objeto salvo na sessão.
        if (adm != null) adm.setSenha(null);
        else colaborador.setSenha(null);
        req.getSession(true);
        // Renova o identificador após a autenticação.
        req.changeSessionId();
        req.getSession().setAttribute("usuarioLogado", adm != null ? adm : colaborador);
        req.getSession().removeAttribute(CsrfToken.SESSION_KEY);
        CsrfToken.getOrCreate(req.getSession());
        resp.sendRedirect(req.getContextPath() + "/index.jsp");
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
