<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("cicloList") == null) {
    response.sendRedirect(request.getContextPath() + "/ciclo-servlet");
    return;
}
List<Ciclo> ciclos = (List<Ciclo>) request.getAttribute("cicloList");
List<Empresa> empresaOpcoes = (List<Empresa>) request.getAttribute("empresaOpcoes");
if (empresaOpcoes == null) empresaOpcoes = new ArrayList<Empresa>();
List<Colaborador> colaboradorOpcoes = (List<Colaborador>) request.getAttribute("colaboradorOpcoes");
if (colaboradorOpcoes == null) colaboradorOpcoes = new ArrayList<Colaborador>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroEtapa = "";
if (consulta && request.getParameter("etapa") != null) filtroEtapa = request.getParameter("etapa");
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
if (recuperarCadastro) modalErro = "cadastrar-ciclo";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Ciclo ciclo : ciclos) {
        if (String.valueOf(ciclo.getId_ciclo()).equals(request.getParameter("id-ciclo"))) {
            modalErro = "editar-ciclo-" + ciclo.getId_ciclo();
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
        <title>ACTA — Ciclos</title>
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
                    <a href="<%= request.getContextPath() %>/ciclo-servlet" class="menu-item ativo">
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

                <section id="ciclos" class="pagina">
                    <h1>Ciclos</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-ciclos" form="filtros-ciclos-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-ciclos" aria-expanded="false">Filtros</button>
                            <section id="filtros-ciclos" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-ciclos-form" method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
                                            <div class="campo">
                                                <label for="filtros-ciclos-etapa">Etapa atual</label>
                                                <select id="filtros-ciclos-etapa" name="etapa">
                                                    <option value="" <%= "".equals(filtroEtapa) ? "selected" : "" %>>Todos</option>
                                                    <option value="PLAN" <%= "PLAN".equals(filtroEtapa) ? "selected" : "" %>>PLAN</option>
                                                    <option value="DO" <%= "DO".equals(filtroEtapa) ? "selected" : "" %>>DO</option>
                                                    <option value="CHECK" <%= "CHECK".equals(filtroEtapa) ? "selected" : "" %>>CHECK</option>
                                                    <option value="ACT" <%= "ACT".equals(filtroEtapa) ? "selected" : "" %>>ACT</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-ciclos-status">Status</label>
                                                <select id="filtros-ciclos-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(filtroStatus) ? "selected" : "" %>>Não iniciado</option>
                                                    <option value="INICIADO" <%= "INICIADO".equals(filtroStatus) ? "selected" : "" %>>Iniciado</option>
                                                    <option value="FINALIZADO" <%= "FINALIZADO".equals(filtroStatus) ? "selected" : "" %>>Finalizado</option>
                                                </select>
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Início do ciclo</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-ciclos-dataInicial">De</label><input type="date" id="filtros-ciclos-dataInicial" name="dataInicial" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataInicial) %>"></div>
                                                    <div class="campo"><label for="filtros-ciclos-dataFinal">Até</label><input type="date" id="filtros-ciclos-dataFinal" name="dataFinal" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataFinal) %>"></div>
                                                </div>
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Criado em</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-ciclos-criadoDe">De</label><input type="date" id="filtros-ciclos-criadoDe" name="criadoDe" min="1900-01-01" max="2100-12-31" value="<%= h(filtroCriadoDe) %>"></div>
                                                    <div class="campo"><label for="filtros-ciclos-criadoAte">Até</label><input type="date" id="filtros-ciclos-criadoAte" name="criadoAte" min="1900-01-01" max="2100-12-31" value="<%= h(filtroCriadoAte) %>"></div>
                                                </div>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/ciclo-servlet#ciclos" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-ciclo" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
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
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "nome".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="nome">
<input type="hidden" name="direcao" value="<%= "nome".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Nome">Nome
<% if ("nome".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "empresa".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="empresa">
<input type="hidden" name="direcao" value="<%= "empresa".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Empresa">Empresa
<% if ("empresa".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "responsavel".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="responsavel">
<input type="hidden" name="direcao" value="<%= "responsavel".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Responsável">Responsável
<% if ("responsavel".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "etapa".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="etapa">
<input type="hidden" name="direcao" value="<%= "etapa".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Etapa atual">Etapa atual
<% if ("etapa".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
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
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "inicio".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="inicio">
<input type="hidden" name="direcao" value="<%= "inicio".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Data de início">Data de início
<% if ("inicio".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "fim".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="criadoDe" value="<%= h(filtroCriadoDe) %>">
<input type="hidden" name="criadoAte" value="<%= h(filtroCriadoAte) %>">
<input type="hidden" name="ordem" value="fim">
<input type="hidden" name="direcao" value="<%= "fim".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Data de fim">Data de fim
<% if ("fim".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "criado_em".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/ciclo-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="etapa" value="<%= h(filtroEtapa) %>">
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
<% if (ciclos.isEmpty()) { %>
<tr><td colspan="12">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Ciclo ciclo : ciclos) { %>
<tr><td><%= h(ciclo.getId_ciclo()) %></td><td><%= h(ciclo.getNome() == null || ciclo.getNome().isBlank() ? "—" : ciclo.getNome()) %></td><td><%= h(ciclo.getNome_empresa() == null || ciclo.getNome_empresa().isBlank() ? "—" : ciclo.getNome_empresa()) %></td><td><%= h(ciclo.getNome_responsavel() == null || ciclo.getNome_responsavel().isBlank() ? "—" : ciclo.getNome_responsavel()) %></td><td><%
String textoEtapaAtual = "—";
if (ciclo.getEtapa_atual() == EtapaCiclo.PLAN) {
    textoEtapaAtual = "PLAN";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.DO) {
    textoEtapaAtual = "DO";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.CHECK) {
    textoEtapaAtual = "CHECK";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.ACT) {
    textoEtapaAtual = "ACT";
}
%>
<%= h(textoEtapaAtual) %></td><td><%
String textoStatus = "—";
if (ciclo.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (ciclo.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (ciclo.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></td><td><%= h(ciclo.getDt_inicio() == null ? "—" : ciclo.getDt_inicio().toLocalDate().format(formatoData)) %></td><td><%= h(ciclo.getDt_fim() == null ? "—" : ciclo.getDt_fim().toLocalDate().format(formatoData)) %></td><td><%= h(ciclo.getCriado_em() == null ? "—" : ciclo.getCriado_em().atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).format(formatoDataHora)) %></td><td class="coluna-acao"><a href="#ver-ciclo-<%= ciclo.getId_ciclo() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-ciclo-<%= ciclo.getId_ciclo() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-ciclo-<%= ciclo.getId_ciclo() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Ciclo ciclo : ciclos) { %>
<section id="ver-ciclo-<%= ciclo.getId_ciclo() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes do ciclo</h2>
                                    <a href="#ciclos" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Nome</span><span class="detalhe-valor"><%= h(ciclo.getNome() == null || ciclo.getNome().isBlank() ? "—" : ciclo.getNome()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Empresa</span><span class="detalhe-valor"><%= h(ciclo.getNome_empresa() == null || ciclo.getNome_empresa().isBlank() ? "—" : ciclo.getNome_empresa()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Responsável</span><span class="detalhe-valor"><%= h(ciclo.getNome_responsavel() == null || ciclo.getNome_responsavel().isBlank() ? "—" : ciclo.getNome_responsavel()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Etapa atual</span><span class="detalhe-valor"><%
String textoEtapaAtual = "—";
if (ciclo.getEtapa_atual() == EtapaCiclo.PLAN) {
    textoEtapaAtual = "PLAN";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.DO) {
    textoEtapaAtual = "DO";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.CHECK) {
    textoEtapaAtual = "CHECK";
}
else if (ciclo.getEtapa_atual() == EtapaCiclo.ACT) {
    textoEtapaAtual = "ACT";
}
%>
<%= h(textoEtapaAtual) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (ciclo.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (ciclo.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (ciclo.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Data de início</span><span class="detalhe-valor"><%= h(ciclo.getDt_inicio() == null ? "—" : ciclo.getDt_inicio().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Data de fim</span><span class="detalhe-valor"><%= h(ciclo.getDt_fim() == null ? "—" : ciclo.getDt_fim().toLocalDate().format(formatoData)) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Criado em</span><span class="detalhe-valor"><%= h(ciclo.getCriado_em() == null ? "—" : ciclo.getCriado_em().atZoneSameInstant(ZoneId.of("America/Sao_Paulo")).format(formatoDataHora)) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(ciclo.getDescricao() == null || ciclo.getDescricao().isBlank() ? "—" : ciclo.getDescricao()) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorEtapaAtualCadastro = "";
if (recuperarCadastro) {
    valorEtapaAtualCadastro = request.getParameter("etapa_atual");
}
String valorStatusCadastro = "";
if (recuperarCadastro) {
    valorStatusCadastro = request.getParameter("status");
}
String valorIdEmpresaCadastro = "";
if (recuperarCadastro) {
    valorIdEmpresaCadastro = request.getParameter("id_empresa");
}
String valorIdResponsavelCadastro = "";
if (recuperarCadastro) {
    valorIdResponsavelCadastro = request.getParameter("id_responsavel");
}
%>
<section id="cadastrar-ciclo" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Cadastrar ciclo</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/ciclo-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-ciclo-nome">Nome</label>
                                        <input type="text" id="cad-ciclo-nome" name="nome" maxlength="100" placeholder="Digite nome" required value="<%= h(recuperarCadastro ? request.getParameter("nome") : "") %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-ciclo-descricao">Descrição</label>
                                        <textarea id="cad-ciclo-descricao" name="descricao" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao") : "") %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-etapa_atual">Etapa atual</label>
                                        <select id="cad-ciclo-etapa_atual" name="etapa_atual" required>
                                            <option value="PLAN" <%= "PLAN".equals(valorEtapaAtualCadastro) ? "selected" : "" %>>PLAN</option>
                                            <option value="DO" <%= "DO".equals(valorEtapaAtualCadastro) ? "selected" : "" %>>DO</option>
                                            <option value="CHECK" <%= "CHECK".equals(valorEtapaAtualCadastro) ? "selected" : "" %>>CHECK</option>
                                            <option value="ACT" <%= "ACT".equals(valorEtapaAtualCadastro) ? "selected" : "" %>>ACT</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-status">Status</label>
                                        <select id="cad-ciclo-status" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusCadastro) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-dt_inicio">Data de início</label>
                                        <input type="date" id="cad-ciclo-dt_inicio" name="dt_inicio" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarCadastro ? request.getParameter("dt_inicio") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-dt_fim">Data de fim</label>
                                        <input type="date" id="cad-ciclo-dt_fim" name="dt_fim" min="1900-01-01" max="2100-12-31" value="<%= h(recuperarCadastro ? request.getParameter("dt_fim") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-id_empresa">Empresa</label>
                                        <input type="text" id="cad-ciclo-id_empresa" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="cad-ciclo-id_empresa-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-ciclo-id_empresa-valor" name="id_empresa" value="<%= h(valorIdEmpresaCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-ciclo-id_responsavel">Responsável</label>
                                        <input type="text" id="cad-ciclo-id_responsavel" data-lista="sugestoes-fk-colaborador-lista" data-validar-lista data-campo-valor="cad-ciclo-id_responsavel-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-ciclo-id_responsavel-valor" name="id_responsavel" value="<%= h(valorIdResponsavelCadastro) %>">
                                    </div>

                                    <div class="botoes-modal">
                                        <a href="#ciclos" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Ciclo ciclo : ciclos) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(ciclo.getId_ciclo()).equals(request.getParameter("id-ciclo"));
String valorEtapaAtualEdicao = "";
if (recuperarEdicao) {
    valorEtapaAtualEdicao = request.getParameter("etapa_atual");
} else if (ciclo.getEtapa_atual() != null) {
    valorEtapaAtualEdicao = ciclo.getEtapa_atual().toString();
}
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (ciclo.getStatus() != null) {
    valorStatusEdicao = ciclo.getStatus().toString();
}
String valorIdEmpresaEdicao = "";
if (recuperarEdicao) {
    valorIdEmpresaEdicao = request.getParameter("id_empresa");
} else if (ciclo.getId_empresa() != null) {
    valorIdEmpresaEdicao = ciclo.getId_empresa().toString();
}
String valorIdResponsavelEdicao = "";
if (recuperarEdicao) {
    valorIdResponsavelEdicao = request.getParameter("id_responsavel");
} else if (ciclo.getId_responsavel() != null) {
    valorIdResponsavelEdicao = ciclo.getId_responsavel().toString();
}
%>
<section id="editar-ciclo-<%= ciclo.getId_ciclo() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Editar ciclo</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/ciclo-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-ciclo" value="<%= ciclo.getId_ciclo() %>">
                                    <div class="campo">
                                        <label for="edi-ciclo-nome-<%= ciclo.getId_ciclo() %>">Nome</label>
                                        <input type="text" id="edi-ciclo-nome-<%= ciclo.getId_ciclo() %>" name="nome" maxlength="100" placeholder="Digite nome" required value="<%= h(recuperarEdicao ? request.getParameter("nome") : ciclo.getNome()) %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-ciclo-descricao-<%= ciclo.getId_ciclo() %>">Descrição</label>
                                        <textarea id="edi-ciclo-descricao-<%= ciclo.getId_ciclo() %>" name="descricao" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao") : ciclo.getDescricao()) %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-etapa_atual-<%= ciclo.getId_ciclo() %>">Etapa atual</label>
                                        <select id="edi-ciclo-etapa_atual-<%= ciclo.getId_ciclo() %>" name="etapa_atual" required>
                                            <option value="PLAN" <%= "PLAN".equals(valorEtapaAtualEdicao) ? "selected" : "" %>>PLAN</option>
                                            <option value="DO" <%= "DO".equals(valorEtapaAtualEdicao) ? "selected" : "" %>>DO</option>
                                            <option value="CHECK" <%= "CHECK".equals(valorEtapaAtualEdicao) ? "selected" : "" %>>CHECK</option>
                                            <option value="ACT" <%= "ACT".equals(valorEtapaAtualEdicao) ? "selected" : "" %>>ACT</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-status-<%= ciclo.getId_ciclo() %>">Status</label>
                                        <select id="edi-ciclo-status-<%= ciclo.getId_ciclo() %>" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusEdicao) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-dt_inicio-<%= ciclo.getId_ciclo() %>">Data de início</label>
                                        <input type="date" id="edi-ciclo-dt_inicio-<%= ciclo.getId_ciclo() %>" name="dt_inicio" required min="1900-01-01" max="2100-12-31" value="<%= h(recuperarEdicao ? request.getParameter("dt_inicio") : ciclo.getDt_inicio()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-dt_fim-<%= ciclo.getId_ciclo() %>">Data de fim</label>
                                        <input type="date" id="edi-ciclo-dt_fim-<%= ciclo.getId_ciclo() %>" name="dt_fim" min="1900-01-01" max="2100-12-31" value="<%= h(recuperarEdicao ? request.getParameter("dt_fim") : ciclo.getDt_fim()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-id_empresa-<%= ciclo.getId_ciclo() %>">Empresa</label>
                                        <input type="text" id="edi-ciclo-id_empresa-<%= ciclo.getId_ciclo() %>" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="edi-ciclo-id_empresa-<%= ciclo.getId_ciclo() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-ciclo-id_empresa-<%= ciclo.getId_ciclo() %>-valor" name="id_empresa" value="<%= h(valorIdEmpresaEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-ciclo-id_responsavel-<%= ciclo.getId_ciclo() %>">Responsável</label>
                                        <input type="text" id="edi-ciclo-id_responsavel-<%= ciclo.getId_ciclo() %>" data-lista="sugestoes-fk-colaborador-lista" data-validar-lista data-campo-valor="edi-ciclo-id_responsavel-<%= ciclo.getId_ciclo() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-ciclo-id_responsavel-<%= ciclo.getId_ciclo() %>-valor" name="id_responsavel" value="<%= h(valorIdResponsavelEdicao) %>">
                                    </div>

                                    <div class="botoes-modal">
                                        <a href="#ciclos" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Ciclo ciclo : ciclos) { %>
<section id="excluir-ciclo-<%= ciclo.getId_ciclo() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(ciclo.getNome() == null || ciclo.getNome().isBlank() ? "—" : ciclo.getNome()) %></p>
                                <h2>Excluir ciclo</h2>
                                <p>Tem certeza que deseja excluir este ciclo?</p>
                                <form method="post" action="<%= request.getContextPath() %>/ciclo-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-ciclo" value="<%= ciclo.getId_ciclo() %>">
                                    <div class="botoes-modal">
                                    <a href="#ciclos" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

                <div id="sugestoes-fk-empresa-lista" class="lista-escolhas" role="listbox" hidden>
<% for (Empresa opcao : empresaOpcoes) {
    String nomeOpcao = opcao.getNome();
    if (nomeOpcao != null && !nomeOpcao.isBlank()) {
        nomeOpcao = nomeOpcao.trim();
        int nomesIguais = 0;
        for (Empresa outra : empresaOpcoes) {
            String outroNome = outra.getNome();
            if (outroNome != null && nomeOpcao.equals(outroNome.trim())) nomesIguais++;
        }
        if (nomesIguais > 1) nomeOpcao += " (#" + opcao.getId_empresa() + ")"; %>
<button type="button" role="option" data-valor="<%= opcao.getId_empresa() %>"><%= h(nomeOpcao) %></button>
<% }
} %>
                <button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
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
            </main>
        </div>
    </body>
</html>
