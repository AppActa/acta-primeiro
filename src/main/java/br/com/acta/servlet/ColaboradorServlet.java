package br.com.acta.servlet;

import br.com.acta.dao.EmpresaDAO;

import br.com.acta.dao.ColaboradorDAO;
import br.com.acta.model.Colaborador;
import br.com.acta.enums.Status;
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
import java.sql.Date;
import java.util.List;

/** Recebe as requisições de Colaborador e prepara os dados para as páginas JSP. */
@WebServlet(name = "ColaboradorServlet", value = "/colaborador-servlet")
public class ColaboradorServlet extends HttpServlet {
    private static final String PAGINA_COLABORADORES = "/crud/colaborador.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ColaboradorDAO DAO = new ColaboradorDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar os colaboradores.",
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

                case "atualizarPorCpf":
                    atualizarPorCpf(req, resp);
                    break;

                case "excluir":
                    excluir(req, resp);
                    break;

                case "excluirPorEmail":
                    excluirPorEmail(req, resp);
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
        Colaborador colaborador = new Colaborador();

        colaborador.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        colaborador.setSobrenome(Validador.nomePessoa(req.getParameter("sobrenome"), "Sobrenome", 50));
        colaborador.setPermissao_gestor(Validador.booleano(req.getParameter("permissao_gestor")));
        colaborador.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        colaborador.setCargo(normalizarCargo(Validador.texto(req.getParameter("cargo"), "Cargo", 100)));
        colaborador.setDt_contratacao(Validador.dataNaoFutura(req.getParameter("dt_contratacao"), "Data de contratação"));
        colaborador.setEmail(Validador.email(req.getParameter("email")));
        colaborador.setSenha(Validador.senha(req.getParameter("senha")));
        colaborador.setTelefone(Validador.telefone(req.getParameter("telefone")));
        colaborador.setCpf(Validador.cpf(req.getParameter("cpf")));
        colaborador.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));

        colaborador.setArea(normalizarArea(Validador.texto(req.getParameter("area"), "Área", 30)));

        int resultado = DAO.inserir(colaborador);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser cadastrado");
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null || id.isBlank()) {
            buscaFiltrada(req, resp);
            return;
        }

        Colaborador colaborador = DAO.buscar(Validador.id(id, "Id"));

        if (colaborador == null) {
            throw new RegistroNaoEncontradoException("Registro de colaborador não encontrado.");
        }

        req.setAttribute("colaboradorList", List.of(colaborador));
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String statusParam = req.getParameter("status");
        String area = req.getParameter("area");
        String cargo = req.getParameter("cargo");
        String gestorParam = req.getParameter("gestor");
        String dataInicialParam = req.getParameter("dataInicial");
        String dataFinalParam = req.getParameter("dataFinal");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        // Filtro ausente permanece null e não restringe a consulta.
        Status status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Status.valueOf(statusParam);
        }

        Boolean permissaoGestor = null;
        if (gestorParam != null && !gestorParam.isBlank()) {
            permissaoGestor = Boolean.parseBoolean(gestorParam);
        }

        Date dataInicial = Validador.dataOpcional(dataInicialParam, "Data inicial");

        Date dataFinal = Validador.dataOpcional(dataFinalParam, "Data final");

        Validador.periodo(dataInicial, dataFinal, "A data final");

        List<Colaborador> colaboradores = DAO.buscaFiltrada(busca, status, area, cargo, permissaoGestor,
                dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-colaborador"), "Id do colaborador");
        Colaborador colaborador = new Colaborador();

        colaborador.setId_colaborador(id);
        colaborador.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        colaborador.setSobrenome(Validador.nomePessoa(req.getParameter("sobrenome"), "Sobrenome", 50));
        colaborador.setPermissao_gestor(Validador.booleano(req.getParameter("permissao_gestor")));
        colaborador.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        colaborador.setCargo(normalizarCargo(Validador.texto(req.getParameter("cargo"), "Cargo", 100)));
        colaborador.setDt_contratacao(Validador.dataNaoFutura(req.getParameter("dt_contratacao"), "Data de contratação"));
        colaborador.setEmail(Validador.email(req.getParameter("email")));
        colaborador.setTelefone(Validador.telefone(req.getParameter("telefone")));
        colaborador.setCpf(Validador.cpf(req.getParameter("cpf")));
        colaborador.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));

        colaborador.setArea(normalizarArea(Validador.texto(req.getParameter("area"), "Área", 30)));

        int resultado = DAO.atualizar(colaborador);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser atualizado");
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-colaborador"), "Id do colaborador");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser excluído");
    }

    /** Usa o CPF atual para localizar o colaborador e valida os novos dados. */
    private void atualizarPorCpf(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String cpfAtual = Validador.cpf(req.getParameter("cpf-atual"));
        Colaborador colaborador = new Colaborador();

        colaborador.setNome(Validador.nomePessoa(req.getParameter("nome"), "Nome", 50));
        colaborador.setSobrenome(Validador.nomePessoa(req.getParameter("sobrenome"), "Sobrenome", 50));
        colaborador.setPermissao_gestor(Validador.booleano(req.getParameter("permissao_gestor")));
        colaborador.setStatus(Validador.opcao(Status.class, req.getParameter("status"), "Status"));
        colaborador.setCargo(normalizarCargo(Validador.texto(req.getParameter("cargo"), "Cargo", 100)));
        colaborador.setDt_contratacao(Validador.dataNaoFutura(
                req.getParameter("dt_contratacao"), "Data de contratação"));
        colaborador.setEmail(Validador.email(req.getParameter("email")));
        colaborador.setTelefone(Validador.telefone(req.getParameter("telefone")));
        colaborador.setCpf(Validador.cpf(req.getParameter("cpf")));
        colaborador.setId_empresa(Validador.id(req.getParameter("id_empresa"), "Empresa"));
        colaborador.setArea(normalizarArea(Validador.texto(req.getParameter("area"), "Área", 30)));

        int resultado = DAO.atualizarPorCpf(colaborador, cpfAtual);
        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser atualizado pelo CPF.");
    }

    /** Valida o e-mail informado e solicita a exclusão do colaborador. */
    private void excluirPorEmail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = Validador.email(req.getParameter("email-exclusao"));
        int resultado = DAO.excluirPorEmail(email);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser excluído pelo e-mail.");
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarArea(String area) {
        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarCargo(String cargo) {
        String limpa = cargo.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCargoExistente(limpa);
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
            List<Colaborador> colaboradores = DAO.buscar();

            req.setAttribute("colaboradorList", colaboradores);
            req.setAttribute("areaList", DAO.buscarAreas());
            req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
            req.setAttribute("cargoList", DAO.buscarCargos());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("colaboradorList", List.of());
            req.setAttribute("areaList", List.of());
            req.setAttribute("empresaOpcoes", List.of());
            req.setAttribute("cargoList", List.of());
        }
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<Colaborador> colaboradores = DAO.buscar();

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }
}
