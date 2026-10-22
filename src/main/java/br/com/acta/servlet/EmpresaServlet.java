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
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import java.io.IOException;
import java.util.List;


@WebServlet(name = "EmpresaServlet", value = "/empresa-servlet")
public class EmpresaServlet extends HttpServlet {
    private static final String PAGINA_EMPRESA = "/empresa.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private static final EmpresaDAO DAO = new EmpresaDAO();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            buscar(req, resp);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_NOT_FOUND);

        } catch (ValidacaoException | IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro de consulta inválido.",
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro ao consultar o banco de dados.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar as empresas.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada.",
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            switch (acao) {
                case "inserir":
                    inserir(req, resp);
                    break;

                case "atualizar":
                    atualizar(req, resp);
                    break;

                case "excluir":
                    excluir(req, resp);
                    break;

                default:
                    enviarErro(req, resp, "Ação não existente.",
                            HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (ValidacaoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_NOT_FOUND);

        } catch (DuplicidadeException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_CONFLICT);

        } catch (IntegridadeReferencialException e) {
            enviarErro(req, resp, e.getMessage(),
                    HttpServletResponse.SC_CONFLICT);

        } catch (IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro inválido.",
                    HttpServletResponse.SC_BAD_REQUEST);

        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro interno ao acessar o banco de dados.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir a operação.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Empresa empresa = new Empresa();

        empresa.setNome(req.getParameter("nome"));
        empresa.setSetor(normalizarSetor(req.getParameter("setor")));
        empresa.setCnpj(req.getParameter("cnpj"));
        empresa.setStatus(Status.valueOf(req.getParameter("status")));
        empresa.setTamanho(TamanhoEmpresa.valueOf(req.getParameter("tamanho")));

        int resultado = DAO.inserir(empresa);

        if(resultado == 1) enviarPaginaCerta(req,resp);
        else enviarErro(req, resp, "A empresa não pode ser cadastrada");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        String id = req.getParameter("id");

        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Empresa empresa = DAO.buscar(Long.parseLong(id));

        if (empresa == null) {
            throw new RegistroNaoEncontradoException(
                    "Empresa não encontrada."
            );
        }

        req.setAttribute("empresaList", List.of(empresa));
        req.setAttribute("setorList", DAO.buscarSetor());
        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String setor = req.getParameter("setor");
        String statusParam = req.getParameter("status");
        String tamanhoParam = req.getParameter("tamanho");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        Status status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Status.valueOf(statusParam);
        }
        TamanhoEmpresa tamanho = null;
        if (tamanhoParam != null && !tamanhoParam.isBlank()) {
            tamanho = TamanhoEmpresa.valueOf(tamanhoParam);
        }

        List<Empresa> empresas = DAO.buscaFiltrada(busca, setor, status, tamanho, ordem, direcao);
        req.setAttribute("empresaList", empresas);
        req.setAttribute("setorList", DAO.buscarSetor());
        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-empresa"));
        Empresa empresa = new Empresa();

        empresa.setId_empresa(id);
        empresa.setNome(req.getParameter("nome"));
        empresa.setSetor(normalizarSetor(req.getParameter("setor")));
        empresa.setCnpj(req.getParameter("cnpj"));
        empresa.setStatus(Status.valueOf(req.getParameter("status")));
        empresa.setTamanho(TamanhoEmpresa.valueOf(req.getParameter("tamanho")));

        int resultado = DAO.atualizar(empresa);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser atualizada");

    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-empresa"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser excluída");
    }

    //UTILITARIOS
    private String normalizarSetor(String setor) {
        String limpa = setor.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarSetorExistente(limpa);
        return existente != null ? existente : limpa;
    }



    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);
        req.setAttribute("mensagemErro", mensagem);

        // Não pode depender do banco: se recarregar a lista falhar, mostra a tela vazia com a mensagem
        try {
            req.setAttribute("empresaList", DAO.buscar());
            req.setAttribute("setorList", DAO.buscarSetor());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("empresaList", List.of());
            req.setAttribute("setorList", List.of());
        }
        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req, resp);
    }

    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }



    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Empresa> empresas = DAO.buscar();

        req.setAttribute("empresaList", empresas);
        req.setAttribute("setorList", DAO.buscarSetor());
        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req, resp);
    }
}
