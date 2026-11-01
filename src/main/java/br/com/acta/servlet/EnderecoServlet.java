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

/** Recebe as requisições de Endereco e prepara os dados para as páginas JSP. */
@WebServlet(name = "EnderecoServlet", value = "/endereco-servlet")
public class EnderecoServlet extends HttpServlet {
    private static final String PAGINA_ENDERECO = "/crud/endereco.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final EnderecoDAO DAO = new EnderecoDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
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

    /** Seleciona a ação do formulário e trata erros de validação e persistência. */
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

    /** Valida os campos do formulário antes de cadastrar o registro. */
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

    /** Busca pelo ID informado ou delega à listagem com filtros. */
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

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
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

    /** Valida os campos e atualiza o registro identificado no formulário. */
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

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-endereco"), "Id do endereço");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O endereco não pode ser excluído");
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarCidade(String cidade) {
        String limpa = cidade.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCidadeExistente(limpa);
        return existente != null ? existente : limpa;
    }

    /** Usa HTTP 500 quando o chamador não informa um status. */
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem
    ) throws ServletException, IOException {

        enviarErro(req, resp, mensagem,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /** Encaminha erros 404/500 à página de erro; os demais retornam à tela do cadastro. */
    private void enviarErro(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mensagem,
            int status
    ) throws ServletException, IOException {

        resp.setStatus(status);

        // Exibe a página de erro sem consultar o banco novamente.
        if (status == HttpServletResponse.SC_NOT_FOUND
                || status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            req.setAttribute(RequestDispatcher.ERROR_MESSAGE, mensagem);
            req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
            req.getRequestDispatcher(PAGINA_ERRO).forward(req, resp);
            return;
        }

        // Mantém a mensagem na tela do cadastro.
        req.setAttribute("mensagemErro", mensagem);

        // Se a recarga falhar, exibe listas vazias e preserva a mensagem.
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

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Endereco> lista = DAO.buscar();

        req.setAttribute("enderecoList", lista);
        req.setAttribute("cidadeList", DAO.buscarCidade());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_ENDERECO).forward(req, resp);
    }


}
