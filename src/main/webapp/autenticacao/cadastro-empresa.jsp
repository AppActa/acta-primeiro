<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.Map,java.util.List,br.com.acta.model.Empresa,br.com.acta.utils.CsrfToken" %>
<%! private String h(Object valor) { return valor == null ? "" : valor.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); } %>
<% response.setHeader("Cache-Control", "no-store");
if (request.getAttribute("empresaDados") == null) { response.sendRedirect(request.getContextPath() + "/cadastro-empresa-servlet"); return; }
Map<String,String> dados = (Map<String,String>) request.getAttribute("empresaDados"); %>
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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/cadastro.css">
    <title>Cadastro de empresa — Etapa 1 | ACTA</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/mensagens.css">
</head>
<body>
    <main class="login-pagina">
        <div class="login-sair">
            <img src="<%= request.getContextPath() %>/assets/images/icone_voltar.svg" alt="">
            <a href="<%= request.getContextPath() %>/cadastro-servlet">Voltar</a>
        </div>

        <div class="login-cartao">
            <!-- Coluna da esquerda -->
            <aside class="login-visual">
                <a class="logo-link" href="<%= request.getContextPath() %>/index.jsp" aria-label="ACTA - Página inicial"><img src="<%= request.getContextPath() %>/assets/images/logo_compacta_escura.svg" alt="ACTA" class="login-logo"></a>
                <p class="login-slogan">
                    ACTA, seu app de PDCA
                    <strong>
                        O ciclo de melhoria <br>começa aqui!
                    </strong>
                </p>
            </aside>

            <!-- Coluna da direita -->
            <section class="login-area">
                <h1>Cadastrar empresa</h1><p class="cadastro-etapa" role="status">Etapa <span data-etapa-atual>1</span> de 2</p>
                <p class="login-descricao">Preencha os dados da empresa. O endereço será informado na próxima etapa.</p>

                <% if (request.getAttribute("mensagemErro") != null && request.getAttribute("campoErro") == null) { %><p class="login-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
<% if (request.getAttribute("mensagemSucesso") != null) { %><p class="operacao-sucesso" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p><% } %>

                <form class="login-form" method="post" action="<%= request.getContextPath() %>/cadastro-empresa-servlet" data-cadastro novalidate>
 <input type="hidden" name="csrfToken" value="<%= h(CsrfToken.getOrCreate(session)) %>"><input type="hidden" name="acao" value="inserir"><fieldset data-etapa="1"><legend>Dados da empresa</legend><div class="login-campo">
                        <label for="cadastro-nome">Nome da empresa</label>
                        <input type="text" id="cadastro-nome" name="nome" maxlength="50" required value="<%= h(dados.get("nome")) %>"><%= erroCampo(request, "nome") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-cnpj">CNPJ</label>
                        <input type="text" id="cadastro-cnpj" name="cnpj" maxlength="18" inputmode="text" pattern="[A-Za-z0-9]{2}\.[A-Za-z0-9]{3}\.[A-Za-z0-9]{3}/[A-Za-z0-9]{4}-[0-9]{2}" placeholder="00.000.000/0000-00" data-mascara="cnpj" required value="<%= h(dados.get("cnpj")) %>"><%= erroCampo(request, "cnpj") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-setor">Setor</label>
                        <input type="text" id="cadastro-setor" name="setor" maxlength="30" required value="<%= h(dados.get("setor")) %>"><%= erroCampo(request, "setor") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-tamanho">Porte da empresa</label>
                        <select id="cadastro-tamanho" name="tamanho" required>
                            <option value="">Selecione</option>
                            <option value="PEQUENA" <%= "PEQUENA".equals(dados.get("tamanho")) ? "selected" : "" %>>Pequena</option>
                            <option value="MEDIA" <%= "MEDIA".equals(dados.get("tamanho")) ? "selected" : "" %>>Média</option>
                            <option value="GRANDE" <%= "GRANDE".equals(dados.get("tamanho")) ? "selected" : "" %>>Grande</option>
                        </select><%= erroCampo(request, "tamanho") %>
                    </div><button type="button" class="login-botao" data-continuar>Continuar</button></fieldset>
 <fieldset data-etapa="2" hidden disabled><legend>Endereço da empresa</legend><div class="login-campo">
                        <label for="cadastro-cep">CEP</label>
                        <input type="text" id="cadastro-cep" name="cep" maxlength="9" inputmode="numeric" pattern="[0-9]{5}-[0-9]{3}" placeholder="00000-000" data-mascara="cep" aria-describedby="cep-status" required value="<%= h(dados.get("cep")) %>"><small id="cep-status" data-viacep-status role="status" hidden></small><%= erroCampo(request, "cep") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-uf">Estado (UF)</label>
                        <select id="cadastro-uf" name="estado" required>
                            <option value="">Selecione</option>
                            <option value="AC" <%= "AC".equals(dados.get("estado")) ? "selected" : "" %>>AC</option>
                            <option value="AL" <%= "AL".equals(dados.get("estado")) ? "selected" : "" %>>AL</option>
                            <option value="AP" <%= "AP".equals(dados.get("estado")) ? "selected" : "" %>>AP</option>
                            <option value="AM" <%= "AM".equals(dados.get("estado")) ? "selected" : "" %>>AM</option>
                            <option value="BA" <%= "BA".equals(dados.get("estado")) ? "selected" : "" %>>BA</option>
                            <option value="CE" <%= "CE".equals(dados.get("estado")) ? "selected" : "" %>>CE</option>
                            <option value="DF" <%= "DF".equals(dados.get("estado")) ? "selected" : "" %>>DF</option>
                            <option value="ES" <%= "ES".equals(dados.get("estado")) ? "selected" : "" %>>ES</option>
                            <option value="GO" <%= "GO".equals(dados.get("estado")) ? "selected" : "" %>>GO</option>
                            <option value="MA" <%= "MA".equals(dados.get("estado")) ? "selected" : "" %>>MA</option>
                            <option value="MT" <%= "MT".equals(dados.get("estado")) ? "selected" : "" %>>MT</option>
                            <option value="MS" <%= "MS".equals(dados.get("estado")) ? "selected" : "" %>>MS</option>
                            <option value="MG" <%= "MG".equals(dados.get("estado")) ? "selected" : "" %>>MG</option>
                            <option value="PA" <%= "PA".equals(dados.get("estado")) ? "selected" : "" %>>PA</option>
                            <option value="PB" <%= "PB".equals(dados.get("estado")) ? "selected" : "" %>>PB</option>
                            <option value="PR" <%= "PR".equals(dados.get("estado")) ? "selected" : "" %>>PR</option>
                            <option value="PE" <%= "PE".equals(dados.get("estado")) ? "selected" : "" %>>PE</option>
                            <option value="PI" <%= "PI".equals(dados.get("estado")) ? "selected" : "" %>>PI</option>
                            <option value="RJ" <%= "RJ".equals(dados.get("estado")) ? "selected" : "" %>>RJ</option>
                            <option value="RN" <%= "RN".equals(dados.get("estado")) ? "selected" : "" %>>RN</option>
                            <option value="RS" <%= "RS".equals(dados.get("estado")) ? "selected" : "" %>>RS</option>
                            <option value="RO" <%= "RO".equals(dados.get("estado")) ? "selected" : "" %>>RO</option>
                            <option value="RR" <%= "RR".equals(dados.get("estado")) ? "selected" : "" %>>RR</option>
                            <option value="SC" <%= "SC".equals(dados.get("estado")) ? "selected" : "" %>>SC</option>
                            <option value="SP" <%= "SP".equals(dados.get("estado")) ? "selected" : "" %>>SP</option>
                            <option value="SE" <%= "SE".equals(dados.get("estado")) ? "selected" : "" %>>SE</option>
                            <option value="TO" <%= "TO".equals(dados.get("estado")) ? "selected" : "" %>>TO</option>
                        </select><%= erroCampo(request, "estado") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-logradouro">Logradouro</label>
                        <input type="text" id="cadastro-logradouro" name="rua" maxlength="50" required value="<%= h(dados.get("rua")) %>"><%= erroCampo(request, "rua") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-numero">Número</label>
                        <input type="text" id="cadastro-numero" name="numero" maxlength="10" required value="<%= h(dados.get("numero")) %>"><%= erroCampo(request, "numero") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-bairro">Bairro</label>
                        <input type="text" id="cadastro-bairro" name="bairro" maxlength="30" required value="<%= h(dados.get("bairro")) %>"><%= erroCampo(request, "bairro") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-cidade">Cidade</label>
                        <input type="text" id="cadastro-cidade" name="cidade" maxlength="30" required value="<%= h(dados.get("cidade")) %>"><%= erroCampo(request, "cidade") %>
                    </div>
                    <div class="login-campo">
                        <label for="cadastro-complemento">Complemento (opcional)</label>
                        <input type="text" id="cadastro-complemento" name="complemento" maxlength="100" value="<%= h(dados.get("complemento")) %>"><%= erroCampo(request, "complemento") %>
                    </div><div class="login-campo"><label for="cadastro-unidade">Unidade</label><input id="cadastro-unidade" name="unidade" maxlength="30" required value="<%= h(dados.get("unidade")) %>"><%= erroCampo(request, "unidade") %></div><div class="cadastro-acoes"><button type="button" class="login-botao cadastro-voltar" data-voltar>Voltar</button><button type="submit" class="login-botao">Concluir cadastro</button></div></fieldset>
 </form>

                <a href="<%= request.getContextPath() %>/cadastro-servlet" class="login-esqueceu">Voltar ao cadastro de colaborador</a>
            </section>
        </div>
    </main>
<noscript><p class="login-erro">Ative o JavaScript para navegar entre as etapas.</p></noscript>
<script src="<%= request.getContextPath() %>/assets/js/regex.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/cadastro.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/mensagens.js"></script>
</body>
</html>
