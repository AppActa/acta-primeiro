package br.com.acta.servlet;


import br.com.acta.dao.EmpresaDAO;
import br.com.acta.model.Empresa;
import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


@WebServlet(name = "EmpresaServlet", value = "/empresa-servlet")
public class EmpresaServlet extends HttpServlet {
    private static final String PAGINA_EMPRESA = "/empresa.jsp";
    private static final String PAGINA_ERRO = "erro.jsp";
    private static final EmpresaDAO DAO = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException{

        try{
            buscar(req,resp);
        }catch(Exception e){
            e.printStackTrace();
            enviarErro(req,resp,"Não foi possível encontrar as empresas.");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException{
        String acao = req.getParameter("acao");

        try{
            switch (acao){
                case "inserir":
                    inserir(req,resp);
                    break;
                case "atualizar":
                    atualizar(req,resp);
                    break;
                case "excluir":
                    excluir(req, resp);
                case null:
                    enviarErro(req,resp,"Ação não informada.");
                default:
                    enviarErro(req,resp,"Ação não existente");
            }
        }catch(Exception e){
            enviarErro(req,resp,"Não foi possível concluir");
        }
    }

        //CRUD
        private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Empresa empresa = new Empresa();

            empresa.setNome(req.getParameter("nome"));
            empresa.setSetor(req.getParameter("setor"));
            empresa.setCnpj(req.getParameter("cnpj"));
            empresa.setStatus(Status.valueOf(req.getParameter("status")));
            empresa.setTamanho(TamanhoEmpresa.valueOf(req.getParameter("tamanho")));

            int resultado = DAO.inserir(empresa);

            if(resultado == 1) enviarPaginaCerta(req,resp);
            else req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
        }

        private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException{
        String id = req.getParameter("id");

        if(id == null){
            enviarPaginaCerta(req,resp);
            return;
        }

        Empresa empresa = DAO.buscar(Long.parseLong("id"));

        if(empresa == null){
            enviarPaginaCerta(req,resp);
            return;
        }

        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
        }

        private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        Long id = Long.parseLong(req.getParameter("id-empresa"));
        Empresa empresa = new Empresa();

        empresa.setId_empresa(id);
        empresa.setNome(req.getParameter("nome"));
        empresa.setSetor(req.getParameter("setor"));
        empresa.setCnpj(req.getParameter("cnpj"));
        empresa.setStatus(Status.valueOf(req.getParameter("status")));
        empresa.setTamanho(TamanhoEmpresa.valueOf(req.getParameter("tamanho")));

            int resultado = DAO.atualizar(empresa);
            if (resultado == 1) enviarPaginaCerta(req, resp);
            else enviarErro(req, resp, "A empresa não pode ser adicionada");

        }

        private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        Long id = Long.parseLong(req.getParameter("id-empresa"));
        int resultado = DAO.excluir(id);

        if(resultado == 1) enviarPaginaCerta(req,resp);
        else enviarErro(req,resp,"A empresa não pode ser excluída");
        }

        //UTILITARIOS
        private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException{
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE,mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
        }

        private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        List<Empresa> empresas = DAO.buscar();
        }


}
