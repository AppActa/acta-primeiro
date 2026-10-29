package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;

import br.com.acta.dao.EnderecoDAO;
import br.com.acta.model.Endereco;
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

import java.io.IOException;
import java.util.List;

@WebServlet(name = "EnderecoServlet", value = "/endereco-servlet")
public class EnderecoServlet extends HttpServlet {
    private static final String PAGINA_ENDERECO = "/crud/endereco.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final EnderecoDAO DAO = new EnderecoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
            enviarErro(req, resp, "Não foi possível encontrar os endereços.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String acao = req.getParameter("acao");

        try {
            if (acao == null) {
                enviarErro(req, resp, "Ação não informada.", HttpServletResponse.SC_BAD_REQUEST);
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
            enviarErro(req, resp, "Erro interno ao acessar o banco de dados.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(req, resp, "Não foi possível concluir a operação.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Endereco endereco = new Endereco();

        endereco.setRua(Validador.texto(req.getParameter("rua"), "Rua", 50));
        endereco.setBairro(Validador.texto(req.getParameter("bairro"), "Bairro", 30));
        endereco.setCidade(normalizarCidade(Validador.nomePessoa(req.getParameter("cidade"), "Cidade", 30)));
        endereco.setEstado(Validador.uf(req.getParameter("estado")));
        endereco.setCep(Validador.cep(req.getParameter("cep")));
        endereco.setNumero(Validador.numeroEndereco(req.getParameter("numero")));
        endereco.setComplemento(Validador.textoLongoOpcional(req.getParameter("complemento"), "Complemento"));
        endereco.setUnidade(Validador.texto(req.getParameter("unidade"), "Unidade", 30));
        endereco.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));

        int resultado = DAO.inserir(endereco);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser cadastrado");
    }

    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Endereco endereco = DAO.buscar(Validador.id(id, "Id"));

        if (endereco == null) {
            throw new RegistroNaoEncontradoException("Registro de endereço não encontrado.");
        }

        req.setAttribute("enderecoList", List.of(endereco));
        req.setAttribute("cidadeList", DAO.buscarCidade());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String estado = req.getParameter("estado");
        String cidade = req.getParameter("cidade");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        List<Endereco> lista = DAO.buscaFiltrada(busca, estado, cidade, ordem, direcao);

        req.setAttribute("enderecoList", lista);
        req.setAttribute("cidadeList", DAO.buscarCidade());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-endereco"), "Id do endereço");
        Endereco endereco = new Endereco();

        endereco.setId_endereco(id);
        endereco.setRua(Validador.texto(req.getParameter("rua"), "Rua", 50));
        endereco.setBairro(Validador.texto(req.getParameter("bairro"), "Bairro", 30));
        endereco.setCidade(normalizarCidade(Validador.nomePessoa(req.getParameter("cidade"), "Cidade", 30)));
        endereco.setEstado(Validador.uf(req.getParameter("estado")));
        endereco.setCep(Validador.cep(req.getParameter("cep")));
        endereco.setNumero(Validador.numeroEndereco(req.getParameter("numero")));
        endereco.setComplemento(Validador.textoLongoOpcional(req.getParameter("complemento"), "Complemento"));
        endereco.setUnidade(Validador.texto(req.getParameter("unidade"), "Unidade", 30));
        endereco.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));

        int resultado = DAO.atualizar(endereco);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser atualizado");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-endereco"), "Id do endereço");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser excluído");
    }

    // UTILITARIOS
    private String normalizarCidade(String cidade) {
        String limpa = cidade.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCidadeExistente(limpa);
        return existente != null ? existente : limpa;
    }

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
            List<Endereco> lista = DAO.buscar();

            req.setAttribute("enderecoList", lista);
            req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
            req.setAttribute("cidadeList", DAO.buscarCidade());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("enderecoList", List.of());
            req.setAttribute("empresaOpcoes", List.of());
            req.setAttribute("cidadeList", List.of());
        }
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }

    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Endereco> lista = DAO.buscar();

        req.setAttribute("enderecoList", lista);
        req.setAttribute("cidadeList", DAO.buscarCidade());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }


}
