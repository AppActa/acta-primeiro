package br.com.acta.servlet;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Ataques HTTP reais em Tomcat isolado; nenhum DAO ou banco e executado. */
class LoginFilterTest {
    @TempDir
    static Path temporario;
    static Tomcat tomcat;
    static String base;
    static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER).build();
    static final List<String> ROTAS = new ArrayList<>();
    static String cookieAdministrador;
    static String cookieColaborador;
    static String cookieInvalido;
    static String cookieExpirado;

    @BeforeAll
    static void iniciar() throws Exception {
        WebFilter configuracao = LoginFilter.class.getAnnotation(WebFilter.class);
        ROTAS.addAll(Arrays.stream(configuracao.urlPatterns())
                .filter(url -> !url.contains("*")).toList());
        try (Stream<Path> arquivos = Files.list(Path.of("src/main/webapp/crud"))) {
            arquivos.filter(Files::isRegularFile).forEach(arquivo -> ROTAS.add(
                    "/crud/" + URLEncoder.encode(arquivo.getFileName().toString(), StandardCharsets.UTF_8)));
        }
        ROTAS.add("/crud/");
        ROTAS.add("/crud/arquivo-futuro.jsp");

        tomcat = new Tomcat();
        tomcat.setBaseDir(temporario.toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context contexto = tomcat.addContext("/acta", temporario.toString());
        FilterDef filtro = new FilterDef();
        filtro.setFilterName(configuracao.filterName());
        filtro.setFilter(new LoginFilter());
        contexto.addFilterDef(filtro);
        FilterMap mapa = new FilterMap();
        mapa.setFilterName(configuracao.filterName());
        for (String url : configuracao.urlPatterns()) mapa.addURLPattern(url);
        for (DispatcherType tipo : configuracao.dispatcherTypes()) mapa.setDispatcher(tipo.name());
        contexto.addFilterMap(mapa);

        // Substitui somente os destinos por sentinelas: chegar aqui significa acesso liberado.
        Tomcat.addServlet(contexto, "destino", new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.getWriter().write("DESTINO_EXECUTADO");
            }
        });
        contexto.addServletMappingDecoded("/", "destino");
        Tomcat.addServlet(contexto, "sessao-teste", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                Object usuario = switch (req.getParameter("tipo")) {
                    case "administrador" -> administrador(1L);
                    case "sem-id" -> administrador(null);
                    case "id-zero" -> administrador(0L);
                    case "id-negativo" -> administrador(-1L);
                    case "colaborador" -> new Colaborador();
                    default -> "administrador_geral";
                };
                req.getSession(true).setAttribute("usuarioLogado", usuario);
            }
        });
        contexto.addServletMappingDecoded("/sessao-teste", "sessao-teste");
        Tomcat.addServlet(contexto, "logout", new LogoutServlet());
        contexto.addServletMappingDecoded("/logout-servlet", "logout");
        Tomcat.addServlet(contexto, "despacho", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws IOException, ServletException {
                if ("include".equals(req.getParameter("tipo"))) {
                    req.getRequestDispatcher("/crud/empresa.jsp").include(req, resp);
                } else {
                    req.getRequestDispatcher("/empresa-servlet").forward(req, resp);
                }
            }
        });
        contexto.addServletMappingDecoded("/despacho", "despacho");
        tomcat.start();
        base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/acta";
        cookieAdministrador = criarSessao("administrador");
        cookieColaborador = criarSessao("colaborador");
        cookieInvalido = criarSessao("falso");
        cookieExpirado = criarSessao("administrador");
        assertEquals(302, enviar("GET", "/logout-servlet", cookieExpirado).statusCode());
    }

    @AfterAll
    static void finalizar() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @TestFactory
    Stream<DynamicTest> bloquearAtaquesSemAdministrador() {
        return ROTAS.stream().flatMap(url -> Stream.of("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .map(metodo -> DynamicTest.dynamicTest(metodo + " " + url, () -> {
                    String ataque = url + "?acao=excluir&id=1&usuarioLogado=administrador_geral&perfil=administrador_geral";
                    for (String cookie : Arrays.asList(null, cookieExpirado, cookieColaborador, cookieInvalido)) {
                        HttpResponse<String> resposta = enviar(metodo, ataque, cookie);
                        int esperado = cookie == null || cookie.equals(cookieExpirado)
                                ? (metodo.equals("GET") || metodo.equals("HEAD") ? 302 : 401) : 403;
                        assertEquals(esperado, resposta.statusCode(), metodo + " " + url);
                        assertFalse(resposta.body().contains("DESTINO_EXECUTADO"));
                        assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
                        if (esperado == 302) {
                            assertTrue(resposta.headers().firstValue("Location").orElseThrow().endsWith("/acta/login.jsp"));
                        }
                    }
                })));
    }

    @TestFactory
    Stream<DynamicTest> permitirTodasAsOperacoesDoAdministrador() {
        return ROTAS.stream().flatMap(url -> Stream.of("GET", "POST", "PUT", "DELETE")
                .map(metodo -> DynamicTest.dynamicTest("administrador " + metodo + " " + url, () -> {
                    HttpResponse<String> resposta = enviar(metodo, url, cookieAdministrador);
                    assertEquals(200, resposta.statusCode());
                    assertEquals("DESTINO_EXECUTADO", resposta.body());
                    assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
                })));
    }

    @Test
    void bloquearVariacoesDeUrlESessaoFalsa() throws Exception {
        for (String url : List.of("/crud/../crud/empresa.jsp", "/%63rud/empresa.jsp",
                "/crud;qualquer=1/empresa.jsp", "/empresa-servlet;qualquer=1",
                "//crud/empresa.jsp", "/crud/empresa.jsp;jsessionid=falso")) {
            HttpResponse<String> resposta = enviar("GET", url, "JSESSIONID=falso");
            assertTrue(List.of(302, 400, 404).contains(resposta.statusCode()), url + ": " + resposta.statusCode());
            assertFalse(resposta.body().contains("DESTINO_EXECUTADO"), url);
        }
    }

    @Test
    void bloquearObjetoAdministradorSemIdentidadePersistida() throws Exception {
        for (String tipo : List.of("sem-id", "id-zero", "id-negativo")) {
            assertEquals(403, enviar("POST", "/empresa-servlet", criarSessao(tipo)).statusCode());
        }
    }

    @Test
    void protegerEncaminhamentosEInclusoes() throws Exception {
        for (String tipo : List.of("forward", "include")) {
            HttpResponse<String> resposta = enviar("GET", "/despacho?tipo=" + tipo, null);
            assertFalse(resposta.body().contains("DESTINO_EXECUTADO"));
            assertEquals("DESTINO_EXECUTADO", enviar("GET", "/despacho?tipo=" + tipo, cookieAdministrador).body());
        }
    }

    @Test
    void manterLandingPageELoginPublicos() throws Exception {
        for (String url : List.of("/", "/index.jsp", "/index.html", "/login.jsp", "/login.html",
                "/login-servlet", "/assets/css/style.css")) {
            assertEquals(200, enviar("GET", url, null).statusCode(), url);
        }
    }

    @Test
    void cobrirTodosOsServletsQueAcessamOCrud() {
        for (Class<?> servlet : List.of(EmpresaServlet.class, EnderecoServlet.class, ColaboradorServlet.class,
                CicloServlet.class, ProblemaServlet.class, MetaServlet.class, PlanoAcaoServlet.class,
                TarefaServlet.class, LicoesAprendidasServlet.class, AdministradorGeralServlet.class,
                CloudinarySignatureServlet.class)) {
            for (String url : servlet.getAnnotation(WebServlet.class).value()) assertTrue(ROTAS.contains(url), url);
        }
    }

    private static AdministradorGeral administrador(Long id) {
        AdministradorGeral administrador = new AdministradorGeral();
        administrador.setAdm_geral_id(id);
        return administrador;
    }

    private static String criarSessao(String tipo) throws Exception {
        return enviar("GET", "/sessao-teste?tipo=" + tipo, null).headers()
                .firstValue("Set-Cookie").orElseThrow().split(";", 2)[0];
    }

    private static HttpResponse<String> enviar(String metodo, String url, String cookie) throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + url))
                .method(metodo, metodo.equals("POST")
                        ? HttpRequest.BodyPublishers.ofString("acao=excluir&id=1&perfil=administrador_geral")
                        : HttpRequest.BodyPublishers.noBody());
        if (metodo.equals("POST")) requisicao.header("Content-Type", "application/x-www-form-urlencoded");
        if (cookie != null) requisicao.header("Cookie", cookie);
        return CLIENT.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }
}
