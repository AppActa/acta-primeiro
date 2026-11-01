package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;
import br.com.acta.model.Empresa;
import br.com.acta.enums.Status;
import br.com.acta.enums.TamanhoEmpresa;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import br.com.acta.excecoes.DaoException;
import br.com.acta.excecoes.DuplicidadeException;
import br.com.acta.excecoes.IntegridadeReferencialException;
import br.com.acta.excecoes.RegistroNaoEncontradoException;
import br.com.acta.excecoes.ValidacaoException;
import br.com.acta.utils.Validador;
import java.io.IOException;
import java.util.List;

/** Recebe as requisições de Empresa e prepara os dados para as páginas JSP. */
@WebServlet(name = "EmpresaServlet", value = "/empresa-servlet")
public class EmpresaServlet extends HttpServlet {
    private static final String PAGINA_EMPRESA = "/crud/empresa.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private static final EmpresaDAO DAO = new EmpresaDAO();

    /** Atende consultas e converte as falhas em respostas HTTP. */
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
            enviarErro(req, resp, "Não foi possível encontrar as empresas.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

                case "atualizarPorCnpj":
                    atualizarPorCnpj(req, resp);
                    break;

                case "excluir":
                    excluir(req, resp);
                    break;

                case "excluirPorCnpj":
                    excluirPorCnpj(req, resp);
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

    /** Valida os campos do formulário antes de cadastrar o registro. */
    private void inserir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Empresa empresa = new Empresa();

        empresa.setNome(Validador.texto(req.getParameter("nome"), "Nome", 50));
        empresa.setCnpj(Validador.cnpj(req.getParameter("cnpj")));
        empresa.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        empresa.setTamanho(Validador.opcao(TamanhoEmpresa.class, req.getParameter("tamanho"), "Tamanho"));

        empresa.setSetor(normalizarSetor(Validador.texto(req.getParameter("setor"), "Setor", 30)));

        int resultado = DAO.inserir(empresa);

        if(resultado == 1) enviarPaginaCerta(req,resp);
        else enviarErro(req, resp, "A empresa não pode ser cadastrada");
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        String id = req.getParameter("id");

        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        Empresa empresa = DAO.buscar(Validador.id(id, "Id"));

        if (empresa == null) {
            throw new RegistroNaoEncontradoException(
                    "Empresa não encontrada."
            );
        }

        req.setAttribute("empresaList", List.of(empresa));
        req.setAttribute("setorList", DAO.buscarSetor());

        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String setor = req.getParameter("setor");
        String statusParam = req.getParameter("status");
        String tamanhoParam = req.getParameter("tamanho");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        // Filtro ausente permanece null e não restringe a consulta.
        Status status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Status.valueOf(statusParam);
        }
        TamanhoEmpresa tamanho = null;
        if (tamanhoParam != null && !tamanhoParam.isBlank()) {
            tamanho = TamanhoEmpresa.valueOf(tamanhoParam);
        }

        List<Empresa> empresas = DAO.buscaFiltrada(busca, setor, status, tamanho, ordem, direcao);
        req.setAttribute("empresaList", empresas);
        req.setAttribute("setorList", DAO.buscarSetor());

        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req,resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-empresa"), "Id da empresa");
        Empresa empresa = new Empresa();

        empresa.setId_empresa(id);
        empresa.setNome(Validador.texto(req.getParameter("nome"), "Nome", 50));
        empresa.setCnpj(Validador.cnpj(req.getParameter("cnpj")));
        empresa.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        empresa.setTamanho(Validador.opcao(TamanhoEmpresa.class, req.getParameter("tamanho"), "Tamanho"));

        empresa.setSetor(normalizarSetor(Validador.texto(req.getParameter("setor"), "Setor", 30)));

        int resultado = DAO.atualizar(empresa);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser atualizada");

    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-empresa"), "Id da empresa");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser excluída");
    }

    /** Usa o CNPJ atual para localizar a empresa e valida os novos dados. */
    private void atualizarPorCnpj(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String cnpjAtual = Validador.cnpj(req.getParameter("cnpj-atual"));
        Empresa empresa = new Empresa();

        empresa.setNome(Validador.texto(req.getParameter("nome"), "Nome", 50));
        empresa.setCnpj(Validador.cnpj(req.getParameter("cnpj")));
        empresa.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        empresa.setTamanho(Validador.opcao(TamanhoEmpresa.class, req.getParameter("tamanho"), "Tamanho"));
        empresa.setSetor(normalizarSetor(Validador.texto(req.getParameter("setor"), "Setor", 30)));

        int resultado = DAO.atualizarPorCnpj(empresa, cnpjAtual);
        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser atualizada pelo CNPJ.");
    }

    /** Valida o CNPJ informado e solicita a exclusão da empresa. */
    private void excluirPorCnpj(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String cnpj = Validador.cnpj(req.getParameter("cnpj-exclusao"));
        int resultado = DAO.excluirPorCnpj(cnpj);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A empresa não pode ser excluída pelo CNPJ.");
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarSetor(String setor) {
        String limpa = setor.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarSetorExistente(limpa);
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
            req.setAttribute("empresaList", DAO.buscar());
            req.setAttribute("setorList", DAO.buscarSetor());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("empresaList", List.of());
            req.setAttribute("setorList", List.of());
        }
        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Empresa> empresas = DAO.buscar();

        req.setAttribute("empresaList", empresas);
        req.setAttribute("setorList", DAO.buscarSetor());

        req.getRequestDispatcher(PAGINA_EMPRESA).forward(req, resp);
    }

}
