<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("metaList") == null) {
    response.sendRedirect(request.getContextPath() + "/meta-servlet");
    return;
}
List<Meta> metas = (List<Meta>) request.getAttribute("metaList");
List<Ciclo> cicloOpcoes = (List<Ciclo>) request.getAttribute("cicloOpcoes");
if (cicloOpcoes == null) cicloOpcoes = new ArrayList<Ciclo>();
List<PlanoAcao> planoAcaoOpcoes = (List<PlanoAcao>) request.getAttribute("planoAcaoOpcoes");
if (planoAcaoOpcoes == null) planoAcaoOpcoes = new ArrayList<PlanoAcao>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroPrioridade = "";
if (consulta && request.getParameter("prioridade") != null) filtroPrioridade = request.getParameter("prioridade");
String filtroStatus = "";
if (consulta && request.getParameter("status") != null) filtroStatus = request.getParameter("status");
String filtroDataInicial = "";
if (consulta && request.getParameter("dataInicial") != null) filtroDataInicial = request.getParameter("dataInicial");
String filtroDataFinal = "";
if (consulta && request.getParameter("dataFinal") != null) filtroDataFinal = request.getParameter("dataFinal");
String filtroCriadoDe = "";
if (consulta && request.getParameter("criadoDe") != null) filtroCriadoDe = request.getParameter("criadoDe");
String filtroCriadoAte = "";
if (consulta && request.getParameter("criadoAte") != null) filtroCriadoAte = request.getParameter("criadoAte");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
DateTimeFormatter formatoDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-meta";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Meta meta : metas) {
        if (String.valueOf(meta.getId_meta()).equals(request.getParameter("id-meta"))) {
            modalErro = "editar-meta-" + meta.getId_meta();
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
        <title>ACTA — Metas</title>
        <script src="<%= request.getContextPath() %>/assets/js/crud.js?v=391bad5325" defer></script>
    </head>
    <body class="crud-admin" data-reabrir="<%= h(modalErro) %>">
        <div class="sistema">
            <aside class="sidebar">
                <div class="sidebar-logo">
                    <img src="<%= request.getContextPath() %>/assets/images/logo_oficial.svg" alt="ACTA">
                </div>
                <nav class="menu">
                    <a href="<%= request.getContextPath() %>/empresa-servlet" class="menu-item">
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
                    <a href="<%= request.getContextPath() %>/meta-servlet" class="menu-item ativo">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_metas.svg" alt="">
                        <span>Metas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/problema-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_problemas.svg" alt="">
                        <span>Problemas</span>
                    </a>
                </nav>
                <div class="perfil-barra">
                    <a href="<%= request.getContextPath() %>/crud/configuracoes.jsp" class="perfil-admin">
                        <div class="perfil-foto">
                            <img src="<%= request.getContextPath() %>/assets/images/foto_perfil.svg" alt="Foto de perfil">
                        </div>
                        <div class="perfil-info">
                            <% br.com.acta.model.AdministradorGeral adm =
                                    (br.com.acta.model.AdministradorGeral) session.getAttribute("usuarioLogado"); %>
                            <span class="perfil-nome"><%= adm != null ? adm.getNome() : "Administrador" %></span>
                            <span class="perfil-email"><%= adm != null ? adm.getEmail() : "" %></span>
                        </div>
                    </a>
                    <a href="<%= request.getContextPath() %>/login.html" class="botao botao-sair">Sair</a>
                </div>
            </aside>
            <main class="conteudo">

                <section id="metas" class="pagina">
                    <h1>Metas</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-metas" form="filtros-metas-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-metas" aria-expanded="false">Filtros</button>
                            <section id="filtros-metas" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-metas-form" method="get" action="<%= request.getContextPath() %>/meta-servlet">
                                            <div class="campo">
                                                <label for="filtros-metas-prioridade">Prioridade</label>
                                                <select id="filtros-metas-prioridade" name="prioridade">
                                                    <option value="" <%= "".equals(filtroPrioridade) ? "selected" : "" %>>Todos</option>
                                                    <option value="ALTO" <%= "ALTO".equals(filtroPrioridade) ? "selected" : "" %>>Alto</option>
                                                    <option value="MEDIO" <%= "MEDIO".equals(filtroPrioridade) ? "selected" : "" %>>Médio</option>
                                                    <option value="BAIXO" <%= "BAIXO".equals(filtroPrioridade) ? "selected" : "" %>>Baixo</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-metas-status">Status</label>
                                                <select id="filtros-metas-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="ABAIXO_DO_ESPERADO" <%= "ABAIXO_DO_ESPERADO".equals(filtroStatus) ? "selected" : "" %>>Abaixo do esperado</option>
                                                    <option value="REGULAR" <%= "REGULAR".equals(filtroStatus) ? "selected" : "" %>>Regular</option>
                                                    <option value="ACIMA_DO_ESPERADO" <%= "ACIMA_DO_ESPERADO".equals(filtroStatus) ? "selected" : "" %>>Acima do esperado</option>
                                                </select>
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Prazo</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-metas-dataInicial">De</label><input type="date" id="filtros-metas-dataInicial" name="dataInicial" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataInicial) %>"></div>
                                                    <div class="campo"><label for="filtros-metas-dataFinal">Até</label><input type="date" id="filtros-metas-dataFinal" name="dataFinal" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataFinal) %>"></div>
                                                </div>
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Criado em</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-metas-criadoDe">De</label><input type="date" id="filtros-metas-criadoDe" name="criadoDe" min="1900-01-01" max="2100-12-31" value="<%= h(filtroCriadoDe) %>"></div>
                                                    <div class="campo"><label for="filtros-metas-criadoAte">Até</label><input type="date" id="filtros-metas-criadoAte" name="criadoAte" min="1900-01-01" max="2100-12-31" value="<%= h(filtroCriadoAte) %>"></div>
                                                </div>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/meta-servlet#metas" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-meta" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "meta".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="meta">
<input type="hidden" name="direcao" value="<%= "meta".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Meta">Meta
<% if ("meta".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Objetivo</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "ciclo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="ciclo">
<input type="hidden" name="direcao" value="<%= "ciclo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Ciclo">Ciclo
<% if ("ciclo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "plano_acao".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="plano_acao">
<input type="hidden" name="direcao" value="<%= "plano_acao".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Plano de ação">Plano de ação
<% if ("plano_acao".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "prioridade".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="prioridade">
<input type="hidden" name="direcao" value="<%= "prioridade".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Prioridade">Prioridade
<% if ("prioridade".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "prazo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="prazo">
<input type="hidden" name="direcao" value="<%= "prazo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Prazo">Prazo
<% if ("prazo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "criado_em".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/meta-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="criado_em">
<input type="hidden" name="direcao" value="<%= "criado_em".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Criado em">Criado em
<% if ("criado_em".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (metas.isEmpty()) { %>
<tr><td colspan="12">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Meta meta : metas) { %>
<tr><td><%= h(meta.getId_meta()) %></td><td><%= h(meta.getMeta() == null || meta.getMeta().isBlank() ? "—" : meta.getMeta()) %></td><td><%= h(meta.getObjetivo() == null || meta.getObjetivo().isBlank() ? "—" : meta.getObjetivo()) %></td><td><%= h(meta.getNome_ciclo() == null || meta.getNome_ciclo().isBlank() ? "—" : meta.getNome_ciclo()) %></td><td><%= h(meta.getNome_plano_acao() == null || meta.getNome_plano_acao().isBlank() ? "—" : meta.getNome_plano_acao()) %></td><td><%
String textoPrioridade = "—";
if (meta.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (meta.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (meta.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></td><td><%= h(meta.getPrazo() == null ? "—" : meta.getPrazo().toLocalDate().format(formatoData)) %></td><td><%
String textoStatus = "—";
if (meta.getStatus() == StatusMeta.ABAIXO_DO_ESPERADO) {
    textoStatus = "Abaixo do esperado";
}
else if (meta.getStatus() == StatusMeta.REGULAR) {
    textoStatus = "Regular";
}
else if (meta.getStatus() == StatusMeta.ACIMA_DO_ESPERADO) {
    textoStatus = "Acima do esperado";
}
%>
<%= h(textoStatus) %></td><td><%= h(meta.getCriando_em() == null ? "—" : meta.getCriando_em().atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).format(formatoDataHora)) %></td><td class="coluna-acao"><a href="#ver-meta-<%= meta.getId_meta() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-meta-<%= meta.getId_meta() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-meta-<%= meta.getId_meta() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Meta meta : metas) { %>
<section id="ver-meta-<%= meta.getId_meta() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes da meta</h2>
                                    <a href="#metas" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Meta</span><span class="detalhe-valor"><%= h(meta.getMeta() == null || meta.getMeta().isBlank() ? "—" : meta.getMeta()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Objetivo</span><span class="detalhe-valor"><%= h(meta.getObjetivo() == null || meta.getObjetivo().isBlank() ? "—" : meta.getObjetivo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Ciclo</span><span class="detalhe-valor"><%= h(meta.getNome_ciclo() == null || meta.getNome_ciclo().isBlank() ? "—" : meta.getNome_ciclo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Plano de ação</span><span class="detalhe-valor"><%= h(meta.getNome_plano_acao() == null || meta.getNome_plano_acao().isBlank() ? "—" : meta.getNome_plano_acao()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Prioridade</span><span class="detalhe-valor"><%
String textoPrioridade = "—";
if (meta.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (meta.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (meta.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Prazo</span><span class="detalhe-valor"><%= h(meta.getPrazo() == null ? "—" : meta.getPrazo().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (meta.getStatus() == StatusMeta.ABAIXO_DO_ESPERADO) {
    textoStatus = "Abaixo do esperado";
}
else if (meta.getStatus() == StatusMeta.REGULAR) {
    textoStatus = "Regular";
}
else if (meta.getStatus() == StatusMeta.ACIMA_DO_ESPERADO) {
    textoStatus = "Acima do esperado";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Criado em</span><span class="detalhe-valor"><%= h(meta.getCriando_em() == null ? "—" : meta.getCriando_em().atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).format(formatoDataHora)) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(meta.getDescricao_meta() == null || meta.getDescricao_meta().isBlank() ? "—" : meta.getDescricao_meta()) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorPrioridadeCadastro = "";
if (recuperarCadastro) {
    valorPrioridadeCadastro = request.getParameter("prioridade");
}
String valorStatusCadastro = "";
if (recuperarCadastro) {
    valorStatusCadastro = request.getParameter("status");
}
String valorIdCicloCadastro = "";
if (recuperarCadastro) {
    valorIdCicloCadastro = request.getParameter("id_ciclo");
}
String valorIdPlanoAcaoCadastro = "";
if (recuperarCadastro) {
    valorIdPlanoAcaoCadastro = request.getParameter("id_plano_acao");
}
%>
<section id="cadastrar-meta" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Cadastrar meta</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/meta-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-meta-meta">Meta</label>
                                        <input type="text" id="cad-meta-meta" name="meta" maxlength="100" placeholder="Digite meta" required value="<%= h(recuperarCadastro ? request.getParameter("meta") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-objetivo">Objetivo</label>
                                        <input type="text" id="cad-meta-objetivo" name="objetivo" maxlength="200" placeholder="Digite objetivo" value="<%= h(recuperarCadastro ? request.getParameter("objetivo") : "") %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-meta-descricao_meta">Descrição</label>
                                        <textarea id="cad-meta-descricao_meta" name="descricao_meta" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao_meta") : "") %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-prioridade">Prioridade</label>
                                        <select id="cad-meta-prioridade" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-prazo">Prazo</label>
                                        <input type="date" id="cad-meta-prazo" name="prazo" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarCadastro ? request.getParameter("prazo") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-status">Status</label>
                                        <select id="cad-meta-status" name="status" required>
                                            <option value="ABAIXO_DO_ESPERADO" <%= "ABAIXO_DO_ESPERADO".equals(valorStatusCadastro) ? "selected" : "" %>>Abaixo do esperado</option>
                                            <option value="REGULAR" <%= "REGULAR".equals(valorStatusCadastro) ? "selected" : "" %>>Regular</option>
                                            <option value="ACIMA_DO_ESPERADO" <%= "ACIMA_DO_ESPERADO".equals(valorStatusCadastro) ? "selected" : "" %>>Acima do esperado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-id_ciclo">Ciclo</label>
                                        <input type="text" id="cad-meta-id_ciclo" data-lista="sugestoes-fk-ciclo-lista" data-validar-lista data-campo-valor="cad-meta-id_ciclo-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-meta-id_ciclo-valor" name="id_ciclo" value="<%= h(valorIdCicloCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-meta-id_plano_acao">Plano de ação</label>
                                        <input type="text" id="cad-meta-id_plano_acao" data-lista="sugestoes-fk-planoacao-lista" data-validar-lista data-campo-valor="cad-meta-id_plano_acao-valor" autocomplete="off" placeholder="Sem vínculo">
<input type="hidden" id="cad-meta-id_plano_acao-valor" name="id_plano_acao" value="<%= h(valorIdPlanoAcaoCadastro) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#metas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Meta meta : metas) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(meta.getId_meta()).equals(request.getParameter("id-meta"));
String valorPrioridadeEdicao = "";
if (recuperarEdicao) {
    valorPrioridadeEdicao = request.getParameter("prioridade");
} else if (meta.getPrioridade() != null) {
    valorPrioridadeEdicao = meta.getPrioridade().toString();
}
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (meta.getStatus() != null) {
    valorStatusEdicao = meta.getStatus().toString();
}
String valorIdCicloEdicao = "";
if (recuperarEdicao) {
    valorIdCicloEdicao = request.getParameter("id_ciclo");
} else if (meta.getId_ciclo() != null) {
    valorIdCicloEdicao = meta.getId_ciclo().toString();
}
String valorIdPlanoAcaoEdicao = "";
if (recuperarEdicao) {
    valorIdPlanoAcaoEdicao = request.getParameter("id_plano_acao");
} else if (meta.getId_plano_acao() != null) {
    valorIdPlanoAcaoEdicao = meta.getId_plano_acao().toString();
}
%>
<section id="editar-meta-<%= meta.getId_meta() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Editar meta</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/meta-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-meta" value="<%= meta.getId_meta() %>">
                                    <div class="campo">
                                        <label for="edi-meta-meta-<%= meta.getId_meta() %>">Meta</label>
                                        <input type="text" id="edi-meta-meta-<%= meta.getId_meta() %>" name="meta" maxlength="100" placeholder="Digite meta" required value="<%= h(recuperarEdicao ? request.getParameter("meta") : meta.getMeta()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-objetivo-<%= meta.getId_meta() %>">Objetivo</label>
                                        <input type="text" id="edi-meta-objetivo-<%= meta.getId_meta() %>" name="objetivo" maxlength="200" placeholder="Digite objetivo" value="<%= h(recuperarEdicao ? request.getParameter("objetivo") : meta.getObjetivo()) %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-meta-descricao_meta-<%= meta.getId_meta() %>">Descrição</label>
                                        <textarea id="edi-meta-descricao_meta-<%= meta.getId_meta() %>" name="descricao_meta" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao_meta") : meta.getDescricao_meta()) %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-prioridade-<%= meta.getId_meta() %>">Prioridade</label>
                                        <select id="edi-meta-prioridade-<%= meta.getId_meta() %>" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-prazo-<%= meta.getId_meta() %>">Prazo</label>
                                        <input type="date" id="edi-meta-prazo-<%= meta.getId_meta() %>" name="prazo" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarEdicao ? request.getParameter("prazo") : meta.getPrazo()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-status-<%= meta.getId_meta() %>">Status</label>
                                        <select id="edi-meta-status-<%= meta.getId_meta() %>" name="status" required>
                                            <option value="ABAIXO_DO_ESPERADO" <%= "ABAIXO_DO_ESPERADO".equals(valorStatusEdicao) ? "selected" : "" %>>Abaixo do esperado</option>
                                            <option value="REGULAR" <%= "REGULAR".equals(valorStatusEdicao) ? "selected" : "" %>>Regular</option>
                                            <option value="ACIMA_DO_ESPERADO" <%= "ACIMA_DO_ESPERADO".equals(valorStatusEdicao) ? "selected" : "" %>>Acima do esperado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-id_ciclo-<%= meta.getId_meta() %>">Ciclo</label>
                                        <input type="text" id="edi-meta-id_ciclo-<%= meta.getId_meta() %>" data-lista="sugestoes-fk-ciclo-lista" data-validar-lista data-campo-valor="edi-meta-id_ciclo-<%= meta.getId_meta() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-meta-id_ciclo-<%= meta.getId_meta() %>-valor" name="id_ciclo" value="<%= h(valorIdCicloEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-meta-id_plano_acao-<%= meta.getId_meta() %>">Plano de ação</label>
                                        <input type="text" id="edi-meta-id_plano_acao-<%= meta.getId_meta() %>" data-lista="sugestoes-fk-planoacao-lista" data-validar-lista data-campo-valor="edi-meta-id_plano_acao-<%= meta.getId_meta() %>-valor" autocomplete="off" placeholder="Sem vínculo">
<input type="hidden" id="edi-meta-id_plano_acao-<%= meta.getId_meta() %>-valor" name="id_plano_acao" value="<%= h(valorIdPlanoAcaoEdicao) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#metas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Meta meta : metas) { %>
<section id="excluir-meta-<%= meta.getId_meta() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(meta.getMeta() == null || meta.getMeta().isBlank() ? "—" : meta.getMeta()) %></p>
                                <h2>Excluir meta</h2>
                                <p>Tem certeza que deseja excluir esta meta?</p>
                                <form method="post" action="<%= request.getContextPath() %>/meta-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-meta" value="<%= meta.getId_meta() %>">
                                    <div class="botoes-modal">
                                    <a href="#metas" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

                <div id="sugestoes-fk-ciclo-lista" class="lista-escolhas" role="listbox" hidden>
<% for (Ciclo opcao : cicloOpcoes) {
    String nomeOpcao = opcao.getNome();
    if (nomeOpcao != null && !nomeOpcao.isBlank()) {
        nomeOpcao = nomeOpcao.trim();
        int nomesIguais = 0;
        for (Ciclo outra : cicloOpcoes) {
            String outroNome = outra.getNome();
            if (outroNome != null && nomeOpcao.equals(outroNome.trim())) nomesIguais++;
        }
        if (nomesIguais > 1) nomeOpcao += " (#" + opcao.getId_ciclo() + ")"; %>
<button type="button" role="option" data-valor="<%= opcao.getId_ciclo() %>"><%= h(nomeOpcao) %></button>
<% }
} %>
                <button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
                <div id="sugestoes-fk-planoacao-lista" class="lista-escolhas" role="listbox" hidden>
<% for (PlanoAcao opcao : planoAcaoOpcoes) {
    String nomeOpcao = opcao.getNome();
    if (nomeOpcao != null && !nomeOpcao.isBlank()) {
        nomeOpcao = nomeOpcao.trim();
        int nomesIguais = 0;
        for (PlanoAcao outra : planoAcaoOpcoes) {
            String outroNome = outra.getNome();
            if (outroNome != null && nomeOpcao.equals(outroNome.trim())) nomesIguais++;
        }
        if (nomesIguais > 1) nomeOpcao += " (#" + opcao.getId_plano_acao() + ")"; %>
<button type="button" role="option" data-valor="<%= opcao.getId_plano_acao() %>"><%= h(nomeOpcao) %></button>
<% }
} %>
                <button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
            </main>
        </div>
    </body>
</html>
