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
@WebServlet(name = "AdministradorGeralServlet", value = "/administrador-geral-servlet")
public class AdministradorGeralServlet extends HttpServlet {

    private static final String PAGINA_ADMINISTRADOR_GERAL = "/administrador-geral.jsp";
    private static final String PAGINA_CONFIGURACOES = "/crud/configuracoes.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final AdministradorGeralDAO DAO = new AdministradorGeralDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            buscar(req, resp);
        } catch (RegistroNaoEncontradoException e) {
            enviarErro(req, resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ValidacaoException | IllegalArgumentException e) {
            enviarErro(req, resp, "Parâmetro de consulta inválido.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DaoException e) {
            e.printStackTrace();
            enviarErro(req, resp, "Erro ao consultar o banco de dados.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível encontrar os administradores gerais.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        List<AdministradorGeral> administradoresGerais = DAO.buscaFiltrada(busca, ordem, direcao);

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
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
        administradorGeral.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        administradorGeral.setEmail(Validador.email(req.getParameter("email")));
        administradorGeral.setTelefone(Validador.telefone(req.getParameter("telefone")));
        administradorGeral.setImagem(validarImagemCloudinary(req.getParameter("imagem")));

        int resultado = DAO.atualizar(administradorGeral);

        if (resultado == 1) {
            logado.setNome(administradorGeral.getNome());
            logado.setEmail(administradorGeral.getEmail());
            logado.setTelefone(administradorGeral.getTelefone());
            logado.setImagem(administradorGeral.getImagem());
            req.getSession(false).setAttribute("usuarioLogado", logado);
            resp.sendRedirect(req.getContextPath() + PAGINA_CONFIGURACOES);
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
        String senhaAntiga = Validador.obrigatorio(req.getParameter("senha_antiga"), "Senha atual");
        String senhaNova = Validador.senha(req.getParameter("senha_nova"));
        String senhaConfirmar = Validador.obrigatorio(req.getParameter("senha_confirmar"), "Confirmação da senha");
        if (!senhaNova.equals(senhaConfirmar)) {
            enviarErro(req, resp, "A confirmação da nova senha não confere.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int resultado = DAO.atualizarSenha(email, senhaAntiga, senhaNova);

        if (resultado == 1) {
            resp.sendRedirect(req.getContextPath() + PAGINA_CONFIGURACOES);
        } else {
            enviarErro(req, resp, "A senha não pode ser atualizada");
        }
    }

    /** Obtém a identidade da sessão; redireciona ao login se estiver ausente. */
    private AdministradorGeral obterAdministradorLogado(
            HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sessao = req.getSession(false);
        AdministradorGeral logado = sessao == null ? null : (AdministradorGeral) sessao.getAttribute("usuarioLogado");
        if (logado == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
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
            req.getRequestDispatcher(PAGINA_CONFIGURACOES).forward(req, resp);
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
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(
            HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<AdministradorGeral> administradoresGerais = DAO.buscar();

        req.setAttribute("administradorGeralList", administradoresGerais);
        req.getRequestDispatcher(PAGINA_ADMINISTRADOR_GERAL).forward(req, resp);
    }
}
