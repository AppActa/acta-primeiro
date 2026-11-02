package br.com.acta.servlet;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.dao.EmpresaDAO;
import br.com.acta.enums.Status;
import br.com.acta.excecoes.*;
import br.com.acta.model.Colaborador;
import br.com.acta.model.Empresa;
import br.com.acta.utils.CsrfToken;
import br.com.acta.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Cadastro público de colaborador comum, separado do CRUD administrativo. */
@WebServlet(name = "CadastroServlet", value = {"/cadastro-servlet", "/cadastro.html", "/autenticacao/cadastro.html"})
public class CadastroServlet extends HttpServlet {
    private final ColaboradorDAO DAO = new ColaboradorDAO();
    private final EmpresaDAO EMPRESAS = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-store");
        if (req.getSession().getAttribute("usuarioLogado") != null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        Map<String, String> dados = new HashMap<>();
        Object rascunho = req.getSession().getAttribute("cadastroColaborador");
        if (rascunho instanceof Map<?, ?> mapa) {
            for (var item : mapa.entrySet()) {
                if (item.getKey() instanceof String chave && item.getValue() instanceof String valor) dados.put(chave, valor);
            }
        }
        String empresa = req.getParameter("empresa");
        if (empresa != null && empresa.matches("[0-9]{1,10}")) dados.put("id_empresa", empresa);
        req.setAttribute("cadastroDados", dados);
        Object aviso = req.getSession().getAttribute("empresaCadastrada");
        if (aviso != null) {
            req.setAttribute("mensagemSucesso", aviso);
            req.getSession().removeAttribute("empresaCadastrada");
        }
        mostrar(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        if (!CsrfToken.isValid(req)) {
            resp.sendError(403, "Atualize a página para enviar o cadastro.");
            return;
        }
        if (req.getSession().getAttribute("usuarioLogado") != null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        Map<String, String> dados = new HashMap<>();
        for (String nome : new String[]{"nome", "sobrenome", "email", "telefone", "cpf", "area", "cargo", "dt_contratacao", "id_empresa"}) {
            String valor = req.getParameter(nome);
            if (valor != null) dados.put(nome, valor.substring(0, Math.min(valor.length(), 200)));
        }
        req.setAttribute("cadastroDados", dados);
        if ("empresa".equals(req.getParameter("acao"))) {
            // Guarda apenas o rascunho de apresentação; nunca guarda a senha.
            req.getSession().setAttribute("cadastroColaborador", dados);
            resp.sendRedirect(req.getContextPath() + "/cadastro-empresa-servlet");
            return;
        }
        String campoErro = null;
        try {
            if (!"inserir".equals(req.getParameter("acao"))) throw new ValidacaoException("Ação inválida.");
            Colaborador col = new Colaborador();
            campoErro = "nome";
            col.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
            campoErro = "sobrenome";
            col.setSobrenome(Validador.nomePessoa(req.getParameter("sobrenome"), "Sobrenome", 50));
            campoErro = "email";
            col.setEmail(Validador.email(req.getParameter("email")));
            campoErro = "senha";
            col.setSenha(Validador.senha(req.getParameter("senha")));
            campoErro = "telefone";
            col.setTelefone(Validador.telefone(req.getParameter("telefone")));
            campoErro = "cpf";
            col.setCpf(Validador.cpf(req.getParameter("cpf")));
            campoErro = "area";
            col.setArea(Validador.texto(req.getParameter("area"), "Área", 30));
            campoErro = "cargo";
            col.setCargo(Validador.texto(req.getParameter("cargo"), "Cargo", 100));
            campoErro = "dt_contratacao";
            col.setDt_contratacao(Validador.dataNaoFutura(req.getParameter("dt_contratacao"), "Data de contratação"));
            campoErro = "id_empresa";
            col.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));
            col.setStatus(Status.ATIVA);
            col.setPermissao_gestor(false);
            Empresa empresa = EMPRESAS.buscar(col.getId_empresa());
            if (empresa == null || empresa.getStatus() != Status.ATIVA) throw new ValidacaoException("Selecione uma empresa ativa.");
            campoErro = null;
            if (DAO.inserir(col) != 1) throw new DaoException("Cadastro não concluído.", new IllegalStateException());
            Colaborador autenticado = null;
            try {
                autenticado = DAO.autenticar(col.getEmail(), col.getSenha());
            } catch (RuntimeException e) {
                // O cadastro já foi gravado; uma falha no login não deve pedir novo cadastro.
                getServletContext().log("Cadastro concluído, mas o login automático falhou.", e);
            }
            req.getSession().removeAttribute("cadastroColaborador");
            if (autenticado == null) {
                resp.sendRedirect(req.getContextPath() + "/autenticacao/login.jsp");
                return;
            }
            autenticado.setSenha(null);
            req.changeSessionId();
            req.getSession().setAttribute("usuarioLogado", autenticado);
            req.getSession().removeAttribute(CsrfToken.SESSION_KEY);
            CsrfToken.getOrCreate(req.getSession());
            req.getSession().setAttribute("cadastroSucesso", "Cadastro concluído com sucesso.");
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } catch (ValidacaoException | IllegalArgumentException e) {
            req.setAttribute("campoErro", campoErro);
            resp.setStatus(400);
            req.setAttribute("mensagemErro", e.getMessage());
            mostrar(req, resp);
        } catch (DuplicidadeException | IntegridadeReferencialException e) {
            req.setAttribute("campoErro", e instanceof DuplicidadeException ? "email,cpf" : "id_empresa");
            resp.setStatus(409);
            req.setAttribute("mensagemErro", e.getMessage());
            mostrar(req, resp);
        } catch (RuntimeException e) {
            getServletContext().log("Erro no cadastro público de colaborador.", e);
            resp.setStatus(500);
            req.setAttribute("mensagemErro", "Não foi possível concluir o cadastro. Tente novamente.");
            mostrar(req, resp);
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("empresaOpcoes", EMPRESAS.buscaFiltrada(null, null, Status.ATIVA, null, "nome", "ASC"));
        } catch (RuntimeException e) {
            getServletContext().log("Erro ao consultar empresas para cadastro.", e);
            resp.setStatus(503);
            req.setAttribute("empresaOpcoes", java.util.List.of());
            req.setAttribute("mensagemErro", "Empresas indisponíveis no momento. Tente novamente.");
        }
        req.getRequestDispatcher("/autenticacao/cadastro.jsp").forward(req, resp);
    }
}
