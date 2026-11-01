package br.com.acta.servlet;

import br.com.acta.dao.CicloDAO;

import br.com.acta.dao.LicoesAprendidasDAO;
import br.com.acta.model.LicoesAprendidas;
import br.com.acta.enums.EtapaCiclo;
import br.com.acta.enums.Intensidade;
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

/** Recebe as requisições de LicoesAprendidas e prepara os dados para as páginas JSP. */
@WebServlet(name = "LicoesAprendidasServlet", value = "/licoes-aprendidas-servlet")
public class LicoesAprendidasServlet extends HttpServlet {
    private static final String PAGINA_LICOES_APRENDIDAS = "/crud/licoes-aprendidas.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final LicoesAprendidasDAO DAO = new LicoesAprendidasDAO();

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
            enviarErro(req, resp, "Não foi possível encontrar as lições aprendidas.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        LicoesAprendidas licoesAprendidas = new LicoesAprendidas();

        licoesAprendidas.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        licoesAprendidas.setArea(normalizarArea(Validador.textoOpcional(req.getParameter("area"), "Área", 30)));
        licoesAprendidas.setAprendizado(Validador.textoLongo(req.getParameter("aprendizado"), "Aprendizado"));
        licoesAprendidas.setCategoria(normalizarCategoria(Validador.textoOpcional(req.getParameter("categoria"), "Categoria", 30)));
        licoesAprendidas.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        licoesAprendidas.setFase_origem(Validador.opcao(EtapaCiclo.class, req.getParameter("fase_origem"), "Fase de origem"));
        licoesAprendidas.setSeveridade(Validador.opcao(Intensidade.class, req.getParameter("severidade"), "Severidade"));
        licoesAprendidas.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));

        int resultado = DAO.inserir(licoesAprendidas);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser cadastrada");
    }

    /** Busca pelo ID informado ou delega à listagem com filtros. */
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null) {
            buscaFiltrada(req, resp);
            return;
        }

        LicoesAprendidas licoesAprendidas = DAO.buscar(Validador.id(id, "Id"));

        if (licoesAprendidas == null) {
            throw new RegistroNaoEncontradoException("Registro de lição aprendida não encontrado.");
        }

        req.setAttribute("licoesAprendidasList", List.of(licoesAprendidas));
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

    /** Lê os filtros opcionais e prepara os dados exibidos pela página. */
    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String faseParam = req.getParameter("fase");
        String severidadeParam = req.getParameter("severidade");
        String area = req.getParameter("area");
        String categoria = req.getParameter("categoria");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

        // Filtro ausente permanece null e não restringe a consulta.
        EtapaCiclo fase = null;
        if (faseParam != null && !faseParam.isBlank()) fase = EtapaCiclo.valueOf(faseParam);

        Intensidade severidade = null;
        if (severidadeParam != null && !severidadeParam.isBlank()) severidade = Intensidade.valueOf(severidadeParam);

        List<LicoesAprendidas> licoesAprendidas = DAO.buscaFiltrada(busca, fase, severidade, area, categoria, ordem, direcao);

        req.setAttribute("licoesAprendidasList", licoesAprendidas);
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

    /** Valida os campos e atualiza o registro identificado no formulário. */
    private void atualizar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-licoesAprendidas"), "Id da lição");
        LicoesAprendidas licoesAprendidas = new LicoesAprendidas();

        licoesAprendidas.setId_licao(id);
        licoesAprendidas.setTitulo(Validador.texto(req.getParameter("titulo"), "Título", 100));
        licoesAprendidas.setArea(normalizarArea(Validador.textoOpcional(req.getParameter("area"), "Área", 30)));
        licoesAprendidas.setAprendizado(Validador.textoLongo(req.getParameter("aprendizado"), "Aprendizado"));
        licoesAprendidas.setCategoria(normalizarCategoria(Validador.textoOpcional(req.getParameter("categoria"), "Categoria", 30)));
        licoesAprendidas.setDescricao(Validador.textoLongoOpcional(req.getParameter("descricao"), "Descrição"));
        licoesAprendidas.setFase_origem(Validador.opcao(EtapaCiclo.class, req.getParameter("fase_origem"), "Fase de origem"));
        licoesAprendidas.setSeveridade(Validador.opcao(Intensidade.class, req.getParameter("severidade"), "Severidade"));
        licoesAprendidas.setId_ciclo(Validador.id(req.getParameter("id_ciclo"), "Ciclo"));

        int resultado = DAO.atualizar(licoesAprendidas);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser atualizada");
    }

    /** Valida o ID recebido e solicita a exclusão ao DAO. */
    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-licoesAprendidas"), "Id da lição");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser excluída");
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarArea(String area) {
        if (area == null || area.isBlank()) return null;

        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    /** Remove espaços excedentes e reutiliza a escrita já cadastrada no banco. */
    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return null;

        String limpa = categoria.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCategoriaExistente(limpa);
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
            List<LicoesAprendidas> licoesAprendidas = DAO.buscar();

            req.setAttribute("licoesAprendidasList", licoesAprendidas);
            req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
            req.setAttribute("areaList", DAO.buscarArea());
            req.setAttribute("categoriaList", DAO.buscarCategoria());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            req.setAttribute("licoesAprendidasList", List.of());
            req.setAttribute("cicloOpcoes", List.of());
            req.setAttribute("areaList", List.of());
            req.setAttribute("categoriaList", List.of());
        }
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

    /** Recarrega a listagem e as opções da página com uma mensagem de sucesso. */
    private void enviarPaginaCerta(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mensagemSucesso", "Operação realizada com sucesso.");
        List<LicoesAprendidas> licoesAprendidas = DAO.buscar();

        req.setAttribute("licoesAprendidasList", licoesAprendidas);
        req.setAttribute("areaList", DAO.buscarArea());
        req.setAttribute("categoriaList", DAO.buscarCategoria());
        req.setAttribute("cicloOpcoes", new CicloDAO().buscar());
        req.getRequestDispatcher(PAGINA_LICOES_APRENDIDAS).forward(req, resp);
    }

}
