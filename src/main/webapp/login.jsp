<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Se já há sessão ativa, redireciona para home
    if (session != null && session.getAttribute("usuarioLogado") != null) {
        response.sendRedirect(request.getContextPath() + "/empresa-servlet");
        return;
    }
    String mensagemErro = (String) request.getAttribute("mensagemErro");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/assets/images/logo_compacta.svg" type="image/x-icon">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/login.css">
    <title>Login</title>
</head>
<body>
    <main class="login-pagina">
        <div class="login-sair">
            <img src="<%= request.getContextPath() %>/assets/images/icone_voltar.svg" alt="">
            <a href="<%= request.getContextPath() %>/index.html">Tela inicial</a>
        </div>

        <div class="login-cartao">
            <!-- Coluna da esquerda -->
            <aside class="login-visual">
                <img src="<%= request.getContextPath() %>/assets/images/logo_compacta_escura.svg" alt="" class="login-logo">
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

                <% if (mensagemErro != null && !mensagemErro.isBlank()) { %>
                <p class="login-erro" role="alert"><%= mensagemErro %></p>
                <% } %>

                <form class="login-form" method="post" action="<%= request.getContextPath() %>/login-servlet"><input type="hidden" name="csrfToken" value="<%= br.com.acta.utils.CsrfToken.getOrCreate(session) %>">
                    <input type="hidden" name="acao" value="login">
                    <div class="login-campo">
                        <label for="login-email">Email</label>
                        <input type="email" id="login-email" name="email" maxlength="80" autocomplete="email" required>
                    </div>

                    <div class="login-campo">
                        <label for="login-senha">Senha</label>
                        <input type="password" id="login-senha" class="senha" name="senha" maxlength="100" autocomplete="current-password" required>
                    </div>

                    <button type="submit" class="login-botao">Concluído</button>
                </form>
            </section>
        </div>
    </main>
</body>
</html>