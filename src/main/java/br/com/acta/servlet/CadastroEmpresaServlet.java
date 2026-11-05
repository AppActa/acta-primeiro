package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;
import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import br.com.acta.excecoes.*;
import br.com.acta.model.Empresa;
import br.com.acta.model.Endereco;
import br.com.acta.utils.CsrfToken;
import br.com.acta.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Cria a empresa e o endereço sem criar conta ou conceder permissão administrativa. */
@WebServlet(name = "CadastroEmpresaServlet", value = {"/cadastro-empresa-servlet", "/autenticacao/cadastro-empresa.html", "/autenticacao/cadastro-empresa.jsp"})
public class CadastroEmpresaServlet extends HttpServlet {
    private final EmpresaDAO DAO = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-store");
        req.setAttribute("empresaDados", Map.of());
        req.getRequestDispatcher("/WEB-INF/views/autenticacao/cadastro-empresa.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        if (!CsrfToken.isValid(req)) {
            resp.sendError(403, "Atualize a página para enviar o cadastro.");
            return;
        }
        Map<String, String> dados = new HashMap<>();
        for (String nome : new String[]{"nome", "setor", "cnpj", "tamanho", "cep", "estado", "rua", "numero", "bairro", "cidade", "complemento", "unidade"}) {
            String valor = req.getParameter(nome);
            if (valor != null) dados.put(nome, valor.substring(0, Math.min(valor.length(), 2000)));
        }
        req.setAttribute("empresaDados", dados);
        String campoErro = null;
        try {
            Empresa empresa = new Empresa();
            campoErro = "nome";
            empresa.setNome(Validador.texto(req.getParameter("nome"), "Nome da empresa", 50));
            campoErro = "setor";
            empresa.setSetor(Validador.texto(req.getParameter("setor"), "Setor", 30));
            campoErro = "cnpj";
            empresa.setCnpj(Validador.cnpj(req.getParameter("cnpj")));
            campoErro = "tamanho";
            empresa.setTamanho(Validador.opcao(TamanhoEmpresa.class, req.getParameter("tamanho"), "Porte"));
            empresa.setStatus(Status.ATIVA);
            Endereco endereco = new Endereco();
            campoErro = "cep";
            endereco.setCep(Validador.cep(req.getParameter("cep")));
            campoErro = "estado";
            endereco.setEstado(Validador.uf(req.getParameter("estado")));
            campoErro = "rua";
            endereco.setRua(Validador.texto(req.getParameter("rua"), "Rua", 50));
            campoErro = "numero";
            endereco.setNumero(Validador.numeroEndereco(req.getParameter("numero")));
            campoErro = "bairro";
            endereco.setBairro(Validador.texto(req.getParameter("bairro"), "Bairro", 30));
            campoErro = "cidade";
            endereco.setCidade(Validador.nomePessoa(req.getParameter("cidade"), "Cidade", 30));
            campoErro = "complemento";
            endereco.setComplemento(Validador.textoLongoOpcional(req.getParameter("complemento"), "Complemento"));
            campoErro = "unidade";
            endereco.setUnidade(Validador.texto(req.getParameter("unidade"), "Unidade", 30));
            campoErro = null;
            Long id = DAO.inserirComEndereco(empresa, endereco);
            req.getSession().setAttribute("empresaCadastrada", "Empresa cadastrada. Confira seus dados e informe a senha para concluir o cadastro de colaborador.");
            resp.sendRedirect(req.getContextPath() + "/cadastro-servlet?empresa=" + id + "#etapa-2");
            return;
        } catch (ValidacaoException | IllegalArgumentException e) {
            req.setAttribute("campoErro", campoErro);
            resp.setStatus(400);
            req.setAttribute("mensagemErro", e.getMessage());
        } catch (DuplicidadeException | IntegridadeReferencialException e) {
            if (e instanceof DuplicidadeException) req.setAttribute("campoErro", "cnpj");
            resp.setStatus(409);
            req.setAttribute("mensagemErro", e.getMessage());
        } catch (RuntimeException e) {
            getServletContext().log("Erro no cadastro público de empresa.", e);
            resp.setStatus(500);
            req.setAttribute("mensagemErro", "Não foi possível cadastrar a empresa. Tente novamente.");
        }
        req.getRequestDispatcher("/WEB-INF/views/autenticacao/cadastro-empresa.jsp").forward(req, resp);
    }
}
