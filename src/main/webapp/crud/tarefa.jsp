<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("tarefaList") == null) {
    response.sendRedirect(request.getContextPath() + "/tarefa-servlet");
    return;
}
List<Tarefa> tarefas = (List<Tarefa>) request.getAttribute("tarefaList");
List<Colaborador> colaboradorOpcoes = (List<Colaborador>) request.getAttribute("colaboradorOpcoes");
if (colaboradorOpcoes == null) colaboradorOpcoes = new ArrayList<Colaborador>();
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
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-tarefa";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Tarefa tarefa : tarefas) {
        if (String.valueOf(tarefa.getId_tarefa()).equals(request.getParameter("id-tarefa"))) {
            modalErro = "editar-tarefa-" + tarefa.getId_tarefa();
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
        <title>ACTA — Tarefa</title>
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
                    <a href="<%= request.getContextPath() %>/tarefa-servlet" class="menu-item ativo">
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

                <section id="tarefa" class="pagina">
                    <h1>Tarefa</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-tarefa" form="filtros-tarefa-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-tarefa" aria-expanded="false">Filtros</button>
                            <section id="filtros-tarefa" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-tarefa-form" method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
                                            <div class="campo">
                                                <label for="filtros-tarefa-prioridade">Prioridade</label>
                                                <select id="filtros-tarefa-prioridade" name="prioridade">
                                                    <option value="" <%= "".equals(filtroPrioridade) ? "selected" : "" %>>Todos</option>
                                                    <option value="ALTO" <%= "ALTO".equals(filtroPrioridade) ? "selected" : "" %>>Alto</option>
                                                    <option value="MEDIO" <%= "MEDIO".equals(filtroPrioridade) ? "selected" : "" %>>Médio</option>
                                                    <option value="BAIXO" <%= "BAIXO".equals(filtroPrioridade) ? "selected" : "" %>>Baixo</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-tarefa-status">Status</label>
                                                <select id="filtros-tarefa-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(filtroStatus) ? "selected" : "" %>>Não iniciado</option>
                                                    <option value="INICIADO" <%= "INICIADO".equals(filtroStatus) ? "selected" : "" %>>Iniciado</option>
                                                    <option value="FINALIZADO" <%= "FINALIZADO".equals(filtroStatus) ? "selected" : "" %>>Finalizado</option>
                                                </select>
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Entrega</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-tarefa-dataInicial">De</label><input type="date" id="filtros-tarefa-dataInicial" name="dataInicial" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataInicial) %>"></div>
                                                    <div class="campo"><label for="filtros-tarefa-dataFinal">Até</label><input type="date" id="filtros-tarefa-dataFinal" name="dataFinal" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataFinal) %>"></div>
                                                </div>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/tarefa-servlet#tarefa" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-tarefa" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
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
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="titulo">
<input type="hidden" name="direcao" value="<%= "titulo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Título">Título
<% if ("titulo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "colaborador".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="colaborador">
<input type="hidden" name="direcao" value="<%= "colaborador".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Colaborador">Colaborador
<% if ("colaborador".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "plano_acao".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="plano_acao">
<input type="hidden" name="direcao" value="<%= "plano_acao".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Plano de ação">Plano de ação
<% if ("plano_acao".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "prioridade".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="prioridade">
<input type="hidden" name="direcao" value="<%= "prioridade".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Prioridade">Prioridade
<% if ("prioridade".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "inicio".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="inicio">
<input type="hidden" name="direcao" value="<%= "inicio".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Data de início">Data de início
<% if ("inicio".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "entrega".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/tarefa-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="entrega">
<input type="hidden" name="direcao" value="<%= "entrega".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Data de entrega">Data de entrega
<% if ("entrega".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (tarefas.isEmpty()) { %>
<tr><td colspan="11">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Tarefa tarefa : tarefas) { %>
<tr><td><%= h(tarefa.getId_tarefa()) %></td><td><%= h(tarefa.getTitulo() == null || tarefa.getTitulo().isBlank() ? "—" : tarefa.getTitulo()) %></td><td><%= h(tarefa.getNome_colaborador() == null || tarefa.getNome_colaborador().isBlank() ? "—" : tarefa.getNome_colaborador()) %></td><td><%= h(tarefa.getNome_plano_acao() == null || tarefa.getNome_plano_acao().isBlank() ? "—" : tarefa.getNome_plano_acao()) %></td><td><%
String textoPrioridade = "—";
if (tarefa.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (tarefa.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (tarefa.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></td><td><%
String textoStatus = "—";
if (tarefa.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (tarefa.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (tarefa.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></td><td><%= h(tarefa.getDt_inicio() == null ? "—" : tarefa.getDt_inicio().toLocalDate().format(formatoData)) %></td><td><%= h(tarefa.getDt_entrega() == null ? "—" : tarefa.getDt_entrega().toLocalDate().format(formatoData)) %></td><td class="coluna-acao"><a href="#ver-tarefa-<%= tarefa.getId_tarefa() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-tarefa-<%= tarefa.getId_tarefa() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-tarefa-<%= tarefa.getId_tarefa() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Tarefa tarefa : tarefas) { %>
<section id="ver-tarefa-<%= tarefa.getId_tarefa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes da tarefa</h2>
                                    <a href="#tarefa" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Título</span><span class="detalhe-valor"><%= h(tarefa.getTitulo() == null || tarefa.getTitulo().isBlank() ? "—" : tarefa.getTitulo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Colaborador</span><span class="detalhe-valor"><%= h(tarefa.getNome_colaborador() == null || tarefa.getNome_colaborador().isBlank() ? "—" : tarefa.getNome_colaborador()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Plano de ação</span><span class="detalhe-valor"><%= h(tarefa.getNome_plano_acao() == null || tarefa.getNome_plano_acao().isBlank() ? "—" : tarefa.getNome_plano_acao()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Prioridade</span><span class="detalhe-valor"><%
String textoPrioridade = "—";
if (tarefa.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (tarefa.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (tarefa.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (tarefa.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (tarefa.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (tarefa.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Data de início</span><span class="detalhe-valor"><%= h(tarefa.getDt_inicio() == null ? "—" : tarefa.getDt_inicio().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Data de entrega</span><span class="detalhe-valor"><%= h(tarefa.getDt_entrega() == null ? "—" : tarefa.getDt_entrega().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(tarefa.getDescricao() == null || tarefa.getDescricao().isBlank() ? "—" : tarefa.getDescricao()) %></span></div>
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
String valorIdColaboradorCadastro = "";
if (recuperarCadastro) {
    valorIdColaboradorCadastro = request.getParameter("id_colaborador");
}
String valorIdPlanoAcaoCadastro = "";
if (recuperarCadastro) {
    valorIdPlanoAcaoCadastro = request.getParameter("id_plano_acao");
}
%>
<section id="cadastrar-tarefa" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Cadastrar tarefa</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/tarefa-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-tarefa-titulo">Título</label>
                                        <input type="text" id="cad-tarefa-titulo" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarCadastro ? request.getParameter("titulo") : "") %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-tarefa-descricao">Descrição</label>
                                        <textarea id="cad-tarefa-descricao" name="descricao" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao") : "") %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-prioridade">Prioridade</label>
                                        <select id="cad-tarefa-prioridade" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-status">Status</label>
                                        <select id="cad-tarefa-status" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusCadastro) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-dt_inicio">Data de início</label>
                                        <input type="date" id="cad-tarefa-dt_inicio" name="dt_inicio" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarCadastro ? request.getParameter("dt_inicio") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-dt_entrega">Data de entrega</label>
                                        <input type="date" id="cad-tarefa-dt_entrega" name="dt_entrega" min="1900-01-01" max="2100-12-31" value="<%= h(recuperarCadastro ? request.getParameter("dt_entrega") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-id_colaborador">Responsável</label>
                                        <input type="text" id="cad-tarefa-id_colaborador" data-lista="sugestoes-fk-colaborador-lista" data-validar-lista data-campo-valor="cad-tarefa-id_colaborador-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-tarefa-id_colaborador-valor" name="id_colaborador" value="<%= h(valorIdColaboradorCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-tarefa-id_plano_acao">Plano de ação</label>
                                        <input type="text" id="cad-tarefa-id_plano_acao" data-lista="sugestoes-fk-planoacao-lista" data-validar-lista data-campo-valor="cad-tarefa-id_plano_acao-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-tarefa-id_plano_acao-valor" name="id_plano_acao" value="<%= h(valorIdPlanoAcaoCadastro) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#tarefa" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Tarefa tarefa : tarefas) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(tarefa.getId_tarefa()).equals(request.getParameter("id-tarefa"));
String valorPrioridadeEdicao = "";
if (recuperarEdicao) {
    valorPrioridadeEdicao = request.getParameter("prioridade");
} else if (tarefa.getPrioridade() != null) {
    valorPrioridadeEdicao = tarefa.getPrioridade().toString();
}
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (tarefa.getStatus() != null) {
    valorStatusEdicao = tarefa.getStatus().toString();
}
String valorIdColaboradorEdicao = "";
if (recuperarEdicao) {
    valorIdColaboradorEdicao = request.getParameter("id_colaborador");
} else if (tarefa.getId_colaborador() != null) {
    valorIdColaboradorEdicao = tarefa.getId_colaborador().toString();
}
String valorIdPlanoAcaoEdicao = "";
if (recuperarEdicao) {
    valorIdPlanoAcaoEdicao = request.getParameter("id_plano_acao");
} else if (tarefa.getId_plano_acao() != null) {
    valorIdPlanoAcaoEdicao = tarefa.getId_plano_acao().toString();
}
%>
<section id="editar-tarefa-<%= tarefa.getId_tarefa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Editar tarefa</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/tarefa-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-tarefa" value="<%= tarefa.getId_tarefa() %>">
                                    <div class="campo">
                                        <label for="edi-tarefa-titulo-<%= tarefa.getId_tarefa() %>">Título</label>
                                        <input type="text" id="edi-tarefa-titulo-<%= tarefa.getId_tarefa() %>" name="titulo" maxlength="100" placeholder="Digite título" required value="<%= h(recuperarEdicao ? request.getParameter("titulo") : tarefa.getTitulo()) %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-tarefa-descricao-<%= tarefa.getId_tarefa() %>">Descrição</label>
                                        <textarea id="edi-tarefa-descricao-<%= tarefa.getId_tarefa() %>" name="descricao" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao") : tarefa.getDescricao()) %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-prioridade-<%= tarefa.getId_tarefa() %>">Prioridade</label>
                                        <select id="edi-tarefa-prioridade-<%= tarefa.getId_tarefa() %>" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-status-<%= tarefa.getId_tarefa() %>">Status</label>
                                        <select id="edi-tarefa-status-<%= tarefa.getId_tarefa() %>" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusEdicao) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-dt_inicio-<%= tarefa.getId_tarefa() %>">Data de início</label>
                                        <input type="date" id="edi-tarefa-dt_inicio-<%= tarefa.getId_tarefa() %>" name="dt_inicio" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarEdicao ? request.getParameter("dt_inicio") : tarefa.getDt_inicio()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-dt_entrega-<%= tarefa.getId_tarefa() %>">Data de entrega</label>
                                        <input type="date" id="edi-tarefa-dt_entrega-<%= tarefa.getId_tarefa() %>" name="dt_entrega" min="1900-01-01" max="2100-12-31" value="<%= h(recuperarEdicao ? request.getParameter("dt_entrega") : tarefa.getDt_entrega()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-id_colaborador-<%= tarefa.getId_tarefa() %>">Responsável</label>
                                        <input type="text" id="edi-tarefa-id_colaborador-<%= tarefa.getId_tarefa() %>" data-lista="sugestoes-fk-colaborador-lista" data-validar-lista data-campo-valor="edi-tarefa-id_colaborador-<%= tarefa.getId_tarefa() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-tarefa-id_colaborador-<%= tarefa.getId_tarefa() %>-valor" name="id_colaborador" value="<%= h(valorIdColaboradorEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-tarefa-id_plano_acao-<%= tarefa.getId_tarefa() %>">Plano de ação</label>
                                        <input type="text" id="edi-tarefa-id_plano_acao-<%= tarefa.getId_tarefa() %>" data-lista="sugestoes-fk-planoacao-lista" data-validar-lista data-campo-valor="edi-tarefa-id_plano_acao-<%= tarefa.getId_tarefa() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-tarefa-id_plano_acao-<%= tarefa.getId_tarefa() %>-valor" name="id_plano_acao" value="<%= h(valorIdPlanoAcaoEdicao) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#tarefa" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Tarefa tarefa : tarefas) { %>
<section id="excluir-tarefa-<%= tarefa.getId_tarefa() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(tarefa.getTitulo() == null || tarefa.getTitulo().isBlank() ? "—" : tarefa.getTitulo()) %></p>
                                <h2>Excluir tarefa</h2>
                                <p>Tem certeza que deseja excluir esta tarefa?</p>
                                <form method="post" action="<%= request.getContextPath() %>/tarefa-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-tarefa" value="<%= tarefa.getId_tarefa() %>">
                                    <div class="botoes-modal">
                                    <a href="#tarefa" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

                <div id="sugestoes-fk-colaborador-lista" class="lista-escolhas" role="listbox" hidden>
<% for (Colaborador opcao : colaboradorOpcoes) {
    String nomeOpcao = (opcao.getNome() == null ? "" : opcao.getNome()) + " " + (opcao.getSobrenome() == null ? "" : opcao.getSobrenome());
    if (nomeOpcao != null && !nomeOpcao.isBlank()) {
        nomeOpcao = nomeOpcao.trim();
        int nomesIguais = 0;
        for (Colaborador outra : colaboradorOpcoes) {
            String outroNome = (outra.getNome() == null ? "" : outra.getNome()) + " " + (outra.getSobrenome() == null ? "" : outra.getSobrenome());
            if (outroNome != null && nomeOpcao.equals(outroNome.trim())) nomesIguais++;
        }
        if (nomesIguais > 1) nomeOpcao += " (#" + opcao.getId_colaborador() + ")"; %>
<button type="button" role="option" data-valor="<%= opcao.getId_colaborador() %>"><%= h(nomeOpcao) %></button>
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
