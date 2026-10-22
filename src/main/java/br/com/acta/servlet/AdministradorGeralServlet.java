package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdministradorGeralServlet", value = "/administrador-geral-servlet")
public class AdministradorGeralServlet extends HttpServlet {
    private static final String PAGINA_ADMINISTRADOR_GERAL = "/administrador-geral.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO DAO = new AdministradorGeralDAO();


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
            enviarErro(req, resp, "Não foi possível encontrar os administradores gerais.",
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

                case "atualizarSenha":
                    atualizarSenha(req, resp);
                    break;

                case "excluir":
                    excluir(req, resp);
                    break;

                case "autenticar":
                    autenticar(req, resp);
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

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setNome(req.getParameter("nome"));
        administradorGeral.setSenha(req.getParameter("senha"));
        administradorGeral.setEmail(req.getParameter("email"));
        administradorGeral.setTelefone(req.getParameter("telefone"));

        int resultado = DAO.inserir(administradorGeral);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        AdministradorGeral administradorGeral = DAO.buscar(Long.parseLong(id));

        if (administradorGeral == null) {
            throw new RegistroNaoEncontradoException("Registro de administrador não encontrado.");
        }

        req.setAttribute("administradorGeralList", List.of(administradorGeral));
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        List<AdministradorGeral> administradoresGerais = DAO.buscaFiltrada(busca, ordem, direcao);

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-adm-geral"));
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setAdm_geral_id(id);
        administradorGeral.setNome(req.getParameter("nome"));
        administradorGeral.setEmail(req.getParameter("email"));
        administradorGeral.setTelefone(req.getParameter("telefone"));

        int resultado = DAO.atualizar(administradorGeral);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser atualizado");
    }

    private void atualizarSenha(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String senhaAntiga = req.getParameter("senha_antiga");
        String senhaNova = req.getParameter("senha_nova");

        int resultado = DAO.atualizarSenha(email, senhaAntiga, senhaNova);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A senha não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id-adm-geral"));
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser excluído");
    }

    // AUTENTICACAO
    private void autenticar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");

        AdministradorGeral administradorGeral = DAO.autenticar(email, senha);

        if (administradorGeral == null) {
            enviarErro(req, resp, "E-mail ou senha inválidos");
            return;
        }

        // não guarda o hash da senha na sessão
        administradorGeral.setSenha(null);

        HttpSession sessao = req.getSession(true);
        sessao.setAttribute("administradorGeralLogado", administradorGeral);

        enviarPaginaCerta(req, resp);
    }

    // UTILITARIOS
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
            List<AdministradorGeral> administradoresGerais = DAO.buscar();

            req.setAttribute("administradorGeralList", administradoresGerais);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("administradorGeralList", List.of());
        }
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
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
        List<AdministradorGeral> administradoresGerais = DAO.buscar();

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }
}