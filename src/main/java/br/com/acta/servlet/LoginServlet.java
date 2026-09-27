package br.com.acta.servlet;


import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.dao.EmpresaDAO;
import br.com.acta.model.Colaborador;
import br.com.acta.model.Empresa;
import br.com.acta.enums.Status;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;


public class LoginServlet {
    private static final String PAGINA_LOGIN = "/login.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ColaboradorDAO COLABORADOR_DAO = new ColaboradorDAO();
    private final AdministradorGeralDAO ADMINISTRADOR_DAO = new AdministradorGeralDAO();

    private void login(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String email = req.getParameter("email");

        if (email == null) {
            enviarErro(req,resp, "Insira o email");
            return;
        }

        Colaborador colaborador
    }

    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

}
