package br.com.acta.servlet;

import br.com.acta.model.AdministradorGeral;
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
@WebFilter(filterName = "LoginFilter", urlPatterns = {
        "/crud/*", "/administrador-geral.jsp",
        "/empresa-servlet", "/endereco-servlet", "/colaborador-servlet",
        "/ciclo-servlet", "/problema-servlet", "/meta-servlet",
        "/planoAcao-servlet", "/tarefa-servlet", "/licoes-aprendidas-servlet",
        "/administrador-geral-servlet", "/cloudinary-signature"
}, dispatcherTypes = {
        DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
        DispatcherType.ERROR, DispatcherType.ASYNC
})
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Impede reutilizar respostas administrativas em cache depois do logout.
        resp.setHeader("Cache-Control", "no-store");
        HttpSession sessao = req.getSession(false);
        Object usuario = sessao == null ? null : sessao.getAttribute("usuarioLogado");

        if (usuario == null) {
            if (req.getDispatcherType() == DispatcherType.REQUEST
                    && ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        if (!(usuario instanceof AdministradorGeral administrador)
                || administrador.getAdm_geral_id() == null
                || administrador.getAdm_geral_id() <= 0) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}
