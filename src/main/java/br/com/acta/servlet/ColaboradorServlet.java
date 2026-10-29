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

@WebServlet(name = "ColaboradorServlet", value = "/colaborador-servlet")
public class ColaboradorServlet extends HttpServlet {
    private static final String PAGINA_COLABORADORES = "/crud/colaborador.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final ColaboradorDAO DAO = new ColaboradorDAO();

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

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = errado, -1 = erro de conexão com o banco
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

        Status status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            status = Status.valueOf(statusParam);
        }

        Boolean permissaoGestor = null;
        if (gestorParam != null && !gestorParam.isBlank()) {
            permissaoGestor = Boolean.parseBoolean(gestorParam);
        }

        Date dataInicial = null;
        if (dataInicialParam != null && !dataInicialParam.isBlank()) {
            dataInicial = Date.valueOf(dataInicialParam);
        }

        Date dataFinal = null;
        if (dataFinalParam != null && !dataFinalParam.isBlank()) {
            dataFinal = Date.valueOf(dataFinalParam);
        }

        List<Colaborador> colaboradores = DAO.buscaFiltrada(busca, status, area, cargo, permissaoGestor,
                dataInicial, dataFinal, ordem, direcao);

        req.setAttribute("colaboradorList", colaboradores);
        req.setAttribute("areaList", DAO.buscarAreas());
        req.setAttribute("cargoList", DAO.buscarCargos());
        req.setAttribute("empresaOpcoes", new EmpresaDAO().buscar());
        req.getRequestDispatcher(PAGINA_COLABORADORES).forward(req, resp);
    }

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

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-colaborador"), "Id do colaborador");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser excluído");
    }

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

    private void excluirPorEmail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = Validador.email(req.getParameter("email-exclusao"));
        int resultado = DAO.excluirPorEmail(email);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "O colaborador não pode ser excluído pelo e-mail.");
    }

    // UTILITARIOS
    private String normalizarArea(String area) {
        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    private String normalizarCargo(String cargo) {
        String limpa = cargo.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCargoExistente(limpa);
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
