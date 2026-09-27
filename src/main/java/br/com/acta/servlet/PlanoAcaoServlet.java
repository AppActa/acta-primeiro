package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;
import br.com.acta.dao.PlanoAcaoDAO;
import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.PlanoAcao;
import br.com.acta.model.Colaborador;
import br.com.acta.model.Ciclo;
import br.com.acta.enums.Status;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "PlanoAcao", value = "/planoAcao-servlet")
public class PlanoAcaoServlet {
    private static String PAGINA_PLANOACAO = "/planoAcao.jsp";
    private static String PAGINA_ERRO = "/erro.jsp";
    private final PlanoAcaoDAO DAO = new PlanoAcaoDAO();
    private final CicloDAO CICLO_DAO = new CicloDAO();
    private final ColaboradorDAO COLABORADOR_DAO = new ColaboradorDAO();


    //UTILITARIOS
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE,mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<PlanoAcao> planosAcoes = DAO.buscar();
        preencherNomesFk(planosAcoes);

        req.setAttribute("planoAcaoList", planosAcoes);
        req.getRequestDispatcher(PAGINA_PLANOACAO).forward(req, resp);
    }

    private void preencherNomesFk(List<PlanoAcao> planosAcoes){
        for(PlanoAcao planoAcao : planosAcoes){

            Ciclo ciclo = CICLO_DAO.buscar(planoAcao.getId_ciclo());
            if (ciclo != null) planoAcao.setNomeCiclo(ciclo.getNome());

            Colaborador criador = COLABORADOR_DAO.buscar(planoAcao.getId_criador());
            if (criador != null) planoAcao.setNomeColaborador(criador.getNome());
        }
    }

}
