package br.com.acta.servlet;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.dao.EmpresaDAO;
import br.com.acta.dao.EnderecoDAO;
import br.com.acta.enums.Status;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
import br.com.acta.utils.CsrfToken;
import br.com.acta.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Consulta e edição da própria conta, identificada exclusivamente pela sessão. */
@WebServlet(name = "PerfilServlet", value = {"/perfil-servlet", "/perfil-colaborador.html", "/minha-empresa-servlet"})
public class PerfilServlet extends HttpServlet {
    private final ColaboradorDAO DAO = new ColaboradorDAO();
    private final EmpresaDAO EMPRESAS = new EmpresaDAO();
    private final EnderecoDAO ENDERECOS = new EnderecoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-store");
        Object aviso = req.getSession().getAttribute("perfilSucesso");
        if (aviso != null) {
            req.setAttribute("mensagemSucesso", aviso);
            req.getSession().removeAttribute("perfilSucesso");
        }
        if ("/minha-empresa-servlet".equals(req.getServletPath())) {
            Colaborador col = carregar(req, resp);
            if (col == null) return;
            req.setAttribute("colaborador", col);
            try {
                req.setAttribute("empresa", EMPRESAS.buscar(col.getId_empresa()));
                req.setAttribute("enderecos", ENDERECOS.buscarPorEmpresa(col.getId_empresa()));
            } catch (RuntimeException e) {
                getServletContext().log("Erro ao consultar a empresa vinculada.", e);
                resp.setStatus(503);
                req.setAttribute("empresaErro", "Não foi possível consultar a empresa. Tente novamente.");
                req.setAttribute("enderecos", java.util.List.of());
            }
            req.getRequestDispatcher("/ver-empresa.jsp").forward(req, resp);
            return;
        }
        if (req.getSession().getAttribute("usuarioLogado") instanceof AdministradorGeral) {
            req.getRequestDispatcher("/perfil-administrador.jsp").forward(req, resp);
            return;
        }
        Colaborador col = carregar(req, resp);
        if (col != null) mostrar(req, resp, col);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/minha-empresa-servlet".equals(req.getServletPath())) { resp.sendError(405); return; }
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        if (!CsrfToken.isValid(req)) { resp.sendError(403); return; }
        Colaborador col = carregar(req, resp);
        if (col == null) return;
        String campoErro = null;
        try {
            if ("atualizar".equals(req.getParameter("acao"))) {
                // Valida antes de modificar o objeto que também representa a sessão.
                campoErro = "nome";
                String nome = Validador.nomePessoa(req.getParameter("nome"), "Nome", 50);
                campoErro = "sobrenome";
                String sobrenome = Validador.nomePessoa(req.getParameter("sobrenome"), "Sobrenome", 50);
                campoErro = "email";
                String email = Validador.email(req.getParameter("email"));
                campoErro = "telefone";
                String telefone = Validador.telefone(req.getParameter("telefone"));
                col.setNome(nome);
                col.setSobrenome(sobrenome);
                col.setEmail(email);
                col.setTelefone(telefone);
                campoErro = null;
                DAO.atualizarPerfil(col);
            } else if ("alterarFoto".equals(req.getParameter("acao"))) {
                campoErro = "imagem";
                String imagem = req.getParameter("imagem");
                if (imagem == null || imagem.isBlank() || imagem.length() > 2048) throw new ValidacaoException("Selecione uma foto antes de salvar.");
                java.net.URI url = java.net.URI.create(imagem);
                if (!"https".equalsIgnoreCase(url.getScheme()) || !"res.cloudinary.com".equalsIgnoreCase(url.getHost())
                        || url.getPath() == null || !url.getPath().contains("/image/upload/")
                        || !url.getPath().contains("/acta/colaborador-profiles/")) {
                    throw new ValidacaoException("Envie uma imagem pelo botão Alterar foto.");
                }
                campoErro = null;
                DAO.atualizarImagem(col.getId_colaborador(), imagem);
                col.setImagem(imagem);
            } else if ("alterarSenha".equals(req.getParameter("acao"))) {
                campoErro = "senhaAtual";
                String atual = req.getParameter("senhaAtual");
                campoErro = "senhaNova";
                String nova = Validador.senha(req.getParameter("senhaNova"));
                campoErro = "confirmacaoSenha";
                if (!nova.equals(req.getParameter("confirmacaoSenha"))) throw new ValidacaoException("As senhas não coincidem.");
                campoErro = "senhaAtual";
                if (atual == null || atual.isEmpty() || atual.length() > 100
                        || DAO.atualizarSenha(col.getEmail(), atual, nova) != 1) {
                    throw new ValidacaoException("Senha atual incorreta.");
                }
                req.changeSessionId();
                req.getSession().removeAttribute(CsrfToken.SESSION_KEY);
                CsrfToken.getOrCreate(req.getSession());
            } else {
                throw new ValidacaoException("Ação inválida.");
            }
            col.setSenha(null);
            req.getSession().setAttribute("usuarioLogado", col);
            req.getSession().setAttribute("perfilSucesso", "Alterações salvas.");
            resp.sendRedirect(req.getContextPath() + "/perfil-servlet");
            return;
        } catch (ValidacaoException | IllegalArgumentException e) {
            req.setAttribute("campoErro", campoErro);
            resp.setStatus(400);
            req.setAttribute("mensagemErro", e.getMessage());
        } catch (DuplicidadeException e) {
            req.setAttribute("campoErro", "email");
            resp.setStatus(409);
            req.setAttribute("mensagemErro", "Este e-mail já está cadastrado.");
        } catch (RuntimeException e) {
            getServletContext().log("Erro ao atualizar o perfil do colaborador.", e);
            resp.setStatus(500);
            req.setAttribute("mensagemErro", "Não foi possível salvar as alterações.");
        }
        // Recarrega para nunca mostrar como salvo um valor rejeitado pelo banco.
        Colaborador atualizado = carregar(req, resp);
        if (atualizado != null) mostrar(req, resp, atualizado);
    }

    private Colaborador carregar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object usuario = req.getSession().getAttribute("usuarioLogado");
        if (!(usuario instanceof Colaborador sessao) || sessao.getId_colaborador() == null) {
            resp.sendError(403);
            return null;
        }
        try {
            Colaborador col = DAO.buscar(sessao.getId_colaborador());
            if (col == null || col.getStatus() != Status.ATIVA) {
                req.getSession().invalidate();
                resp.sendError(403, "Conta indisponível.");
                return null;
            }
            col.setSenha(null);
            req.getSession().setAttribute("usuarioLogado", col);
            return col;
        } catch (RuntimeException e) {
            getServletContext().log("Erro ao consultar o perfil do colaborador.", e);
            resp.sendError(503, "Perfil indisponível no momento.");
            return null;
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Colaborador col) throws ServletException, IOException {
        req.setAttribute("colaborador", col);
        req.getRequestDispatcher("/perfil-colaborador.jsp").forward(req, resp);
    }
}
