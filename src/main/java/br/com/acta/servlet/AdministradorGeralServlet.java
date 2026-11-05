package br.com.acta.servlet;

import br.com.acta.dao.AdministradorGeralDAO;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.Validador;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/** Recebe as requisições de AdministradorGeral e prepara os dados para as páginas JSP. */
@WebServlet(name = "AdministradorGeralServlet", value = {"/administrador-geral-servlet", "/administrador-geral.jsp", "/crud/configuracoes.jsp"})
public class AdministradorGeralServlet extends HttpServlet {

    private static final String PAGINA_ADMINISTRADOR_GERAL = "/WEB-INF/views/crud/configuracoes.jsp";
    private static final String PAGINA_CONFIGURACOES = "/WEB-INF/views/crud/configuracoes.jsp";
    private static final String PAGINA_PERFIL = "/WEB-INF/views/perfis/administrador.jsp";
    private static final String PAGINA_ERRO = "/WEB-INF/views/erros/erro.jsp";
    private final AdministradorGeralDAO DAO = new AdministradorGeralDAO();

    /** Abre as configurações da própria conta administrativa. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (prepararPerfil(req, resp)) {
            req.getRequestDispatcher(PAGINA_CONFIGURACOES).forward(req, resp);
        }
    }

    /** Permite editar o próprio perfil e senha; bloqueia cadastro, exclusão e login nesta rota. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            switch (acao) {
                case "inserir":
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "O cadastro de administradores não está disponível por esta rota.");
                    break;

                case "atualizar":
                    atualizar(req, resp);
                    break;

                case "atualizarSenha":
                    atualizarSenha(req, resp);
                    break;

                case "excluir":
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "A exclusão de administradores não está disponível por esta rota.");
                    break;

                case "autenticar":
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Use a rota de login para autenticar.");
                    break;

                default:
                    enviarErro(req, resp, "Ação não existente.", HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (ValidacaoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (DuplicidadeException e) {
            req.setAttribute("campoErro", "email");
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (IntegridadeReferencialException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro inválido.", HttpServletResponse.SC_BAD_REQUEST);
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

    /** Monta o cadastro; este método não é chamado pelas ações públicas desta rota. */
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        administradorGeral.setSenha(Validador.senha(req.getParameter("senha")));
        administradorGeral.setEmail(Validador.email(req.getParameter("email")));
        administradorGeral.setTelefone(Validador.telefone(req.getParameter("telefone")));

        int resultado = DAO.inserir(administradorGeral);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "O administrador geral não pode ser cadastrado");
        }
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
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
        if (prepararPerfil(req, resp)) req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        List<AdministradorGeral> administradoresGerais = DAO.buscaFiltrada(busca, ordem, direcao);

        req.setAttribute("administradorGeralList", administradoresGerais);
        if (prepararPerfil(req, resp)) req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    /** Confere se o ID é do administrador logado e sincroniza o perfil da sessão. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdministradorGeral logado = obterAdministradorLogado(req, resp);
        if (logado == null) {
            return;
        }
        Long id = Validador.id(req.getParameter("id-adm-geral"), "Id do administrador");
        if (!id.equals(logado.getAdm_geral_id())) {
            enviarErro(req, resp, "Não é permitido atualizar outro administrador.", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        AdministradorGeral administradorGeral = new AdministradorGeral();

        administradorGeral.setAdm_geral_id(id);
        req.setAttribute("campoErro", "nome");
        administradorGeral.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        req.setAttribute("campoErro", "email");
        administradorGeral.setEmail(Validador.email(req.getParameter("email")));
        req.setAttribute("campoErro", "telefone");
        administradorGeral.setTelefone(Validador.telefone(req.getParameter("telefone")));
        req.removeAttribute("campoErro");
        administradorGeral.setImagem(validarImagemCloudinary(req.getParameter("imagem")));

        int resultado = DAO.atualizar(administradorGeral);

        if (resultado == 1) {
            logado.setNome(administradorGeral.getNome());
            logado.setEmail(administradorGeral.getEmail());
            logado.setTelefone(administradorGeral.getTelefone());
            logado.setImagem(administradorGeral.getImagem());
            req.getSession(false).setAttribute("usuarioLogado", logado);
            req.getSession().setAttribute("perfilSucesso", "Alterações salvas.");
            resp.sendRedirect(req.getContextPath() + ("perfil".equals(req.getParameter("retorno")) ? "/perfil-servlet" : "/administrador-geral-servlet"));
        } else {
            enviarErro(req, resp, "O administrador geral não pode ser atualizado");
        }
    }

    /** Valida a identidade, a nova senha e a confirmação antes de solicitar a troca. */
    private void atualizarSenha(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdministradorGeral logado = obterAdministradorLogado(req, resp);
        if (logado == null) {
            return;
        }
        String email = Validador.email(req.getParameter("email"));
        if (!email.equalsIgnoreCase(logado.getEmail())) {
            enviarErro(req, resp, "Não é permitido alterar a senha de outro administrador.",
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setAttribute("campoErro", "senha_antiga");
        String senhaAntiga = req.getParameter("senha_antiga");
        if (senhaAntiga == null || senhaAntiga.isEmpty() || senhaAntiga.length() > 100) {
            throw new ValidacaoException("Informe a senha atual.");
        }
        req.setAttribute("campoErro", "senha_nova");
        String senhaNova = Validador.senha(req.getParameter("senha_nova"));
        req.setAttribute("campoErro", "senha_confirmar");
        String senhaConfirmar = req.getParameter("senha_confirmar");
        if (!senhaNova.equals(senhaConfirmar)) {
            enviarErro(req, resp, "A confirmação da nova senha não confere.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        req.setAttribute("campoErro", "senha_antiga");
        int resultado = DAO.atualizarSenha(email, senhaAntiga, senhaNova);

        if (resultado == 1) {
            req.getSession().setAttribute("perfilSucesso", "Alterações salvas.");
            resp.sendRedirect(req.getContextPath() + ("perfil".equals(req.getParameter("retorno")) ? "/perfil-servlet" : "/administrador-geral-servlet"));
        } else {
            enviarErro(req, resp, "Senha atual incorreta.", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /** Obtém a identidade da sessão; redireciona ao login se estiver ausente. */
    private AdministradorGeral obterAdministradorLogado(
            HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sessao = req.getSession(false);
        AdministradorGeral logado = sessao == null ? null : (AdministradorGeral) sessao.getAttribute("usuarioLogado");
        if (logado == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        return logado;
    }

    /** Aceita foto opcional em HTTPS no domínio e no caminho de imagens do Cloudinary. */
    private String validarImagemCloudinary(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(valor.trim());
            if ("https".equalsIgnoreCase(uri.getScheme()) && "res.cloudinary.com".equalsIgnoreCase(uri.getHost())
                    && uri.getPath() != null && uri.getPath().contains("/image/upload/")) {
                return uri.toString();
            }
        } catch (IllegalArgumentException ignored) {
            // URL inválida; cai na validação abaixo.
        }
        throw new ValidacaoException("A foto precisa ser uma URL HTTPS válida do Cloudinary.");
    }

    /** Executa a exclusão por ID; a ação pública correspondente está bloqueada. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-adm-geral"), "Id do administrador");
        int resultado = DAO.excluir(id);

        if (resultado == 1) {
            enviarPaginaCerta(req, resp);
        } else {
            enviarErro(req, resp, "O administrador geral não pode ser excluído");
        }
    }

    /** Cria uma sessão autenticada; a ação pública desta rota direciona ao login próprio. */
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
        sessao.setAttribute("usuarioLogado", administradorGeral);

        enviarPaginaCerta(req, resp);
    }

    /** Retorna somente a uma das duas paginas locais do proprio administrador. */
    private String paginaRetorno(HttpServletRequest req) {
        return "perfil".equals(req.getParameter("retorno")) ? PAGINA_PERFIL : PAGINA_CONFIGURACOES;
    }

    /** Prepara os dados comuns aos perfis administrativos antes da apresentação. */
    static boolean prepararPerfil(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object usuario = req.getSession().getAttribute("usuarioLogado");
        if (!(usuario instanceof AdministradorGeral administrador)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        try {
            req.setAttribute("empresasVinculadas", new AdministradorGeralDAO().buscarEmpresasAtivas(administrador.getAdm_geral_id()));
            return true;
        } catch (DaoException e) {
            req.getServletContext().log("Erro ao consultar empresas vinculadas ao administrador.", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Erro ao consultar as empresas vinculadas.");
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return false;
        }
    }

    /** Usa HTTP 500 quando o chamador não informa um status. */
    private void enviarErro(
            HttpServletRequest req, HttpServletResponse resp, String mensagem) throws ServletException, IOException {
        enviarErro(req, resp, mensagem, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /** Encaminha erros 404/500 à página de erro; os demais retornam à tela do cadastro. */
    private void enviarErro(
            HttpServletRequest req, HttpServletResponse resp, String mensagem, int status)
            throws ServletException, IOException {
        resp.setStatus(status);

        // Exibe a página de erro sem consultar o banco novamente.
        if (status == HttpServletResponse.SC_NOT_FOUND || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Mantém a mensagem na tela do cadastro.
        req.setAttribute("mensagemErro", mensagem);

        String acao = req.getParameter("acao");
        if ("atualizar".equals(acao) || "atualizarSenha".equals(acao)) {
            if (prepararPerfil(req, resp)) req.getRequestDispatcher(paginaRetorno(req)).forward(req, resp);
            return;
        }

        // Se a recarga falhar, exibe listas vazias e preserva a mensagem.
        try {
            List<AdministradorGeral> administradoresGerais = DAO.buscar();

            req.setAttribute("administradorGeralList", administradoresGerais);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("administradorGeralList", List.of());
        }
        if (prepararPerfil(req, resp)) req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(
            HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<AdministradorGeral> administradoresGerais = DAO.buscar();

        req.setAttribute("administradorGeralList", administradoresGerais);
        if (prepararPerfil(req, resp)) req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }
}
