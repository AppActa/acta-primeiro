<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.Map,java.util.List,br.com.acta.model.Empresa,br.com.acta.utils.CsrfToken" %>
<%! private String h(Object valor) { return valor == null ? "" : valor.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); } %>
<% response.setHeader("Cache-Control", "no-store");
if (request.getAttribute("cadastroDados") == null) { response.sendRedirect(request.getContextPath() + "/cadastro-servlet"); return; }
Map<String,String> dados = (Map<String,String>) request.getAttribute("cadastroDados"); %>
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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/autenticacao/cadastro.css">
    <title>Cadastro de colaborador — Etapa 1 | ACTA</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/componentes/mensagens.css">
</head>
<body>
    <main class="login-pagina">
        <div class="login-sair">
            <img src="<%= request.getContextPath() %>/assets/images/icone_voltar.svg" alt="">
            <a href="<%= request.getContextPath() %>/login">Voltar</a>
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
                <h1>Cadastro de colaborador</h1><p class="cadastro-etapa" role="status">Etapa <span data-etapa-atual>1</span> de 2</p>
                <p class="login-descricao">Informe seus dados pessoais para começar seu cadastro no ACTA.</p>

                <% if (request.getAttribute("mensagemErro") != null && request.getAttribute("campoErro") == null) { %><p class="login-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
<% if (request.getAttribute("mensagemSucesso") != null) { %><p class="operacao-sucesso" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p><% } %>

                <form class="login-form" method="post" action="<%= request.getContextPath() %>/cadastro-servlet" data-cadastro novalidate>
 <input type="hidden" name="csrfToken" value="<%= h(CsrfToken.getOrCreate(session)) %>"><input type="hidden" name="acao" value="inserir"><fieldset data-etapa="1"><legend>Dados pessoais</legend><div class="login-campo">
                        <label for="cadastro-nome">Nome</label>
                        <input type="text" id="cadastro-nome" name="nome" maxlength="50" autocomplete="given-name" required value="<%= h(dados.get("nome")) %>"><%= erroCampo(request, "nome") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-sobrenome">Sobrenome</label>
                        <input type="text" id="cadastro-sobrenome" name="sobrenome" maxlength="50" autocomplete="family-name" required value="<%= h(dados.get("sobrenome")) %>"><%= erroCampo(request, "sobrenome") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-email">Email</label>
                        <input type="email" id="cadastro-email" name="email" maxlength="80" autocomplete="email" required value="<%= h(dados.get("email")) %>"><%= erroCampo(request, "email") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-senha">Senha</label>
                        <input type="password" id="cadastro-senha" name="senha" maxlength="100" autocomplete="new-password" required><%= erroCampo(request, "senha") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-telefone">Telefone</label>
                        <input type="tel" id="cadastro-telefone" name="telefone" maxlength="15" inputmode="numeric" pattern="\([0-9]{2}\) [0-9]{4,5}-[0-9]{4}" placeholder="(11) 99999-9999" autocomplete="tel-national" data-mascara="telefone" required value="<%= h(dados.get("telefone")) %>"><%= erroCampo(request, "telefone") %>
                    </div><button type="button" class="login-botao" data-continuar>Continuar</button></fieldset>
 <fieldset data-etapa="2" hidden disabled><legend>Dados profissionais</legend><div class="login-campo">
                        <label for="cadastro-cpf">CPF</label>
                        <input type="text" id="cadastro-cpf" name="cpf" maxlength="14" inputmode="numeric" pattern="[0-9]{3}\.[0-9]{3}\.[0-9]{3}-[0-9]{2}" placeholder="000.000.000-00" data-mascara="cpf" required value="<%= h(dados.get("cpf")) %>"><%= erroCampo(request, "cpf") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-area">Área</label>
                        <input type="text" id="cadastro-area" name="area" maxlength="30" required value="<%= h(dados.get("area")) %>"><%= erroCampo(request, "area") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-cargo">Cargo</label>
                        <input type="text" id="cadastro-cargo" name="cargo" maxlength="100" required value="<%= h(dados.get("cargo")) %>"><%= erroCampo(request, "cargo") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-dt_contratacao">Data de contratação</label>
                        <input type="date" id="cadastro-dt_contratacao" name="dt_contratacao" required value="<%= h(dados.get("dt_contratacao")) %>"><%= erroCampo(request, "dt_contratacao") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-id_empresa">Empresa</label>
                        <select id="cadastro-id_empresa" name="id_empresa" required>
                            <option value="">Selecione</option>
<% for (Empresa empresa : (List<Empresa>) request.getAttribute("empresaOpcoes")) { %>
<option value="<%= empresa.getId_empresa() %>" <%= String.valueOf(empresa.getId_empresa()).equals(dados.get("id_empresa")) ? "selected" : "" %>><%= h(empresa.getNome()) %></option><% } %>
                        </select><%= erroCampo(request, "id_empresa") %>
                    </div>
                    <button type="submit" class="login-esqueceu" data-cadastrar-empresa>Minha empresa não está cadastrada</button><div class="cadastro-acoes"><button type="button" class="login-botao cadastro-voltar" data-voltar>Voltar</button><button type="submit" class="login-botao">Concluir cadastro</button></div></fieldset>
 </form>

                <a href="<%= request.getContextPath() %>/login" class="login-esqueceu">Já tem cadastro?</a>
            </section>
        </div>
    </main>
<noscript><p class="login-erro">Ative o JavaScript para navegar entre as etapas.</p></noscript>
<script src="<%= request.getContextPath() %>/assets/js/compartilhado/regex.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/autenticacao/cadastro.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/compartilhado/mensagens.js"></script>
</body>
</html>
