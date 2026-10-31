<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("empresaList") == null) {
    response.sendRedirect(request.getContextPath() + "/empresa-servlet");
    return;
}
List<Empresa> empresas = (List<Empresa>) request.getAttribute("empresaList");
List<String> setorList = (List<String>) request.getAttribute("setorList");
if (setorList == null) setorList = new ArrayList<String>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroSetor = "";
if (consulta && request.getParameter("setor") != null) filtroSetor = request.getParameter("setor");
String filtroStatus = "";
if (consulta && request.getParameter("status") != null) filtroStatus = request.getParameter("status");
String filtroTamanho = "";
if (consulta && request.getParameter("tamanho") != null) filtroTamanho = request.getParameter("tamanho");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-empresa";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Empresa empresa : empresas) {
        if (String.valueOf(empresa.getId_empresa()).equals(request.getParameter("id-empresa"))) {
            modalErro = "editar-empresa-" + empresa.getId_empresa();
        }
    }
}
%>
<!DOCTYPE html>

<html lang="pt-br">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="shortcut icon" href="<%= request.getContextPath() %>/assets/images/logo_compacta.svg" type="image/x-icon">
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/crud.css?v=07443b8c11">
        <title>ACTA — Empresas</title>
        <script src="<%= request.getContextPath() %>/assets/js/crud.js?v=391bad5325" defer></script>
        <script src="<%= request.getContextPath() %>/assets/js/regex.js?v=7c47ef420f" defer></script>
    </head>
    <body class="crud-admin" data-reabrir="<%= h(modalErro) %>">
        <div class="sistema">
            <aside class="sidebar">
                <div class="sidebar-logo">
                    <img src="<%= request.getContextPath() %>/assets/images/logo_oficial.svg" alt="ACTA">
                </div>
                <nav class="menu">
                    <a href="<%= request.getContextPath() %>/empresa-servlet" class="menu-item ativo">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_empresas.svg" alt="">
                        <span>Empresas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/endereco-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_endereco.svg" alt="">
                        <span>Endereço</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/colaborador-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_colaborador.svg" alt="">
                        <span>Colaborador</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/tarefa-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_tarefa.svg" alt="">
                        <span>Tarefa</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/planoAcao-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_plano_de_acao.svg" alt="">
                        <span>Plano de ação</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/ciclo-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_ciclos.svg" alt="">
                        <span>Ciclos</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/licoes-aprendidas-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_licoes_aprendidas.svg" alt="">
                        <span>Lições Aprendidas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/meta-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_metas.svg" alt="">
                        <span>Metas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/problema-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_problemas.svg" alt="">
                        <span>Problemas</span>
                    </a>
                </nav>
                <div class="perfil-barra">
                    <% br.com.acta.model.AdministradorGeral adm = (br.com.acta.model.AdministradorGeral) session.getAttribute("usuarioLogado"); %>
                    <a href="<%= request.getContextPath() %>/crud/configuracoes.jsp" class="perfil-admin">
                        <div class="perfil-foto">
                            <img src="<%= adm != null && adm.getImagem() != null && !adm.getImagem().isBlank() ? h(adm.getImagem().replace("/image/upload/", "/image/upload/c_fill,w_96,h_96,q_auto,f_auto/")) : request.getContextPath() + "/assets/images/foto_perfil.svg" %>" alt="Foto de perfil">
                        </div>
                        <div class="perfil-info">
                            
                            <span class="perfil-nome"><%= adm != null ? adm.getNome() : "Administrador" %></span>
                            <span class="perfil-email"><%= adm != null ? adm.getEmail() : "" %></span>
                        </div>
                    </a>
                    <a href="<%= request.getContextPath() %>/logout-servlet" class="botao botao-sair">Sair</a>
                </div>
            </aside>
            <main class="conteudo">

                <section id="empresas" class="pagina">
                    <h1>Empresas</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-empresas" form="filtros-empresas-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-empresas" aria-expanded="false">Filtros</button>
                            <section id="filtros-empresas" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-empresas-form" method="get" action="<%= request.getContextPath() %>/empresa-servlet">
                                            <div class="campo">
                                                <label for="filtros-empresas-setor">Setor</label>
                                                <input type="text" id="filtros-empresas-setor" name="setor" data-lista="sugestoes-empresas-setor-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroSetor) %>">
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-empresas-status">Status</label>
                                                <select id="filtros-empresas-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="ATIVA" <%= "ATIVA".equals(filtroStatus) ? "selected" : "" %>>Ativa</option>
                                                    <option value="INATIVA" <%= "INATIVA".equals(filtroStatus) ? "selected" : "" %>>Inativa</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-empresas-tamanho">Tamanho</label>
                                                <select id="filtros-empresas-tamanho" name="tamanho">
                                                    <option value="" <%= "".equals(filtroTamanho) ? "selected" : "" %>>Todos</option>
                                                    <option value="GRANDE" <%= "GRANDE".equals(filtroTamanho) ? "selected" : "" %>>Grande</option>
                                                    <option value="PEQUENA" <%= "PEQUENA".equals(filtroTamanho) ? "selected" : "" %>>Pequena</option>
                                                    <option value="MEDIA" <%= "MEDIA".equals(filtroTamanho) ? "selected" : "" %>>Média</option>
                                                </select>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/empresa-servlet#empresas" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-empresa" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/empresa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="setor" value="<%= h(filtroSetor) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="tamanho" value="<%= h(filtroTamanho) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "nome".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/empresa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="setor" value="<%= h(filtroSetor) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="tamanho" value="<%= h(filtroTamanho) %>">
<input type="hidden" name="ordem" value="nome">
<input type="hidden" name="direcao" value="<%= "nome".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Nome">Nome
<% if ("nome".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "setor".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/empresa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="setor" value="<%= h(filtroSetor) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="tamanho" value="<%= h(filtroTamanho) %>">
<input type="hidden" name="ordem" value="setor">
<input type="hidden" name="direcao" value="<%= "setor".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Setor">Setor
<% if ("setor".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">CNPJ</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/empresa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="setor" value="<%= h(filtroSetor) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="tamanho" value="<%= h(filtroTamanho) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "tamanho".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/empresa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="setor" value="<%= h(filtroSetor) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="tamanho" value="<%= h(filtroTamanho) %>">
<input type="hidden" name="ordem" value="tamanho">
<input type="hidden" name="direcao" value="<%= "tamanho".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Tamanho">Tamanho
<% if ("tamanho".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (empresas.isEmpty()) { %>
<tr><td colspan="9">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Empresa empresa : empresas) { %>
<tr><td><%= h(empresa.getId_empresa()) %></td><td><%= h(empresa.getNome() == null || empresa.getNome().isBlank() ? "—" : empresa.getNome()) %></td><td><%= h(empresa.getSetor() == null || empresa.getSetor().isBlank() ? "—" : empresa.getSetor()) %></td><td data-formato="cnpj"><%= h(empresa.getCnpj() == null || empresa.getCnpj().isBlank() ? "—" : empresa.getCnpj()) %></td><td><%
String textoStatus = "—";
if (empresa.getStatus() == Status.ATIVA) {
    textoStatus = "Ativa";
}
else if (empresa.getStatus() == Status.INATIVA) {
    textoStatus = "Inativa";
}
%>
<%= h(textoStatus) %></td><td><%
String textoTamanho = "—";
if (empresa.getTamanho() == TamanhoEmpresa.GRANDE) {
    textoTamanho = "Grande";
}
else if (empresa.getTamanho() == TamanhoEmpresa.PEQUENA) {
    textoTamanho = "Pequena";
}
else if (empresa.getTamanho() == TamanhoEmpresa.MEDIA) {
    textoTamanho = "Média";
}
%>
<%= h(textoTamanho) %></td><td class="coluna-acao"><a href="#ver-empresa-<%= empresa.getId_empresa() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-empresa-<%= empresa.getId_empresa() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-empresa-<%= empresa.getId_empresa() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Empresa empresa : empresas) { %>
<section id="ver-empresa-<%= empresa.getId_empresa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes da empresa</h2>
                                    <a href="#empresas" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Nome</span><span class="detalhe-valor"><%= h(empresa.getNome() == null || empresa.getNome().isBlank() ? "—" : empresa.getNome()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Setor</span><span class="detalhe-valor"><%= h(empresa.getSetor() == null || empresa.getSetor().isBlank() ? "—" : empresa.getSetor()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">CNPJ</span><span class="detalhe-valor" data-formato="cnpj"><%= h(empresa.getCnpj() == null || empresa.getCnpj().isBlank() ? "—" : empresa.getCnpj()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (empresa.getStatus() == Status.ATIVA) {
    textoStatus = "Ativa";
}
else if (empresa.getStatus() == Status.INATIVA) {
    textoStatus = "Inativa";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Tamanho</span><span class="detalhe-valor"><%
String textoTamanho = "—";
if (empresa.getTamanho() == TamanhoEmpresa.GRANDE) {
    textoTamanho = "Grande";
}
else if (empresa.getTamanho() == TamanhoEmpresa.PEQUENA) {
    textoTamanho = "Pequena";
}
else if (empresa.getTamanho() == TamanhoEmpresa.MEDIA) {
    textoTamanho = "Média";
}
%>
<%= h(textoTamanho) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorStatusCadastro = "";
if (recuperarCadastro) {
    valorStatusCadastro = request.getParameter("status");
}
String valorTamanhoCadastro = "";
if (recuperarCadastro) {
    valorTamanhoCadastro = request.getParameter("tamanho");
}
%>
<section id="cadastrar-empresa" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal">
                                <h2>Cadastrar empresa</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/empresa-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-empresa-nome">Nome</label>
                                        <input type="text" id="cad-empresa-nome" name="nome" maxlength="50" placeholder="Digite o nome" required value="<%= h(recuperarCadastro ? request.getParameter("nome") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-empresa-setor">Setor</label>
                                        <input type="text" id="cad-empresa-setor" name="setor" maxlength="30" placeholder="Digite o setor" required data-lista="sugestoes-empresas-setor-lista" value="<%= h(recuperarCadastro ? request.getParameter("setor") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-empresa-cnpj">CNPJ</label>
                                        <input type="text" id="cad-empresa-cnpj" name="cnpj" data-mascara="cnpj" maxlength="18" placeholder="CNPJ com ou sem pontuação" required pattern="^[A-Za-z0-9]{2}\.?[A-Za-z0-9]{3}\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?[0-9]{2}$" value="<%= h(recuperarCadastro ? request.getParameter("cnpj") : "") %>">
                                    </div>

                                    <div class="campo">
                                        <label for="cad-empresa-status">Status</label>
                                        <select id="cad-empresa-status" name="status" required>
                                            <option value="ATIVA" <%= "ATIVA".equals(valorStatusCadastro) ? "selected" : "" %>>Ativa</option>
                                            <option value="INATIVA" <%= "INATIVA".equals(valorStatusCadastro) ? "selected" : "" %>>Inativa</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-empresa-tamanho">Tamanho</label>
                                        <select id="cad-empresa-tamanho" name="tamanho" required>
                                            <option value="GRANDE" <%= "GRANDE".equals(valorTamanhoCadastro) ? "selected" : "" %>>Grande</option>
                                            <option value="PEQUENA" <%= "PEQUENA".equals(valorTamanhoCadastro) ? "selected" : "" %>>Pequena</option>
                                            <option value="MEDIA" <%= "MEDIA".equals(valorTamanhoCadastro) ? "selected" : "" %>>Média</option>
                                        </select>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#empresas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Empresa empresa : empresas) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(empresa.getId_empresa()).equals(request.getParameter("id-empresa"));
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (empresa.getStatus() != null) {
    valorStatusEdicao = empresa.getStatus().toString();
}
String valorTamanhoEdicao = "";
if (recuperarEdicao) {
    valorTamanhoEdicao = request.getParameter("tamanho");
} else if (empresa.getTamanho() != null) {
    valorTamanhoEdicao = empresa.getTamanho().toString();
}
%>
<section id="editar-empresa-<%= empresa.getId_empresa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal">
                                <h2>Editar empresa</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/empresa-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-empresa" value="<%= empresa.getId_empresa() %>">
                                    <div class="campo">
                                        <label for="edi-empresa-nome-<%= empresa.getId_empresa() %>">Nome</label>
                                        <input type="text" id="edi-empresa-nome-<%= empresa.getId_empresa() %>" name="nome" maxlength="50" placeholder="Digite o nome" required value="<%= h(recuperarEdicao ? request.getParameter("nome") : empresa.getNome()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-empresa-setor-<%= empresa.getId_empresa() %>">Setor</label>
                                        <input type="text" id="edi-empresa-setor-<%= empresa.getId_empresa() %>" name="setor" maxlength="30" placeholder="Digite o setor" required data-lista="sugestoes-empresas-setor-lista" value="<%= h(recuperarEdicao ? request.getParameter("setor") : empresa.getSetor()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-empresa-cnpj-<%= empresa.getId_empresa() %>">CNPJ</label>
                                        <input type="text" id="edi-empresa-cnpj-<%= empresa.getId_empresa() %>" name="cnpj" data-mascara="cnpj" maxlength="18" placeholder="CNPJ com ou sem pontuação" required pattern="^[A-Za-z0-9]{2}\.?[A-Za-z0-9]{3}\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?[0-9]{2}$" value="<%= h(recuperarEdicao ? request.getParameter("cnpj") : empresa.getCnpj()) %>">
                                    </div>

                                    <div class="campo">
                                        <label for="edi-empresa-status-<%= empresa.getId_empresa() %>">Status</label>
                                        <select id="edi-empresa-status-<%= empresa.getId_empresa() %>" name="status" required>
                                            <option value="ATIVA" <%= "ATIVA".equals(valorStatusEdicao) ? "selected" : "" %>>Ativa</option>
                                            <option value="INATIVA" <%= "INATIVA".equals(valorStatusEdicao) ? "selected" : "" %>>Inativa</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-empresa-tamanho-<%= empresa.getId_empresa() %>">Tamanho</label>
                                        <select id="edi-empresa-tamanho-<%= empresa.getId_empresa() %>" name="tamanho" required>
                                            <option value="GRANDE" <%= "GRANDE".equals(valorTamanhoEdicao) ? "selected" : "" %>>Grande</option>
                                            <option value="PEQUENA" <%= "PEQUENA".equals(valorTamanhoEdicao) ? "selected" : "" %>>Pequena</option>
                                            <option value="MEDIA" <%= "MEDIA".equals(valorTamanhoEdicao) ? "selected" : "" %>>Média</option>
                                        </select>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#empresas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Empresa empresa : empresas) { %>
<section id="excluir-empresa-<%= empresa.getId_empresa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(empresa.getNome() == null || empresa.getNome().isBlank() ? "—" : empresa.getNome()) %></p>
                                <h2>Excluir empresa</h2>
                                <p>Tem certeza que deseja excluir esta empresa?</p>
                                <form method="post" action="<%= request.getContextPath() %>/empresa-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-empresa" value="<%= empresa.getId_empresa() %>">
                                    <div class="botoes-modal">
                                    <a href="#empresas" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>


<div id="sugestoes-empresas-setor-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : setorList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<button type="button" role="option" data-valor="<%= h(sugestao) %>"><%= h(sugestao) %></button>
<% } %>
<% } %>
<button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
            </main>
        </div>
    </body>
</html>
