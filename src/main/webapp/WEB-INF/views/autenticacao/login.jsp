<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Se já há sessão ativa, redireciona para home
    if (session != null && session.getAttribute("usuarioLogado") != null) {
        response.sendRedirect(request.getContextPath() + "/inicio");
        return;
    }
    String mensagemErro = (String) request.getAttribute("mensagemErro");
%>
<%!
    private String h(Object valor) {
        return valor == null ? "" : valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%!
private String erroCampo(jakarta.servlet.http.HttpServletRequest req, String campo) {
    Object campos = req.getAttribute("campoErro");
    Object mensagem = req.getAttribute("mensagemErro");
    if (campos == null || mensagem == null || !java.util.Arrays.asList(campos.toString().split(",")).contains(campo)) return "";
    return "<small class=\"campo-erro\" id=\"erro-" + campo + "\" role=\"alert\">" + h(mensagem) + "</small>";
}
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/assets/images/logo_compacta.svg" type="image/x-icon">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/autenticacao/login.css">
    <title>Login</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/componentes/mensagens.css">
</head>
<body>
    <main class="login-pagina">
        <div class="login-sair">
            <img src="<%= request.getContextPath() %>/assets/images/icone_voltar.svg" alt="">
            <a href="<%= request.getContextPath() %>/inicio">Tela inicial</a>
        </div>

        <div class="login-cartao">
            <!-- Coluna da esquerda -->
            <aside class="login-visual">
                <a class="logo-link" href="<%= request.getContextPath() %>/inicio" aria-label="ACTA - Página inicial"><img src="<%= request.getContextPath() %>/assets/images/logo_compacta_escura.svg" alt="ACTA" class="login-logo"></a>
                <p class="login-slogan">
                    ACTA, seu app de PDCA
                    <strong>
                        O ciclo de melhoria <br>começa aqui!
                    </strong>
                </p>
            </aside>

            <!-- Coluna da direita -->
            <section class="login-area">
                <h1>Login</h1>
                <p class="login-descricao">
                    Junte-se ao ACTA e dê início a um novo ciclo em sua carreira por meio da metodologia PDCA
                </p>

                <% if (mensagemErro != null && !mensagemErro.isBlank() && request.getAttribute("campoErro") == null) { %>
                <p class="login-erro" role="alert"><%= h(mensagemErro) %></p>
                <% } %>

                <form class="login-form" method="post" action="<%= request.getContextPath() %>/login-servlet"><input type="hidden" name="csrfToken" value="<%= br.com.acta.utils.CsrfToken.getOrCreate(session) %>">
                    <input type="hidden" name="acao" value="login">
                    <div class="login-campo">
                        <label for="login-email">Email</label>
                        <input type="email" id="login-email" name="email" maxlength="80" autocomplete="email" required><%= erroCampo(request, "email") %>
                    </div>

                    <div class="login-campo">
                        <label for="login-senha">Senha</label>
                        <input type="password" id="login-senha" class="senha" name="senha" maxlength="100" autocomplete="current-password" required><%= erroCampo(request, "senha") %>
                    </div>

                    <button type="submit" class="login-botao">Concluído</button>
                </form>
                <a href="<%= request.getContextPath() %>/cadastro-servlet" class="login-esqueceu">Cadastrar-se</a>
            </section>
        </div>
    </main>
<script src="<%= request.getContextPath() %>/assets/js/compartilhado/mensagens.js"></script>
</body>
</html>