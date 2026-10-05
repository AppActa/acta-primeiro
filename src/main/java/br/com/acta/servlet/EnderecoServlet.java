package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;
import br.com.acta.dao.EnderecoDAO;
import br.com.acta.model.Empresa;
import br.com.acta.model.Endereco;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "EnderecoServlet", value = "/endereco-servlet")
public class EnderecoServlet extends HttpServlet {
    private static final String PAGINA_ENDERECO = "/endereco.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final EnderecoDAO DAO = new EnderecoDAO();
    private final EmpresaDAO EMPRESA_DAO = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os enderecos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
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
                case null:
                    enviarErro(req, resp, "Ação não informada");
                    break;
                default:
                    enviarErro(req, resp, "Ação não existente");
            }

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir");
        }
    }

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Endereco endereco = new Endereco();

        endereco.setRua(req.getParameter("rua"));
        endereco.setBairro(req.getParameter("bairro"));
        endereco.setCidade(req.getParameter("cidade"));
        endereco.setEstado(req.getParameter("estado"));
        endereco.setCep(req.getParameter("cep"));
        endereco.setNumero(req.getParameter("numero"));
        endereco.setComplemento(req.getParameter("complemento"));
        endereco.setUnidade(req.getParameter("unidade"));
        endereco.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));

        int resultado = DAO.inserir(endereco);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        Endereco endereco = DAO.buscar(Long.parseLong(id));

        if (endereco == null) {
            enviarPaginaCerta(req, resp);
            return;
        }

        preencherNomesFk(List.of(endereco));
        req.setAttribute("enderecoList", List.of(endereco));
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-endereco"));
        Endereco endereco = new Endereco();

        endereco.setId_endereco(id);
        endereco.setRua(req.getParameter("rua"));
        endereco.setBairro(req.getParameter("bairro"));
        endereco.setCidade(req.getParameter("cidade"));
        endereco.setEstado(req.getParameter("estado"));
        endereco.setCep(req.getParameter("cep"));
        endereco.setNumero(req.getParameter("numero"));
        endereco.setComplemento(req.getParameter("complemento"));
        endereco.setUnidade(req.getParameter("unidade"));
        endereco.setId_empresa(Long.parseLong(req.getParameter("id_empresa")));

        int resultado = DAO.atualizar(endereco);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-endereco"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser excluído");
    }

    // UTILITARIOS
    private void enviarErro(HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
        req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Endereco> lista = DAO.buscar();
        preencherNomesFk(lista);

        req.setAttribute("enderecoList", lista);
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }

    private void preencherNomesFk(List<Endereco> lista) {
        for (Endereco endereco : lista) {
            Long idEmpresa = endereco.getId_empresa();
            Empresa empresa = EMPRESA_DAO.buscar(idEmpresa);

            if (empresa != null) {
                endereco.setNomeEmpresa(empresa.getNome());
            }
        }
    }

}