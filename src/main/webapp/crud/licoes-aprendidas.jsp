<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("licoesAprendidasList") == null) {
    response.sendRedirect(request.getContextPath() + "/licoes-aprendidas-servlet");
    return;
}
List<LicoesAprendidas> licoesAprendidas = (List<LicoesAprendidas>) request.getAttribute("licoesAprendidasList");
List<Ciclo> cicloOpcoes = (List<Ciclo>) request.getAttribute("cicloOpcoes");
if (cicloOpcoes == null) cicloOpcoes = new ArrayList<Ciclo>();
List<String> areaList = (List<String>) request.getAttribute("areaList");
if (areaList == null) areaList = new ArrayList<String>();
List<String> categoriaList = (List<String>) request.getAttribute("categoriaList");
if (categoriaList == null) categoriaList = new ArrayList<String>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroFase = "";
if (consulta && request.getParameter("fase") != null) filtroFase = request.getParameter("fase");
String filtroSeveridade = "";
if (consulta && request.getParameter("severidade") != null) filtroSeveridade = request.getParameter("severidade");
String filtroArea = "";
if (consulta && request.getParameter("area") != null) filtroArea = request.getParameter("area");
String filtroCategoria = "";
if (consulta && request.getParameter("categoria") != null) filtroCategoria = request.getParameter("categoria");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-lição-aprendida";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (LicoesAprendidas licao : licoesAprendidas) {
        if (String.valueOf(licao.getId_licao()).equals(request.getParameter("id-licoesAprendidas"))) {
            modalErro = "editar-lição-aprendida-" + licao.getId_licao();
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
        <title>ACTA — Lições Aprendidas</title>
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
                    <a href="<%= request.getContextPath() %>/licoes-aprendidas-servlet" class="menu-item ativo">
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

                <section id="licoes" class="pagina">
                    <h1>Lições Aprendidas</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-licoes" form="filtros-licoes-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-licoes" aria-expanded="false">Filtros</button>
                            <section id="filtros-licoes" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-licoes-form" method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
                                            <div class="campo">
                                                <label for="filtros-licoes-fase">Fase de origem</label>
                                                <select id="filtros-licoes-fase" name="fase">
                                                    <option value="" <%= "".equals(filtroFase) ? "selected" : "" %>>Todos</option>
                                                    <option value="PLAN" <%= "PLAN".equals(filtroFase) ? "selected" : "" %>>PLAN</option>
                                                    <option value="DO" <%= "DO".equals(filtroFase) ? "selected" : "" %>>DO</option>
                                                    <option value="CHECK" <%= "CHECK".equals(filtroFase) ? "selected" : "" %>>CHECK</option>
                                                    <option value="ACT" <%= "ACT".equals(filtroFase) ? "selected" : "" %>>ACT</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-licoes-severidade">Severidade</label>
                                                <select id="filtros-licoes-severidade" name="severidade">
                                                    <option value="" <%= "".equals(filtroSeveridade) ? "selected" : "" %>>Todos</option>
                                                    <option value="ALTO" <%= "ALTO".equals(filtroSeveridade) ? "selected" : "" %>>Alto</option>
                                                    <option value="MEDIO" <%= "MEDIO".equals(filtroSeveridade) ? "selected" : "" %>>Médio</option>
                                                    <option value="BAIXO" <%= "BAIXO".equals(filtroSeveridade) ? "selected" : "" %>>Baixo</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-licoes-area">Área</label>
                                                <input type="text" id="filtros-licoes-area" name="area" data-lista="sugestoes-licoes-area-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroArea) %>">
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-licoes-categoria">Categoria</label>
                                                <input type="text" id="filtros-licoes-categoria" name="categoria" data-lista="sugestoes-licoes-categoria-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroCategoria) %>">
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/licoes-aprendidas-servlet#licoes" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-lição-aprendida" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "titulo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="titulo">
<input type="hidden" name="direcao" value="<%= "titulo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Título">Título
<% if ("titulo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "categoria".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="categoria">
<input type="hidden" name="direcao" value="<%= "categoria".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Categoria">Categoria
<% if ("categoria".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "area".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="area">
<input type="hidden" name="direcao" value="<%= "area".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Área">Área
<% if ("area".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "ciclo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="ciclo">
<input type="hidden" name="direcao" value="<%= "ciclo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Ciclo">Ciclo
<% if ("ciclo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "fase".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="fase">
<input type="hidden" name="direcao" value="<%= "fase".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Fase de origem">Fase de origem
<% if ("fase".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "severidade".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="fase" value="<%= h(filtroFase) %>">
<input type="hidden" name="severidade" value="<%= h(filtroSeveridade) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="categoria" value="<%= h(filtroCategoria) %>">
<input type="hidden" name="ordem" value="severidade">
<input type="hidden" name="direcao" value="<%= "severidade".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Severidade">Severidade
<% if ("severidade".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (licoesAprendidas.isEmpty()) { %>
<tr><td colspan="10">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (LicoesAprendidas licao : licoesAprendidas) { %>
<tr><td><%= h(licao.getId_licao()) %></td><td><%= h(licao.getTitulo() == null || licao.getTitulo().isBlank() ? "—" : licao.getTitulo()) %></td><td><%= h(licao.getCategoria() == null || licao.getCategoria().isBlank() ? "—" : licao.getCategoria()) %></td><td><%= h(licao.getArea() == null || licao.getArea().isBlank() ? "—" : licao.getArea()) %></td><td><%= h(licao.getNome_ciclo() == null || licao.getNome_ciclo().isBlank() ? "—" : licao.getNome_ciclo()) %></td><td><%
String textoFaseOrigem = "—";
if (licao.getFase_origem() == EtapaCiclo.PLAN) {
    textoFaseOrigem = "PLAN";
}
else if (licao.getFase_origem() == EtapaCiclo.DO) {
    textoFaseOrigem = "DO";
}
else if (licao.getFase_origem() == EtapaCiclo.CHECK) {
    textoFaseOrigem = "CHECK";
}
else if (licao.getFase_origem() == EtapaCiclo.ACT) {
    textoFaseOrigem = "ACT";
}
%>
<%= h(textoFaseOrigem) %></td><td><%
String textoSeveridade = "—";
if (licao.getSeveridade() == Intensidade.ALTO) {
    textoSeveridade = "Alto";
}
else if (licao.getSeveridade() == Intensidade.MEDIO) {
    textoSeveridade = "Médio";
}
else if (licao.getSeveridade() == Intensidade.BAIXO) {
    textoSeveridade = "Baixo";
}
%>
<%= h(textoSeveridade) %></td><td class="coluna-acao"><a href="#ver-lição-aprendida-<%= licao.getId_licao() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-lição-aprendida-<%= licao.getId_licao() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-lição-aprendida-<%= licao.getId_licao() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (LicoesAprendidas licao : licoesAprendidas) { %>
<section id="ver-lição-aprendida-<%= licao.getId_licao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes da lição aprendida</h2>
                                    <a href="#licoes" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Título</span><span class="detalhe-valor"><%= h(licao.getTitulo() == null || licao.getTitulo().isBlank() ? "—" : licao.getTitulo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Categoria</span><span class="detalhe-valor"><%= h(licao.getCategoria() == null || licao.getCategoria().isBlank() ? "—" : licao.getCategoria()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Área</span><span class="detalhe-valor"><%= h(licao.getArea() == null || licao.getArea().isBlank() ? "—" : licao.getArea()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Ciclo</span><span class="detalhe-valor"><%= h(licao.getNome_ciclo() == null || licao.getNome_ciclo().isBlank() ? "—" : licao.getNome_ciclo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Fase de origem</span><span class="detalhe-valor"><%
String textoFaseOrigem = "—";
if (licao.getFase_origem() == EtapaCiclo.PLAN) {
    textoFaseOrigem = "PLAN";
}
else if (licao.getFase_origem() == EtapaCiclo.DO) {
    textoFaseOrigem = "DO";
}
else if (licao.getFase_origem() == EtapaCiclo.CHECK) {
    textoFaseOrigem = "CHECK";
}
else if (licao.getFase_origem() == EtapaCiclo.ACT) {
    textoFaseOrigem = "ACT";
}
%>
<%= h(textoFaseOrigem) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Severidade</span><span class="detalhe-valor"><%
String textoSeveridade = "—";
if (licao.getSeveridade() == Intensidade.ALTO) {
    textoSeveridade = "Alto";
}
else if (licao.getSeveridade() == Intensidade.MEDIO) {
    textoSeveridade = "Médio";
}
else if (licao.getSeveridade() == Intensidade.BAIXO) {
    textoSeveridade = "Baixo";
}
%>
<%= h(textoSeveridade) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Aprendizado</span><span class="detalhe-valor"><%= h(licao.getAprendizado() == null || licao.getAprendizado().isBlank() ? "—" : licao.getAprendizado()) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(licao.getDescricao() == null || licao.getDescricao().isBlank() ? "—" : licao.getDescricao()) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorIdCicloCadastro = "";
if (recuperarCadastro) {
    valorIdCicloCadastro = request.getParameter("id_ciclo");
}
String valorFaseOrigemCadastro = "";
if (recuperarCadastro) {
    valorFaseOrigemCadastro = request.getParameter("fase_origem");
}
String valorSeveridadeCadastro = "";
if (recuperarCadastro) {
    valorSeveridadeCadastro = request.getParameter("severidade");
}
%>
<section id="cadastrar-lição-aprendida" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Cadastrar lição aprendida</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-titulo">Título</label>
                                        <input type="text" id="cad-lição-aprendida-titulo" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarCadastro ? request.getParameter("titulo") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-id_ciclo">Ciclo</label>
                                        <input type="text" id="cad-lição-aprendida-id_ciclo" data-lista="sugestoes-fk-ciclo-lista" data-validar-lista data-campo-valor="cad-lição-aprendida-id_ciclo-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-lição-aprendida-id_ciclo-valor" name="id_ciclo" value="<%= h(valorIdCicloCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-area">Área</label>
                                        <input type="text" id="cad-lição-aprendida-area" name="area" maxlength="30" placeholder="Digite área" data-lista="sugestoes-licoes-area-lista" value="<%= h(recuperarCadastro ? request.getParameter("area") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-categoria">Categoria</label>
                                        <input type="text" id="cad-lição-aprendida-categoria" name="categoria" maxlength="30" placeholder="Digite categoria" data-lista="sugestoes-licoes-categoria-lista" value="<%= h(recuperarCadastro ? request.getParameter("categoria") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-fase_origem">Fase de origem</label>
                                        <select id="cad-lição-aprendida-fase_origem" name="fase_origem" required>
                                            <option value="PLAN" <%= "PLAN".equals(valorFaseOrigemCadastro) ? "selected" : "" %>>PLAN</option>
                                            <option value="DO" <%= "DO".equals(valorFaseOrigemCadastro) ? "selected" : "" %>>DO</option>
                                            <option value="CHECK" <%= "CHECK".equals(valorFaseOrigemCadastro) ? "selected" : "" %>>CHECK</option>
                                            <option value="ACT" <%= "ACT".equals(valorFaseOrigemCadastro) ? "selected" : "" %>>ACT</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-lição-aprendida-severidade">Severidade</label>
                                        <select id="cad-lição-aprendida-severidade" name="severidade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorSeveridadeCadastro) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorSeveridadeCadastro) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorSeveridadeCadastro) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-lição-aprendida-aprendizado">Aprendizado</label>
                                        <textarea id="cad-lição-aprendida-aprendizado" name="aprendizado" rows="3" required maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("aprendizado") : "") %></textarea>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-lição-aprendida-descricao">Descrição</label>
                                        <textarea id="cad-lição-aprendida-descricao" name="descricao" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao") : "") %></textarea>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#licoes" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (LicoesAprendidas licao : licoesAprendidas) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(licao.getId_licao()).equals(request.getParameter("id-licoesAprendidas"));
String valorIdCicloEdicao = "";
if (recuperarEdicao) {
    valorIdCicloEdicao = request.getParameter("id_ciclo");
} else if (licao.getId_ciclo() != null) {
    valorIdCicloEdicao = licao.getId_ciclo().toString();
}
String valorFaseOrigemEdicao = "";
if (recuperarEdicao) {
    valorFaseOrigemEdicao = request.getParameter("fase_origem");
} else if (licao.getFase_origem() != null) {
    valorFaseOrigemEdicao = licao.getFase_origem().toString();
}
String valorSeveridadeEdicao = "";
if (recuperarEdicao) {
    valorSeveridadeEdicao = request.getParameter("severidade");
} else if (licao.getSeveridade() != null) {
    valorSeveridadeEdicao = licao.getSeveridade().toString();
}
%>
<section id="editar-lição-aprendida-<%= licao.getId_licao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Editar lição aprendida</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-licoesAprendidas" value="<%= licao.getId_licao() %>">
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-titulo-<%= licao.getId_licao() %>">Título</label>
                                        <input type="text" id="edi-lição-aprendida-titulo-<%= licao.getId_licao() %>" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarEdicao ? request.getParameter("titulo") : licao.getTitulo()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-id_ciclo-<%= licao.getId_licao() %>">Ciclo</label>
                                        <input type="text" id="edi-lição-aprendida-id_ciclo-<%= licao.getId_licao() %>" data-lista="sugestoes-fk-ciclo-lista" data-validar-lista data-campo-valor="edi-lição-aprendida-id_ciclo-<%= licao.getId_licao() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-lição-aprendida-id_ciclo-<%= licao.getId_licao() %>-valor" name="id_ciclo" value="<%= h(valorIdCicloEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-area-<%= licao.getId_licao() %>">Área</label>
                                        <input type="text" id="edi-lição-aprendida-area-<%= licao.getId_licao() %>" name="area" maxlength="30" placeholder="Digite área" data-lista="sugestoes-licoes-area-lista" value="<%= h(recuperarEdicao ? request.getParameter("area") : licao.getArea()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-categoria-<%= licao.getId_licao() %>">Categoria</label>
                                        <input type="text" id="edi-lição-aprendida-categoria-<%= licao.getId_licao() %>" name="categoria" maxlength="30" placeholder="Digite categoria" data-lista="sugestoes-licoes-categoria-lista" value="<%= h(recuperarEdicao ? request.getParameter("categoria") : licao.getCategoria()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-fase_origem-<%= licao.getId_licao() %>">Fase de origem</label>
                                        <select id="edi-lição-aprendida-fase_origem-<%= licao.getId_licao() %>" name="fase_origem" required>
                                            <option value="PLAN" <%= "PLAN".equals(valorFaseOrigemEdicao) ? "selected" : "" %>>PLAN</option>
                                            <option value="DO" <%= "DO".equals(valorFaseOrigemEdicao) ? "selected" : "" %>>DO</option>
                                            <option value="CHECK" <%= "CHECK".equals(valorFaseOrigemEdicao) ? "selected" : "" %>>CHECK</option>
                                            <option value="ACT" <%= "ACT".equals(valorFaseOrigemEdicao) ? "selected" : "" %>>ACT</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-lição-aprendida-severidade-<%= licao.getId_licao() %>">Severidade</label>
                                        <select id="edi-lição-aprendida-severidade-<%= licao.getId_licao() %>" name="severidade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorSeveridadeEdicao) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorSeveridadeEdicao) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorSeveridadeEdicao) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-lição-aprendida-aprendizado-<%= licao.getId_licao() %>">Aprendizado</label>
                                        <textarea id="edi-lição-aprendida-aprendizado-<%= licao.getId_licao() %>" name="aprendizado" rows="3" required maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("aprendizado") : licao.getAprendizado()) %></textarea>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-lição-aprendida-descricao-<%= licao.getId_licao() %>">Descrição</label>
                                        <textarea id="edi-lição-aprendida-descricao-<%= licao.getId_licao() %>" name="descricao" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao") : licao.getDescricao()) %></textarea>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#licoes" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (LicoesAprendidas licao : licoesAprendidas) { %>
<section id="excluir-lição-aprendida-<%= licao.getId_licao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(licao.getTitulo() == null || licao.getTitulo().isBlank() ? "—" : licao.getTitulo()) %></p>
                                <h2>Excluir lição aprendida</h2>
                                <p>Tem certeza que deseja excluir esta lição aprendida?</p>
                                <form method="post" action="<%= request.getContextPath() %>/licoes-aprendidas-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-licoesAprendidas" value="<%= licao.getId_licao() %>">
                                    <div class="botoes-modal">
                                    <a href="#licoes" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>


<div id="sugestoes-licoes-area-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : areaList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<button type="button" role="option" data-valor="<%= h(sugestao) %>"><%= h(sugestao) %></button>
<% } %>
<% } %>
<button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>

<div id="sugestoes-licoes-categoria-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : categoriaList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<button type="button" role="option" data-valor="<%= h(sugestao) %>"><%= h(sugestao) %></button>
<% } %>
<% } %>
<button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
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
            </main>
        </div>
    </body>
</html>
