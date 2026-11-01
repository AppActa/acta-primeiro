package br.com.acta;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import java.io.*;

/** Servlet de exemplo que responde com uma mensagem em HTML. */
@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    private String message;

    /** Define a mensagem quando o servlet é inicializado pelo container. */
    public void init() {
        message = "Hello World!";
    }

    /** Escreve a resposta HTML para uma requisição GET. */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Monta um documento HTML simples com a mensagem inicial.
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    /** Encerra o ciclo de vida; este exemplo não possui recursos para liberar. */
    public void destroy() {}
}
