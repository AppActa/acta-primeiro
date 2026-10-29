package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.utils.Validador;
import jakarta.servlet.RequestDispatcher;
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

        administradorGeral.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        administradorGeral.setSenha(Validador.senha(req.getParameter("senha")));
        administradorGeral.setEmail(Validador.email(req.getParameter("email")));
        administradorGeral.setTelefone(Validador.telefone(req.getParameter("telefone")));

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

        AdministradorGeral administradorGeral = DAO.buscar(Validador.id(id, "Id"));

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
        Long id = Validador.id(req.getParameter("id-adm-geral"), "Id do administrador");
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setAdm_geral_id(id);
        administradorGeral.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        administradorGeral.setEmail(Validador.email(req.getParameter("email")));
        administradorGeral.setTelefone(Validador.telefone(req.getParameter("telefone")));

        int resultado = DAO.atualizar(administradorGeral);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser atualizado");
    }

    private void atualizarSenha(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validador.email(req.getParameter("email"));
        String senhaAntiga = Validador.obrigatorio(req.getParameter("senha_antiga"), "Senha atual");
        String senhaNova = Validador.senha(req.getParameter("senha_nova"));

        int resultado = DAO.atualizarSenha(email, senhaAntiga, senhaNova);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A senha não pode ser atualizada");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-adm-geral"), "Id do administrador");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O administrador geral não pode ser excluído");
    }

    // AUTENTICACAO
    private void autenticar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Validador.obrigatorio(req.getParameter("email"), "E-mail");
        String senha = Validador.obrigatorio(req.getParameter("senha"), "Senha");

        AdministradorGeral administradorGeral = DAO.autenticar(email, senha);

        if (administradorGeral == null) {
            enviarErro(req, resp, "E-mail ou senha inválidos");
            return;
        }

        // não guarda o hash da senha na sessão
        administradorGeral.setSenha(null);

        HttpSession sessao = req.getSession(true);
        req.changeSessionId();
        sessao.setAttribute("administradorGeralLogado", administradorGeral);

        enviarPaginaCerta(req, resp);
    }

    // UTILITARIOS
    // SOBRECARGA 1 (3 parâmetros): erro sem status definido.
    // Assume 500 (erro grave), então o usuário é levado para a página de erro.
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    // SOBRECARGA 2 (4 parâmetros): quem realmente trata o erro. O destino depende do status:
    //  - 404 (registro não encontrado) e 500 (erro de banco/inesperado): vai para a página de erro (/erro.jsp)
    //  - 400 (dado inválido) e 409 (duplicado/em uso): volta para a tela da entidade, mostrando a mensagem
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);

        // Erros graves: página de erro (não consulta o banco, então funciona mesmo com ele fora do ar)
        if (status == HttpServletResponse.SC_NOT_FOUND
                || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Erros do usuário: volta para a tela com a mensagem
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


    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<AdministradorGeral> administradoresGerais = DAO.buscar();

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }
}
