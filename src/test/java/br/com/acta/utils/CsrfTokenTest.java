package br.com.acta.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Cobre geração, persistência e validação de tokens sem abrir servidor ou banco. */
class CsrfTokenTest {

    @Test
    void criaTokenAleatorioDe32BytesEReutilizaNaSessao() {
        Map<String, Object> atributos = new HashMap<>();
        HttpSession sessao = sessao(atributos);

        String primeiro = CsrfToken.getOrCreate(sessao);
        String segundo = CsrfToken.getOrCreate(sessao);

        assertEquals(43, primeiro.length()); // Base64 URL sem padding de 32 bytes.
        assertNotEquals(primeiro, CsrfToken.getOrCreate(sessao(new HashMap<>())));
        assertEquals(primeiro, segundo);
        assertEquals(primeiro, atributos.get(CsrfToken.SESSION_KEY));
    }

    @Test
    void aceitaCabecalhoOuParametroERejeitaTokenAusenteOuDiferente() {
        Map<String, Object> atributos = new HashMap<>();
        atributos.put(CsrfToken.SESSION_KEY, "segredo-de-teste");

        assertTrue(CsrfToken.isValid(requisicao(atributos, "segredo-de-teste", "outro")));
        assertTrue(CsrfToken.isValid(requisicao(atributos, null, "segredo-de-teste")));
        assertFalse(CsrfToken.isValid(requisicao(atributos, "incorreto", "segredo-de-teste")));
        assertFalse(CsrfToken.isValid(requisicao(atributos, null, null)));
        assertFalse(CsrfToken.isValid(requisicao(null, "segredo-de-teste", null)));
    }

    private static HttpSession sessao(Map<String, Object> atributos) {
        return (HttpSession) Proxy.newProxyInstance(CsrfTokenTest.class.getClassLoader(),
                new Class<?>[]{HttpSession.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> atributos.get(args[0]);
                    case "setAttribute" -> { atributos.put((String) args[0], args[1]); yield null; }
                    default -> null;
                });
    }

    private static HttpServletRequest requisicao(Map<String, Object> atributos, String header, String parametro) {
        HttpSession sessao = atributos == null ? null : sessao(atributos);
        return (HttpServletRequest) Proxy.newProxyInstance(CsrfTokenTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> sessao;
                    case "getHeader" -> "X-CSRF-Token".equals(args[0]) ? header : null;
                    case "getParameter" -> "csrfToken".equals(args[0]) ? parametro : null;
                    default -> null;
                });
    }
}
