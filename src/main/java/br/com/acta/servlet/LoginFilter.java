package br.com.acta.servlet;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
import br.com.acta.utils.CsrfToken;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Autoriza o CRUD somente a partir da identidade autenticada no servidor. */
@WebFilter(filterName = "LoginFilter",
        urlPatterns = {
            "/crud/*",
            "/administrador-geral.jsp",
            "/empresa-servlet",
            "/endereco-servlet",
            "/colaborador-servlet",
            "/ciclo-servlet",
            "/problema-servlet",
            "/meta-servlet",
            "/planoAcao-servlet",
            "/tarefa-servlet",
            "/licoes-aprendidas-servlet",
            "/administrador-geral-servlet",
            "/cloudinary-signature",
            "/perfil-servlet",
            "/perfil-colaborador.jsp",
            "/perfil-colaborador.html",
            "/logout-servlet"
        },
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE})
public class LoginFilter implements Filter {

    /** Exige administrador com ID válido e, nos POSTs, token CSRF da sessão. */
    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Impede reutilizar respostas administrativas em cache depois do logout.
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' https://res.cloudinary.com data:; "
                + "style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; "
                + "script-src 'self'; connect-src 'self' https://api.cloudinary.com https://viacep.com.br; "
                + "form-action 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'");
        // Consulta a sessão existente sem criar uma para visitantes.
        HttpSession sessao = req.getSession(false);
        Object usuario = sessao == null ? null : sessao.getAttribute("usuarioLogado");

        // Navegação anônima vai ao login; outras requisições recebem HTTP 401.
        if (usuario == null) {
            if (req.getDispatcherType() == DispatcherType.REQUEST
                    && ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))) {
                resp.sendRedirect(req.getContextPath() + "/autenticacao/login.jsp");
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        // Perfil e logout aceitam ambos os tipos; o restante continua exclusivo do administrador.
        String caminho = req.getServletPath();
        if (req.getDispatcherType() == DispatcherType.INCLUDE
                && req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH) instanceof String incluido) {
            caminho = incluido;
        }
        boolean pessoal = caminho.equals("/perfil-servlet") || caminho.equals("/perfil-colaborador.jsp")
                || caminho.equals("/perfil-colaborador.html") || caminho.equals("/logout-servlet");
        boolean administrador = usuario instanceof AdministradorGeral adm
                && adm.getAdm_geral_id() != null && adm.getAdm_geral_id() > 0;
        boolean colaborador = usuario instanceof Colaborador col
                && col.getId_colaborador() != null && col.getId_colaborador() > 0 && col.getStatus() == Status.ATIVA;
        if (!administrador && !(pessoal && colaborador)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Alterações exigem o token associado à sessão autenticada.
        if ("POST".equalsIgnoreCase(req.getMethod()) && !CsrfToken.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token de segurança inválido ou expirado.");
            return;
        }

        // Encaminha ao destino somente após todas as verificações.
        chain.doFilter(request, response);
    }
}
