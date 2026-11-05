package br.com.acta.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Mantém os links antigos da landing integrados à sessão no servidor. */
@WebServlet(name = "InicioServlet", value = {"", "/inicio", "/index.html", "/index.jsp"})
public class InicioServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Antecipa o CSS, o tema importado e a imagem principal sem guardar dados da sessão.
        String contexto = req.getContextPath();
        resp.setHeader("Cache-Control", "no-store");
        resp.addHeader("Link", "<" + contexto + "/assets/css/paginas/inicio.css>; rel=preload; as=style");
        resp.addHeader("Link", "<" + contexto + "/assets/css/base/tema-acta.css>; rel=preload; as=style");
        resp.addHeader("Link", "<" + contexto + "/assets/images/mockup_celulares.png>; rel=preload; as=image");
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }
}
