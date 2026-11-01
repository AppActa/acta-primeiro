package br.com.acta.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Gera e valida token sincronizado com a sessão para requisições que alteram estado. */
public final class CsrfToken {
    public static final String SESSION_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Impede instâncias desta classe utilitária. */
    private CsrfToken() { }

    /** Reutiliza o token da sessão ou cria um valor aleatório de 32 bytes. */
    public static String getOrCreate(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    /** Confere o token recebido com o da sessão, sem criar outra sessão. */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String expected = (String) session.getAttribute(SESSION_KEY);
        // Prioriza o cabeçalho; formulários também podem enviar o parâmetro.
        String actual = request.getHeader("X-CSRF-Token");
        if (actual == null || actual.isBlank()) actual = request.getParameter("csrfToken");
        // Compara os bytes com a rotina de comparação de MessageDigest.
        return expected != null && actual != null
                && MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                         actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
