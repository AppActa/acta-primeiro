<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("problemaList") == null) {
    response.sendRedirect(request.getContextPath() + "/problema-servlet");
    return;
}
List<Problema> problemas = (List<Problema>) request.getAttribute("problemaList");
List<Ciclo> cicloOpcoes = (List<Ciclo>) request.getAttribute("cicloOpcoes");
if (cicloOpcoes == null) cicloOpcoes = new ArrayList<Ciclo>();
List<PlanoAcao> planoAcaoOpcoes = (List<PlanoAcao>) request.getAttribute("planoAcaoOpcoes");
if (planoAcaoOpcoes == null) planoAcaoOpcoes = new ArrayList<PlanoAcao>();
List<Colaborador> colaboradorOpcoes = (List<Colaborador>) request.getAttribute("colaboradorOpcoes");
if (colaboradorOpcoes == null) colaboradorOpcoes = new ArrayList<Colaborador>();
List<String> origemList = (List<String>) request.getAttribute("origemList");
if (origemList == null) origemList = new ArrayList<String>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroPeso = "";
if (consulta && request.getParameter("peso") != null) filtroPeso = request.getParameter("peso");
String filtroStatus = "";
if (consulta && request.getParameter("status") != null) filtroStatus = request.getParameter("status");
String filtroOrigem = "";
if (consulta && request.getParameter("origem") != null) filtroOrigem = request.getParameter("origem");
String filtroDataInicial = "";
if (consulta && request.getParameter("dataInicial") != null) filtroDataInicial = request.getParameter("dataInicial");
String filtroDataFinal = "";
if (consulta && request.getParameter("dataFinal") != null) filtroDataFinal = request.getParameter("dataFinal");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-problema";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Problema problema : problemas) {
        if (String.valueOf(problema.getId_problema()).equals(request.getParameter("id-problema"))) {
            modalErro = "editar-problema-" + problema.getId_problema();
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
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/crud.css">
        <title>ACTA — Problemas</title>
        <script src="<%= request.getContextPath() %>/assets/js/crud.js" defer></script>
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
                    <a href="<%= request.getContextPath() %>/meta-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_metas.svg" alt="">
                        <span>Metas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/problema-servlet" class="menu-item ativo">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_problemas.svg" alt="">
                        <span>Problemas</span>
                    </a>
                </nav>
                <div class="perfil-barra">
                    <a href="<%= request.getContextPath() %>/crud/configurações.html" class="perfil-admin">
                        <div class="perfil-foto">
                            <img src="<%= request.getContextPath() %>/assets/images/foto_perfil.svg" alt="Foto de perfil">
                        </div>
                        <div class="perfil-info">
                            <span class="perfil-nome">Área administrativa</span>
                            <span class="perfil-email">ACTA</span>
                        </div>
                    </a>
                    <a href="<%= request.getContextPath() %>/login.html" class="botao botao-sair">Sair</a>
                </div>
            </aside>
            <main class="conteudo">

                <section id="problemas" class="pagina">
                    <h1>Problemas</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-problemas" form="filtros-problemas-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-problemas" aria-expanded="false">Filtros</button>
                            <section id="filtros-problemas" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-problemas-form" method="get" action="<%= request.getContextPath() %>/problema-servlet">
                                            <div class="campo">
                                                <label for="filtros-problemas-peso">Gravidade</label>
                                                <select id="filtros-problemas-peso" name="peso">
                                                    <option value="" <%= "".equals(filtroPeso) ? "selected" : "" %>>Todos</option>
                                                    <option value="ALTO" <%= "ALTO".equals(filtroPeso) ? "selected" : "" %>>Alto</option>
                                                    <option value="MEDIO" <%= "MEDIO".equals(filtroPeso) ? "selected" : "" %>>Médio</option>
                                                    <option value="BAIXO" <%= "BAIXO".equals(filtroPeso) ? "selected" : "" %>>Baixo</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-problemas-status">Status</label>
                                                <select id="filtros-problemas-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="EM_ANALISE" <%= "EM_ANALISE".equals(filtroStatus) ? "selected" : "" %>>Em análise</option>
                                                    <option value="EM_RESOLUCAO" <%= "EM_RESOLUCAO".equals(filtroStatus) ? "selected" : "" %>>Em resolução</option>
                                                    <option value="RESOLVIDO" <%= "RESOLVIDO".equals(filtroStatus) ? "selected" : "" %>>Resolvido</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-problemas-origem">Origem</label>
                                                <input type="text" id="filtros-problemas-origem" name="origem" list="sugestoes-problemas-origem" value="<%= h(filtroOrigem) %>">
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Encontrado em</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-problemas-dataInicial">De</label><input type="date" id="filtros-problemas-dataInicial" name="dataInicial" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataInicial) %>"></div>
                                                    <div class="campo"><label for="filtros-problemas-dataFinal">Até</label><input type="date" id="filtros-problemas-dataFinal" name="dataFinal" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataFinal) %>"></div>
                                                </div>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/problema-servlet#problemas" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-problema" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "titulo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="titulo">
<input type="hidden" name="direcao" value="<%= "titulo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Título">Título
<% if ("titulo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "peso".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="peso">
<input type="hidden" name="direcao" value="<%= "peso".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Gravidade">Gravidade
<% if ("peso".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "ciclo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="ciclo">
<input type="hidden" name="direcao" value="<%= "ciclo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Ciclo">Ciclo
<% if ("ciclo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "plano_acao".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="plano_acao">
<input type="hidden" name="direcao" value="<%= "plano_acao".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Plano de ação">Plano de ação
<% if ("plano_acao".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Colaborador</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "encontrado_em".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="encontrado_em">
<input type="hidden" name="direcao" value="<%= "encontrado_em".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Encontrado em">Encontrado em
<% if ("encontrado_em".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "origem".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/problema-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="peso" value="<%= h(filtroPeso) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="origem" value="<%= h(filtroOrigem) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="origem">
<input type="hidden" name="direcao" value="<%= "origem".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Origem">Origem
<% if ("origem".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Solução</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (problemas.isEmpty()) { %>
<tr><td colspan="13">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Problema problema : problemas) { %>
<tr><td><%= h(problema.getId_problema()) %></td><td><%= h(problema.getTitulo() == null || problema.getTitulo().isBlank() ? "—" : problema.getTitulo()) %></td><td><%
String textoPeso = "—";
if (problema.getPeso() == Intensidade.ALTO) {
    textoPeso = "Alto";
}
else if (problema.getPeso() == Intensidade.MEDIO) {
    textoPeso = "Médio";
}
else if (problema.getPeso() == Intensidade.BAIXO) {
    textoPeso = "Baixo";
}
%>
<%= h(textoPeso) %></td><td><%
String textoStatus = "—";
if (problema.getStatus() == StatusProblema.EM_ANALISE) {
    textoStatus = "Em análise";
}
else if (problema.getStatus() == StatusProblema.EM_RESOLUCAO) {
    textoStatus = "Em resolução";
}
else if (problema.getStatus() == StatusProblema.RESOLVIDO) {
    textoStatus = "Resolvido";
}
%>
<%= h(textoStatus) %></td><td><%= h(problema.getNome_ciclo() == null || problema.getNome_ciclo().isBlank() ? "—" : problema.getNome_ciclo()) %></td><td><%= h(problema.getNome_plano_acao() == null || problema.getNome_plano_acao().isBlank() ? "—" : problema.getNome_plano_acao()) %></td><td><%= h(problema.getNome_colaborador() == null || problema.getNome_colaborador().isBlank() ? "—" : problema.getNome_colaborador()) %></td><td><%= h(problema.getEncontrado_em() == null ? "—" : problema.getEncontrado_em().toLocalDate().format(formatoData)) %></td><td><%= h(problema.getOrigem() == null || problema.getOrigem().isBlank() ? "—" : problema.getOrigem()) %></td><td><%= h(problema.getSolucao() == null || problema.getSolucao().isBlank() ? "—" : problema.getSolucao()) %></td><td class="coluna-acao"><a href="#ver-problema-<%= problema.getId_problema() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-problema-<%= problema.getId_problema() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-problema-<%= problema.getId_problema() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Problema problema : problemas) { %>
<section id="ver-problema-<%= problema.getId_problema() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver modal-larga">
                                <div class="modal-topo">
                                    <h2>Detalhes do problema</h2>
                                    <a href="#problemas" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Título</span><span class="detalhe-valor"><%= h(problema.getTitulo() == null || problema.getTitulo().isBlank() ? "—" : problema.getTitulo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Gravidade</span><span class="detalhe-valor"><%
String textoPeso = "—";
if (problema.getPeso() == Intensidade.ALTO) {
    textoPeso = "Alto";
}
else if (problema.getPeso() == Intensidade.MEDIO) {
    textoPeso = "Médio";
}
else if (problema.getPeso() == Intensidade.BAIXO) {
    textoPeso = "Baixo";
}
%>
<%= h(textoPeso) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (problema.getStatus() == StatusProblema.EM_ANALISE) {
    textoStatus = "Em análise";
}
else if (problema.getStatus() == StatusProblema.EM_RESOLUCAO) {
    textoStatus = "Em resolução";
}
else if (problema.getStatus() == StatusProblema.RESOLVIDO) {
    textoStatus = "Resolvido";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Ciclo</span><span class="detalhe-valor"><%= h(problema.getNome_ciclo() == null || problema.getNome_ciclo().isBlank() ? "—" : problema.getNome_ciclo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Plano de ação</span><span class="detalhe-valor"><%= h(problema.getNome_plano_acao() == null || problema.getNome_plano_acao().isBlank() ? "—" : problema.getNome_plano_acao()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Colaborador</span><span class="detalhe-valor"><%= h(problema.getNome_colaborador() == null || problema.getNome_colaborador().isBlank() ? "—" : problema.getNome_colaborador()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Encontrado em</span><span class="detalhe-valor"><%= h(problema.getEncontrado_em() == null ? "—" : problema.getEncontrado_em().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Origem</span><span class="detalhe-valor"><%= h(problema.getOrigem() == null || problema.getOrigem().isBlank() ? "—" : problema.getOrigem()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Solução</span><span class="detalhe-valor"><%= h(problema.getSolucao() == null || problema.getSolucao().isBlank() ? "—" : problema.getSolucao()) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(problema.getDescricao() == null || problema.getDescricao().isBlank() ? "—" : problema.getDescricao()) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorPesoCadastro = "";
if (recuperarCadastro) {
    valorPesoCadastro = request.getParameter("peso");
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
String valorIdColaboradorCadastro = "";
if (recuperarCadastro) {
    valorIdColaboradorCadastro = request.getParameter("id_colaborador");
}
%>
<section id="cadastrar-problema" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Cadastrar problema</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/problema-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-problema-titulo">Título</label>
                                        <input type="text" id="cad-problema-titulo" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarCadastro ? request.getParameter("titulo") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-peso">Gravidade (peso)</label>
                                        <select id="cad-problema-peso" name="peso" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPesoCadastro) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPesoCadastro) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPesoCadastro) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-status">Status</label>
                                        <select id="cad-problema-status" name="status" required>
                                            <option value="EM_ANALISE" <%= "EM_ANALISE".equals(valorStatusCadastro) ? "selected" : "" %>>Em análise</option>
                                            <option value="EM_RESOLUCAO" <%= "EM_RESOLUCAO".equals(valorStatusCadastro) ? "selected" : "" %>>Em resolução</option>
                                            <option value="RESOLVIDO" <%= "RESOLVIDO".equals(valorStatusCadastro) ? "selected" : "" %>>Resolvido</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-encontrado_em">Encontrado em</label>
                                        <input type="date" id="cad-problema-encontrado_em" name="encontrado_em" required min="1900-01-01" max="<%= java.time.LocalDate.now(ZoneId.of("America/Sao_Paulo")) %>" value="<%= h(recuperarCadastro ? request.getParameter("encontrado_em") : "") %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-problema-descricao">Descrição</label>
                                        <textarea id="cad-problema-descricao" name="descricao" rows="3" required maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao") : "") %></textarea>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-problema-origem">Origem</label>
                                        <textarea id="cad-problema-origem" name="origem" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("origem") : "") %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-solucao">Solução</label>
                                        <input type="text" id="cad-problema-solucao" name="solucao" maxlength="200" placeholder="Digite solução" value="<%= h(recuperarCadastro ? request.getParameter("solucao") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-id_ciclo">Ciclo</label>
                                        <select id="cad-problema-id_ciclo" name="id_ciclo" required>
<option value="">Selecione</option>
<% for (Ciclo opcao : cicloOpcoes) { %>
<option value="<%= opcao.getId_ciclo() %>" <%= String.valueOf(opcao.getId_ciclo()).equals(valorIdCicloCadastro) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-id_plano_acao">Plano de ação</label>
                                        <select id="cad-problema-id_plano_acao" name="id_plano_acao">
<option value="">Sem vínculo</option>
<% for (PlanoAcao opcao : planoAcaoOpcoes) { %>
<option value="<%= opcao.getId_plano_acao() %>" <%= String.valueOf(opcao.getId_plano_acao()).equals(valorIdPlanoAcaoCadastro) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-problema-id_colaborador">Colaborador</label>
                                        <select id="cad-problema-id_colaborador" name="id_colaborador">
<option value="">Sem vínculo</option>
<% for (Colaborador opcao : colaboradorOpcoes) { %>
<option value="<%= opcao.getId_colaborador() %>" <%= String.valueOf(opcao.getId_colaborador()).equals(valorIdColaboradorCadastro) ? "selected" : "" %>><%= h(opcao.getNome() + " " + opcao.getSobrenome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#problemas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Problema problema : problemas) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(problema.getId_problema()).equals(request.getParameter("id-problema"));
String valorPesoEdicao = "";
if (recuperarEdicao) {
    valorPesoEdicao = request.getParameter("peso");
} else if (problema.getPeso() != null) {
    valorPesoEdicao = problema.getPeso().toString();
}
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (problema.getStatus() != null) {
    valorStatusEdicao = problema.getStatus().toString();
}
String valorIdCicloEdicao = "";
if (recuperarEdicao) {
    valorIdCicloEdicao = request.getParameter("id_ciclo");
} else if (problema.getId_ciclo() != null) {
    valorIdCicloEdicao = problema.getId_ciclo().toString();
}
String valorIdPlanoAcaoEdicao = "";
if (recuperarEdicao) {
    valorIdPlanoAcaoEdicao = request.getParameter("id_plano_acao");
} else if (problema.getId_plano_acao() != null) {
    valorIdPlanoAcaoEdicao = problema.getId_plano_acao().toString();
}
String valorIdColaboradorEdicao = "";
if (recuperarEdicao) {
    valorIdColaboradorEdicao = request.getParameter("id_colaborador");
} else if (problema.getId_colaborador() != null) {
    valorIdColaboradorEdicao = problema.getId_colaborador().toString();
}
%>
<section id="editar-problema-<%= problema.getId_problema() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Editar problema</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/problema-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-problema" value="<%= problema.getId_problema() %>">
                                    <div class="campo">
                                        <label for="edi-problema-titulo-<%= problema.getId_problema() %>">Título</label>
                                        <input type="text" id="edi-problema-titulo-<%= problema.getId_problema() %>" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarEdicao ? request.getParameter("titulo") : problema.getTitulo()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-peso-<%= problema.getId_problema() %>">Gravidade (peso)</label>
                                        <select id="edi-problema-peso-<%= problema.getId_problema() %>" name="peso" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPesoEdicao) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPesoEdicao) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPesoEdicao) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-status-<%= problema.getId_problema() %>">Status</label>
                                        <select id="edi-problema-status-<%= problema.getId_problema() %>" name="status" required>
                                            <option value="EM_ANALISE" <%= "EM_ANALISE".equals(valorStatusEdicao) ? "selected" : "" %>>Em análise</option>
                                            <option value="EM_RESOLUCAO" <%= "EM_RESOLUCAO".equals(valorStatusEdicao) ? "selected" : "" %>>Em resolução</option>
                                            <option value="RESOLVIDO" <%= "RESOLVIDO".equals(valorStatusEdicao) ? "selected" : "" %>>Resolvido</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-encontrado_em-<%= problema.getId_problema() %>">Encontrado em</label>
                                        <input type="date" id="edi-problema-encontrado_em-<%= problema.getId_problema() %>" name="encontrado_em" required min="1900-01-01" max="<%= java.time.LocalDate.now(ZoneId.of("America/Sao_Paulo")) %>" value="<%= h(recuperarEdicao ? request.getParameter("encontrado_em") : problema.getEncontrado_em()) %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-problema-descricao-<%= problema.getId_problema() %>">Descrição</label>
                                        <textarea id="edi-problema-descricao-<%= problema.getId_problema() %>" name="descricao" rows="3" required maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao") : problema.getDescricao()) %></textarea>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-problema-origem-<%= problema.getId_problema() %>">Origem</label>
                                        <textarea id="edi-problema-origem-<%= problema.getId_problema() %>" name="origem" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("origem") : problema.getOrigem()) %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-solucao-<%= problema.getId_problema() %>">Solução</label>
                                        <input type="text" id="edi-problema-solucao-<%= problema.getId_problema() %>" name="solucao" maxlength="200" placeholder="Digite solução" value="<%= h(recuperarEdicao ? request.getParameter("solucao") : problema.getSolucao()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-id_ciclo-<%= problema.getId_problema() %>">Ciclo</label>
                                        <select id="edi-problema-id_ciclo-<%= problema.getId_problema() %>" name="id_ciclo" required>
<option value="">Selecione</option>
<% for (Ciclo opcao : cicloOpcoes) { %>
<option value="<%= opcao.getId_ciclo() %>" <%= String.valueOf(opcao.getId_ciclo()).equals(valorIdCicloEdicao) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-id_plano_acao-<%= problema.getId_problema() %>">Plano de ação</label>
                                        <select id="edi-problema-id_plano_acao-<%= problema.getId_problema() %>" name="id_plano_acao">
<option value="">Sem vínculo</option>
<% for (PlanoAcao opcao : planoAcaoOpcoes) { %>
<option value="<%= opcao.getId_plano_acao() %>" <%= String.valueOf(opcao.getId_plano_acao()).equals(valorIdPlanoAcaoEdicao) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-problema-id_colaborador-<%= problema.getId_problema() %>">Colaborador</label>
                                        <select id="edi-problema-id_colaborador-<%= problema.getId_problema() %>" name="id_colaborador">
<option value="">Sem vínculo</option>
<% for (Colaborador opcao : colaboradorOpcoes) { %>
<option value="<%= opcao.getId_colaborador() %>" <%= String.valueOf(opcao.getId_colaborador()).equals(valorIdColaboradorEdicao) ? "selected" : "" %>><%= h(opcao.getNome() + " " + opcao.getSobrenome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#problemas" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Problema problema : problemas) { %>
<section id="excluir-problema-<%= problema.getId_problema() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(problema.getTitulo() == null || problema.getTitulo().isBlank() ? "—" : problema.getTitulo()) %></p>
                                <h2>Excluir problema</h2>
                                <p>Tem certeza que deseja excluir este problema?</p>
                                <form method="post" action="<%= request.getContextPath() %>/problema-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-problema" value="<%= problema.getId_problema() %>">
                                    <div class="botoes-modal">
                                    <a href="#problemas" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

                <datalist id="sugestoes-problemas-origem">
<% for (String sugestao : origemList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<option value="<%= h(sugestao) %>"></option>
<% } %>
<% } %>
</datalist>
            </main>
        </div>
    </body>
</html>
