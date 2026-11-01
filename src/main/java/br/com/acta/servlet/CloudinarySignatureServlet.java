package br.com.acta.servlet;

import br.com.acta.model.AdministradorGeral;
import br.com.acta.utils.CsrfToken;
import br.com.acta.utils.Configuracao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@WebServlet(name = "CloudinarySignatureServlet", value = "/cloudinary-signature")
public class CloudinarySignatureServlet extends HttpServlet {
    private static final String FOLDER = "acta/admin-profiles";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object usuario = req.getSession(false) == null
                ? null
                : req.getSession(false).getAttribute("usuarioLogado");
        if (!(usuario instanceof AdministradorGeral)) {
            responderErro(resp, HttpServletResponse.SC_UNAUTHORIZED, "Entre novamente para enviar uma foto.");
            return;
        }
        if (!CsrfToken.isValid(req)) {
            responderErro(resp, HttpServletResponse.SC_FORBIDDEN, "Token de segurança inválido ou expirado.");
            return;
        }

        try {
            String cloudName = Configuracao.obter("CLOUDINARY_CLOUD_NAME", "");
            String apiKey = Configuracao.obter("CLOUDINARY_API_KEY", "");
            String apiSecret = Configuracao.obter("CLOUDINARY_API_SECRET", "");
            if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
                responderErro(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Configure CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY e CLOUDINARY_API_SECRET no .env.");
                return;
            }

            long timestamp = Instant.now().getEpochSecond();
            Map<String, String> parametros = new TreeMap<>();
            parametros.put("folder", FOLDER);
            parametros.put("timestamp", String.valueOf(timestamp));
            String assinatura = assinar(parametros, apiSecret);

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            resp.getWriter().write("{\"cloudName\":\"" + json(cloudName)
                    + "\",\"apiKey\":\"" + json(apiKey)
                    + "\",\"timestamp\":" + timestamp
                    + ",\"folder\":\"" + FOLDER
                    + "\",\"signature\":\"" + assinatura + "\"}");
        } catch (Exception e) {
            getServletContext().log("Falha ao gerar assinatura para upload de perfil no Cloudinary.", e);
            responderErro(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível preparar o envio da imagem.");
        }
    }

    private String assinar(Map<String, String> parametros, String apiSecret) throws NoSuchAlgorithmException {
        StringBuilder base = new StringBuilder();
        for (Map.Entry<String, String> parametro : parametros.entrySet()) {
            if (!base.isEmpty()) base.append('&');
            base.append(parametro.getKey()).append('=').append(parametro.getValue());
        }
        base.append(apiSecret);
        byte[] hash = MessageDigest.getInstance("SHA-1")
                .digest(base.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexadecimal = new StringBuilder(hash.length * 2);
        for (byte valor : hash) hexadecimal.append(String.format("%02x", valor));
        return hexadecimal.toString();
    }

    private String json(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void responderErro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + json(mensagem) + "\"}");
    }
}
