package br.com.acta.servlet;

import static org.junit.jupiter.api.Assertions.*;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.model.Colaborador;
import br.com.acta.utils.CsrfToken;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.security.NoSuchAlgorithmException;

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

/** Exercita o filtro por HTTP em Tomcat isolado, com destinos que dispensam o banco. */
class LoginFilterTest {

    @TempDir static Path temporario;
    static Tomcat tomcat;
    static String base;

    // Mantém redirecionamentos visíveis para conferir o status e o destino.
    static final HttpClient CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
    static final List<String> ROTAS = new ArrayList<>();
    static String cookieAdministrador;
    static String cookieColaborador;
    static String cookieInvalido;
    static String cookieExpirado;
    static String cookieCsrfCorreto;
    static String cookieCsrfIncorreto;

    /** Sobe o Tomcat em porta livre e prepara rotas e sessões para os cenários. */
    @BeforeAll
    static void iniciar() throws Exception {
        WebFilter configuracao = LoginFilter.class.getAnnotation(WebFilter.class);
        ROTAS.addAll(Arrays.stream(configuracao.urlPatterns()).filter(url -> !url.contains("*")).toList());
        try (Stream<Path> arquivos = Files.list(Path.of("src/main/webapp/crud"))) {
            arquivos.filter(Files::isRegularFile).forEach(arquivo ->
                    ROTAS.add("/crud/" + URLEncoder.encode(
                            arquivo.getFileName().toString(), StandardCharsets.UTF_8)));
        }
        ROTAS.add("/crud/");
        ROTAS.add("/crud/arquivo-futuro.jsp");

        tomcat = new Tomcat();
        tomcat.setBaseDir(temporario.toString());
        // A porta zero deixa o sistema escolher uma porta disponível.
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context contexto = tomcat.addContext("/acta", temporario.toString());
        FilterDef filtro = new FilterDef();
        filtro.setFilterName(configuracao.filterName());
        filtro.setFilter(new LoginFilter());
        contexto.addFilterDef(filtro);
        FilterMap mapa = new FilterMap();
        mapa.setFilterName(configuracao.filterName());
        for (String url : configuracao.urlPatterns()) {
            mapa.addURLPattern(url);
        }
        for (DispatcherType tipo : configuracao.dispatcherTypes()) {
            mapa.setDispatcher(tipo.name());
        }
        contexto.addFilterMap(mapa);

        // Substitui somente os destinos por sentinelas: chegar aqui significa acesso liberado.
        Tomcat.addServlet(contexto,
                "destino",
                new HttpServlet() {
                    @Override
                    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        resp.getWriter().write("DESTINO_EXECUTADO");
                    }
                });
        contexto.addServletMappingDecoded("/", "destino");
        // Cria identidades de teste sem autenticação ou consulta ao banco.
        Tomcat.addServlet(contexto,
                "sessao-teste",
                new HttpServlet() {
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
                        var sessao = req.getSession(true);
                        sessao.setAttribute("usuarioLogado", usuario);
                        sessao.setAttribute(CsrfToken.SESSION_KEY, "token-teste");
                    }
                });
        contexto.addServletMappingDecoded("/sessao-teste", "sessao-teste");
        Tomcat.addServlet(contexto, "logout", new LogoutServlet());
        contexto.addServletMappingDecoded("/logout-servlet", "logout");
        Tomcat.addServlet(contexto, "cloudinary-signature", new CloudinarySignatureServlet());
        contexto.addServletMappingDecoded("/cloudinary-signature", "cloudinary-signature");
        // Simula despachos internos para os caminhos protegidos.
        Tomcat.addServlet(contexto,
                "despacho",
                new HttpServlet() {
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
        cookieCsrfCorreto = criarSessao("administrador");
        cookieCsrfIncorreto = criarSessao("administrador");
        cookieCsrfIncorreto += "; csrfToken=token-injetado";
        assertEquals(302, enviar("POST", "/logout-servlet", cookieExpirado).statusCode());
    }

    /** Para o servidor e libera os recursos usados pelos testes. */
    @AfterAll
    static void finalizar() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    /** Combina rotas, métodos HTTP e sessões sem um administrador autorizado. */
    @TestFactory
    Stream<DynamicTest> bloquearAtaquesSemAdministrador() {
        return ROTAS.stream().filter(url -> !url.equals("/cloudinary-signature"))
                .flatMap(url -> Stream.of("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .map(metodo -> DynamicTest.dynamicTest(metodo + " " + url, () -> {
                    String ataque = url
                            + "?acao=excluir&id=1&usuarioLogado=administrador_geral&perfil=administrador_geral";
                    for (String cookie : Arrays.asList(null, cookieExpirado, cookieColaborador, cookieInvalido)) {
                        HttpResponse<String> resposta = enviar(metodo, ataque, cookie);
                        int esperado = cookie == null || cookie.equals(cookieExpirado)
                                ? (metodo.equals("GET") || metodo.equals("HEAD") ? 302 : 401) : 403;
                        assertEquals(esperado, resposta.statusCode(), metodo + " " + url);
                        assertFalse(resposta.body().contains("DESTINO_EXECUTADO"));
                        assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
                        if (esperado == 302) {
                            assertTrue(resposta.headers().firstValue("Location").orElseThrow()
                                    .endsWith("/acta/login.jsp"));
                        }
                    }
                })));
    }

    /** Verifica o acesso esperado para a sessão de administrador. */
    @TestFactory
    Stream<DynamicTest> permitirTodasAsOperacoesDoAdministrador() {
        return ROTAS.stream().filter(url -> !url.equals("/logout-servlet") && !url.equals("/cloudinary-signature"))
                .flatMap(url -> Stream.of("GET", "POST", "PUT", "DELETE")
                .map(metodo -> DynamicTest.dynamicTest("administrador " + metodo + " " + url, () -> {
                    HttpResponse<String> resposta = enviar(metodo, url, cookieAdministrador);
                    assertEquals(200, resposta.statusCode());
                    assertEquals("DESTINO_EXECUTADO", resposta.body());
                    assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
                })));
    }

    /** Exercita variações de URL com um cookie de sessão falso. */
    @Test
    void bloquearVariacoesDeUrlESessaoFalsa() throws Exception {
        for (String url :
                List.of("/crud/../crud/empresa.jsp", "/%63rud/empresa.jsp", "/crud;qualquer=1/empresa.jsp",
                        "/empresa-servlet;qualquer=1", "//crud/empresa.jsp",
                        "/crud/empresa.jsp;jsessionid=falso")) {
            HttpResponse<String> resposta = enviar("GET", url, "JSESSIONID=falso");
            assertTrue(List.of(302, 400, 404).contains(resposta.statusCode()), url + ": " + resposta.statusCode());
            assertFalse(resposta.body().contains("DESTINO_EXECUTADO"), url);
        }
    }

    /** Verifica a recusa de administradores com ID ausente, zero ou negativo. */
    @Test
    void bloquearObjetoAdministradorSemIdentidadePersistida() throws Exception {
        for (String tipo : List.of("sem-id", "id-zero", "id-negativo")) {
            assertEquals(403, enviar("POST", "/empresa-servlet", criarSessao(tipo)).statusCode());
        }
    }

    /** Compara encaminhamentos e inclusões com e sem sessão autenticada. */
    @Test
    void protegerEncaminhamentosEInclusoes() throws Exception {
        for (String tipo : List.of("forward", "include")) {
            HttpResponse<String> resposta = enviar("GET", "/despacho?tipo=" + tipo, null);
            assertFalse(resposta.body().contains("DESTINO_EXECUTADO"));
            assertEquals("DESTINO_EXECUTADO", enviar("GET", "/despacho?tipo=" + tipo, cookieAdministrador).body());
        }
    }

    /** Exige POST e token CSRF válido para encerrar uma sessão. */
    @Test
    void logoutExigePostComTokenCsrf() throws Exception {
        String cookie = criarSessao("administrador");
        assertEquals(405, enviar("GET", "/logout-servlet", cookie).statusCode());
        assertEquals(403, enviarPost("/logout-servlet", cookie, "acao=sair").statusCode());
        assertEquals(403, enviarPost("/logout-servlet", cookie, "csrfToken=token-invalido").statusCode());
        assertEquals(302, enviarPost("/logout-servlet", cookie, "csrfToken=token-teste").statusCode());
        assertEquals(302, enviar("GET", "/empresa-servlet", cookie).statusCode());
    }

    /** Verifica se páginas e arquivos públicos continuam acessíveis. */
    @Test
    void manterLandingPageELoginPublicos() throws Exception {
        for (String url :
                List.of("/", "/index.jsp", "/index.html", "/login.jsp", "/login.html", "/login-servlet",
                        "/assets/css/style.css")) {
            assertEquals(200, enviar("GET", url, null).statusCode(), url);
        }
    }

    /** Confere se as rotas dos servlets constam na cobertura do filtro. */
    @Test
    void cobrirTodosOsServletsQueAcessamOCrud() {
        for (Class<?> servlet :
                List.of(EmpresaServlet.class, EnderecoServlet.class, ColaboradorServlet.class,
                        CicloServlet.class, ProblemaServlet.class, MetaServlet.class, PlanoAcaoServlet.class,
                        TarefaServlet.class, LicoesAprendidasServlet.class, AdministradorGeralServlet.class,
                        CloudinarySignatureServlet.class)) {
            for (String url : servlet.getAnnotation(WebServlet.class).value()) {
                assertTrue(ROTAS.contains(url), url);
            }
        }
    }

    /** Exercita Cloudinary no estado sem credenciais: autorização, CSRF e resposta configurada. */
    @Test
    void protegerEndpointCloudinaryEAssinarComConfiguracaoDeTeste() throws Exception {
        assertEquals(401, enviarPost("/cloudinary-signature", null, "").statusCode());
        assertEquals(403, enviarPost("/cloudinary-signature", cookieCsrfIncorreto, "").statusCode());
        assertEquals(405, enviar("GET", "/cloudinary-signature", cookieCsrfCorreto).statusCode());
        HttpResponse<String> resposta = enviarPost("/cloudinary-signature", cookieCsrfCorreto,
                "csrfToken=token-teste");
        assertTrue(List.of(200, 503).contains(resposta.statusCode()), resposta.body());
        assertTrue(resposta.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        if (resposta.statusCode() == 503) assertTrue(resposta.body().contains("Configure CLOUDINARY_"));
        else assertTrue(resposta.body().contains("\"signature\":"));
        assertFalse(resposta.body().contains("apiSecret"));
    }

    /** Confere assinatura SHA-1 segundo a ordenação alfabética dos parâmetros. */
    @Test
    void gerarAssinaturaCloudinaryComParametrosOrdenados() throws Exception {
        var servlet = new CloudinarySignatureServlet();
        var metodo = CloudinarySignatureServlet.class.getDeclaredMethod("assinar", Map.class, String.class);
        metodo.setAccessible(true);
        Map<String, String> parametros = new TreeMap<>();
        parametros.put("timestamp", "1234567890");
        parametros.put("folder", "acta/admin-profiles");
        String esperado = sha1("folder=acta/admin-profiles&timestamp=1234567890segredo-falso");
        assertEquals(esperado, metodo.invoke(servlet, parametros, "segredo-falso"));
    }

    private static String sha1(String valor) throws NoSuchAlgorithmException {
        byte[] bytes = MessageDigest.getInstance("SHA-1").digest(valor.getBytes(StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(bytes);
    }

    /** Monta a identidade mínima de administrador usada nos cenários. */
    private static AdministradorGeral administrador(Long id) {
        AdministradorGeral administrador = new AdministradorGeral();
        administrador.setAdm_geral_id(id);
        return administrador;
    }

    /** Obtém o cookie criado pela rota de sessão exclusiva do teste. */
    private static String criarSessao(String tipo) throws Exception {
        return enviar("GET", "/sessao-teste?tipo=" + tipo, null).headers().firstValue("Set-Cookie").orElseThrow()
                .split(";", 2)[0];
    }

    /** Envia a requisição HTTP com o método, o corpo e o cookie do cenário. */
    private static HttpResponse<String> enviar(String metodo, String url, String cookie) throws Exception {
        if (metodo.equals("POST")) {
            return enviarPost(url, cookie,
                    "acao=excluir&id=1&perfil=administrador_geral&csrfToken=token-teste");
        }
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + url))
                .method(metodo, HttpRequest.BodyPublishers.noBody());
        if (cookie != null) {
            requisicao.header("Cookie", cookie);
        }
        return CLIENT.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Envia um POST com corpo explícito para cenários de CSRF e logout. */
    private static HttpResponse<String> enviarPost(String url, String cookie, String corpo) throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corpo));
        if (cookie != null) {
            requisicao.header("Cookie", cookie);
        }
        return CLIENT.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }
}
