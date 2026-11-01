package br.com.acta.servlet;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.CsrfToken;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
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
            "/logout-servlet"
        },
        dispatcherTypes = {DispatcherType.REQUEST})
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
                + "script-src 'self'; connect-src 'self' https://api.cloudinary.com; "
                + "form-action 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'");
        // Consulta a sessão existente sem criar uma para visitantes.
        HttpSession sessao = req.getSession(false);
        Object usuario = sessao == null ? null : sessao.getAttribute("usuarioLogado");

        // Navegação anônima vai ao login; outras requisições recebem HTTP 401.
        if (usuario == null) {
            if (req.getDispatcherType() == DispatcherType.REQUEST
                    && ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        // A autorização depende do objeto autenticado no servidor.
        if (!(usuario instanceof AdministradorGeral administrador) || administrador.getAdm_geral_id() == null
                || administrador.getAdm_geral_id() <= 0) {
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
