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
@WebServlet(name = "CadastroEmpresaServlet", value = {"/cadastro-empresa-servlet", "/autenticacao/cadastro-empresa.html"})
public class CadastroEmpresaServlet extends HttpServlet {
    private final EmpresaDAO DAO = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-store");
        req.setAttribute("empresaDados", Map.of());
        req.getRequestDispatcher("/autenticacao/cadastro-empresa.jsp").forward(req, resp);
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
        try {
            Empresa empresa = new Empresa();
            empresa.setNome(Validador.texto(req.getParameter("nome"), "Nome da empresa", 50));
            empresa.setSetor(Validador.texto(req.getParameter("setor"), "Setor", 30));
            empresa.setCnpj(Validador.cnpj(req.getParameter("cnpj")));
            empresa.setTamanho(Validador.opcao(TamanhoEmpresa.class, req.getParameter("tamanho"), "Porte"));
            empresa.setStatus(Status.ATIVA);
            Endereco endereco = new Endereco();
            endereco.setCep(Validador.cep(req.getParameter("cep")));
            endereco.setEstado(Validador.uf(req.getParameter("estado")));
            endereco.setRua(Validador.texto(req.getParameter("rua"), "Rua", 50));
            endereco.setNumero(Validador.numeroEndereco(req.getParameter("numero")));
            endereco.setBairro(Validador.texto(req.getParameter("bairro"), "Bairro", 30));
            endereco.setCidade(Validador.nomePessoa(req.getParameter("cidade"), "Cidade", 30));
            endereco.setComplemento(Validador.textoLongoOpcional(req.getParameter("complemento"), "Complemento"));
            endereco.setUnidade(Validador.texto(req.getParameter("unidade"), "Unidade", 30));
            Long id = DAO.inserirComEndereco(empresa, endereco);
            req.getSession().setAttribute("empresaCadastrada", "Empresa cadastrada. Confira seus dados e informe a senha para concluir o cadastro de colaborador.");
            resp.sendRedirect(req.getContextPath() + "/cadastro-servlet?empresa=" + id);
            return;
        } catch (ValidacaoException | IllegalArgumentException e) {
            resp.setStatus(400);
            req.setAttribute("mensagemErro", e.getMessage());
        } catch (DuplicidadeException | IntegridadeReferencialException e) {
            resp.setStatus(409);
            req.setAttribute("mensagemErro", e.getMessage());
        } catch (RuntimeException e) {
            getServletContext().log("Erro no cadastro público de empresa.", e);
            resp.setStatus(500);
            req.setAttribute("mensagemErro", "Não foi possível cadastrar a empresa. Tente novamente.");
        }
        req.getRequestDispatcher("/autenticacao/cadastro-empresa.jsp").forward(req, resp);
    }
}
