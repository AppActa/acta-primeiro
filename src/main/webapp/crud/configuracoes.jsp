<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,br.com.acta.model.AdministradorGeral,br.com.acta.dao.AdministradorGeralDAO" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    private String imagemOtimizada(String url, int tamanho) {
        if (url == null || url.isBlank()) return "";
        return url.replace("/image/upload/", "/image/upload/c_fill,w_" + tamanho + ",h_" + tamanho + ",q_auto,f_auto/");
    }
%>
<%
AdministradorGeral administrador = (AdministradorGeral) session.getAttribute("usuarioLogado");
if (administrador == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
}
String nome = administrador.getNome() == null ? "" : administrador.getNome();
String email = administrador.getEmail() == null ? "" : administrador.getEmail();
String telefone = administrador.getTelefone() == null ? "" : administrador.getTelefone();
String imagem = administrador.getImagem() == null ? "" : administrador.getImagem();
String imagemPerfil = imagem.isBlank() ? request.getContextPath() + "/assets/images/foto_perfil.svg" : h(imagemOtimizada(imagem, 512));
List<String> empresasVinculadas = new AdministradorGeralDAO().buscarEmpresasAtivas(administrador.getAdm_geral_id());
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/assets/images/logo_compacta.svg" type="image/x-icon">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/crud.css?v=07443b8c11">
    <title>ACTA — Configurações</title>
    <script src="<%= request.getContextPath() %>/assets/js/regex.js?v=7c47ef420f" defer></script>
    <script src="<%= request.getContextPath() %>/assets/js/efeitos.js" defer></script>
    <script src="<%= request.getContextPath() %>/assets/js/cloudinary-profile.js" defer></script>
</head>
<body class="crud-admin" data-context-path="<%= request.getContextPath() %>">
<div class="sistema">
    <aside class="sidebar">
        <div class="sidebar-logo"><img src="<%= request.getContextPath() %>/assets/images/logo_oficial.svg" alt="ACTA"></div>
        <nav class="menu">
            <a href="<%= request.getContextPath() %>/empresa-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_empresas.svg" alt=""><span>Empresas</span></a>
            <a href="<%= request.getContextPath() %>/endereco-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_endereco.svg" alt=""><span>Endereço</span></a>
            <a href="<%= request.getContextPath() %>/colaborador-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_colaborador.svg" alt=""><span>Colaborador</span></a>
            <a href="<%= request.getContextPath() %>/tarefa-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_tarefa.svg" alt=""><span>Tarefa</span></a>
            <a href="<%= request.getContextPath() %>/planoAcao-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_plano_de_acao.svg" alt=""><span>Plano de ação</span></a>
            <a href="<%= request.getContextPath() %>/ciclo-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_ciclos.svg" alt=""><span>Ciclos</span></a>
            <a href="<%= request.getContextPath() %>/licoes-aprendidas-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_licoes_aprendidas.svg" alt=""><span>Lições Aprendidas</span></a>
            <a href="<%= request.getContextPath() %>/meta-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_metas.svg" alt=""><span>Metas</span></a>
            <a href="<%= request.getContextPath() %>/problema-servlet" class="menu-item"><img src="<%= request.getContextPath() %>/assets/images/icone_problemas.svg" alt=""><span>Problemas</span></a>
        </nav>
        <div class="perfil-barra">
            <a href="<%= request.getContextPath() %>/crud/configuracoes.jsp" class="perfil-admin">
                <div class="perfil-foto"><img src="<%= imagemPerfil %>" alt="Foto de perfil"></div>
                <div class="perfil-info"><span class="perfil-nome"><%= h(nome) %></span><span class="perfil-email"><%= h(email) %></span></div>
            </a>
            <a href="<%= request.getContextPath() %>/logout-servlet" class="botao botao-sair">Sair</a>
        </div>
    </aside>
    <main class="conteudo conteudo-config">
        <section class="config">
            <input type="checkbox" id="config-editar-perfil" class="config-toggle" aria-label="Editar dados">
            <input type="checkbox" id="config-modal-senha" class="config-toggle" aria-label="Alterar senha">
            <div class="config-principal">
                <h1 class="config-titulo">Configurações</h1>
                <div class="config-foto-area">
                    <div class="config-foto">
                        <img id="config-foto-preview" src="<%= imagemPerfil %>" alt="Foto de perfil">
                        <label for="config-editar-perfil" class="config-camera config-camera-modo" aria-label="Editar foto">
                            <img src="<%= request.getContextPath() %>/assets/images/icone_camera.svg" alt="">
                        </label>
                        <label for="config-foto-arquivo" class="config-camera config-camera-arquivo" aria-label="Escolher nova foto">
                            <img src="<%= request.getContextPath() %>/assets/images/icone_camera.svg" alt="">
                        </label>
                        <input type="file" id="config-foto-arquivo" accept="image/png,image/jpeg,image/webp" hidden>
                    </div>
                    <p id="config-foto-status" class="config-vazio" role="status" aria-live="polite"></p>
                </div>
            </div>
            <% if (request.getAttribute("mensagemErro") != null) { %>
            <p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
            <% } %>
            <form class="config-dados" id="form-perfil" method="post" action="<%= request.getContextPath() %>/administrador-geral-servlet">
                <input type="hidden" id="config-imagem-url" name="imagem" value="<%= h(imagem) %>">
                <div class="config-dados-topo"><h2>Meus dados</h2><label for="config-editar-perfil" class="botao botao-principal botao-pilula config-dados-editar">Editar</label></div>
                <div class="config-campos">
                    <div class="config-campo"><label for="config-nome" class="config-rotulo">Nome</label><p class="config-valor editavel"><%= h(nome) %></p><input type="text" id="config-nome" name="nome" maxlength="50" value="<%= h(nome) %>" autocomplete="name" placeholder="Digite o nome" required></div>
                    <div class="config-campo"><label for="config-email" class="config-rotulo">E-mail</label><p class="config-valor editavel"><a href="mailto:<%= h(email) %>"><%= h(email) %></a></p><input type="email" id="config-email" name="email" maxlength="80" value="<%= h(email) %>" autocomplete="email" placeholder="Digite o e-mail" required></div>
                    <div class="config-campo"><label for="config-telefone" class="config-rotulo">Telefone</label><p class="config-valor editavel" data-formato="telefone"><%= h(telefone) %></p><input type="tel" id="config-telefone" name="telefone" data-mascara="telefone" maxlength="15" pattern="^(?:\([0-9]{2}\)|[0-9]{2}) ?[0-9]{4,5}-?[0-9]{4}$" inputmode="numeric" value="<%= h(telefone) %>" autocomplete="tel-national" required></div>
                    <div class="config-campo"><span class="config-rotulo">Senha</span><p class="config-valor editavel">********</p><div class="config-senha-acao"><label for="config-modal-senha" class="botao botao-secundario">Alterar senha</label></div></div>
                    <div class="config-campo config-campo-largo">
                        <span class="config-rotulo">Empresas que administra</span>
                        <% if (empresasVinculadas.isEmpty()) { %>
                        <p class="config-vazio">Nenhuma empresa ativa vinculada.</p>
                        <% } else { %>
                        <ul class="lista-empresas">
                            <% for (String nomeEmpresa : empresasVinculadas) { %>
                            <li><%= h(nomeEmpresa) %></li>
                            <% } %>
                        </ul>
                        <% } %>
                    </div>
                </div>
                <div class="botoes-modal config-dados-botoes"><input type="hidden" name="acao" value="atualizar"><input type="hidden" name="id-adm-geral" value="<%= administrador.getAdm_geral_id() %>"><label for="config-editar-perfil" class="botao botao-cancelar">Cancelar</label><button type="submit" class="botao botao-principal">Confirmar edição</button></div>
            </form>
            <div class="modal-pagina config-modal"><div class="modal-fundo"><label for="config-modal-senha" class="config-modal-fundo-fechar" aria-label="Fechar"></label><div class="modal"><h2>Alterar senha</h2>
                <form class="formulario" method="post" action="<%= request.getContextPath() %>/administrador-geral-servlet">
                    <input type="hidden" name="acao" value="atualizarSenha"><input type="hidden" name="email" value="<%= h(email) %>">
                    <div class="campo"><label for="alt-senha-antiga">Senha antiga</label><input type="password" id="alt-senha-antiga" name="senha_antiga" maxlength="100" autocomplete="current-password" placeholder="Digite a senha antiga" required></div>
                    <div class="campo"><label for="alt-senha-nova">Nova senha</label><input type="password" id="alt-senha-nova" name="senha_nova" maxlength="100" autocomplete="new-password" placeholder="Digite a nova senha" required></div>
                    <div class="campo"><label for="alt-senha-confirmar">Confirmar nova senha</label><input type="password" id="alt-senha-confirmar" name="senha_confirmar" maxlength="100" autocomplete="new-password" placeholder="Repita a nova senha" data-confirma="alt-senha-nova" required></div>
                    <div class="botoes-modal"><label for="config-modal-senha" class="botao botao-cancelar">Cancelar</label><button type="submit" class="botao botao-principal">Confirmar</button></div>
                </form>
            </div></div></div>
        </section>
    </main>
</div>
</body>
</html>
