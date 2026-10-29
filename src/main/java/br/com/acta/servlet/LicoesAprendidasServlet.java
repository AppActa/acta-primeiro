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

@WebServlet(name = "LicoesAprendidasServlet", value = "/licoes-aprendidas-servlet")
public class LicoesAprendidasServlet extends HttpServlet {
    private static final String PAGINA_LICOES_APRENDIDAS = "/licoes-aprendidas.jsp";
    private static final String PAGINA_ERRO = "/erro.jsp";
    private final LicoesAprendidasDAO DAO = new LicoesAprendidasDAO();

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

    // CRUD
    // Retorno do DAO: 1 = certo, 0 = erro de negócio, -1 = erro de conexão com o banco
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

    private void buscaFiltrada(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = req.getParameter("busca");
        String faseParam = req.getParameter("fase");
        String severidadeParam = req.getParameter("severidade");
        String area = req.getParameter("area");
        String categoria = req.getParameter("categoria");
        String ordem = req.getParameter("ordem");
        String direcao = req.getParameter("direcao");

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

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Validador.id(req.getParameter("id-licoesAprendidas"), "Id da lição");
        int resultado = DAO.excluir(id);

        if (resultado == 1) enviarPaginaCerta(req, resp);
        else enviarErro(req, resp, "A lição aprendida não pode ser excluída");
    }

    // UTILITARIOS
    private String normalizarArea(String area) {
        if (area == null || area.isBlank()) return null;

        String limpa = area.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarAreaExistente(limpa);
        return existente != null ? existente : limpa;
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return null;

        String limpa = categoria.trim().replaceAll("\\s+", " ");
        String existente = DAO.buscarCategoriaExistente(limpa);
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
