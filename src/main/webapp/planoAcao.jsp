<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("planoAcaoList") == null) {
    response.sendRedirect(request.getContextPath() + "/planoAcao-servlet");
    return;
}
List<PlanoAcao> planos = (List<PlanoAcao>) request.getAttribute("planoAcaoList");
List<Ciclo> cicloOpcoes = (List<Ciclo>) request.getAttribute("cicloOpcoes");
if (cicloOpcoes == null) cicloOpcoes = new ArrayList<Ciclo>();
List<Colaborador> colaboradorOpcoes = (List<Colaborador>) request.getAttribute("colaboradorOpcoes");
if (colaboradorOpcoes == null) colaboradorOpcoes = new ArrayList<Colaborador>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroStatus = "";
if (consulta && request.getParameter("status") != null) filtroStatus = request.getParameter("status");
String filtroPrioridade = "";
if (consulta && request.getParameter("prioridade") != null) filtroPrioridade = request.getParameter("prioridade");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-plano-de-ação";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (PlanoAcao planoAcao : planos) {
        if (String.valueOf(planoAcao.getId_plano_acao()).equals(request.getParameter("id-planoAcao"))) {
            modalErro = "editar-plano-de-ação-" + planoAcao.getId_plano_acao();
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
        <title>ACTA — Plano de Ação</title>
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
                    <a href="<%= request.getContextPath() %>/planoAcao-servlet" class="menu-item ativo">
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

                <section id="plano" class="pagina">
                    <h1>Plano de Ação</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-plano" form="filtros-plano-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-plano" aria-expanded="false">Filtros</button>
                            <section id="filtros-plano" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-plano-form" method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
                                            <div class="campo">
                                                <label for="filtros-plano-status">Status</label>
                                                <select id="filtros-plano-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(filtroStatus) ? "selected" : "" %>>Não iniciado</option>
                                                    <option value="INICIADO" <%= "INICIADO".equals(filtroStatus) ? "selected" : "" %>>Iniciado</option>
                                                    <option value="FINALIZADO" <%= "FINALIZADO".equals(filtroStatus) ? "selected" : "" %>>Finalizado</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-plano-prioridade">Prioridade</label>
                                                <select id="filtros-plano-prioridade" name="prioridade">
                                                    <option value="" <%= "".equals(filtroPrioridade) ? "selected" : "" %>>Todos</option>
                                                    <option value="ALTO" <%= "ALTO".equals(filtroPrioridade) ? "selected" : "" %>>Alto</option>
                                                    <option value="MEDIO" <%= "MEDIO".equals(filtroPrioridade) ? "selected" : "" %>>Médio</option>
                                                    <option value="BAIXO" <%= "BAIXO".equals(filtroPrioridade) ? "selected" : "" %>>Baixo</option>
                                                </select>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/planoAcao-servlet#plano" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-plano-de-ação" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "nome".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="nome">
<input type="hidden" name="direcao" value="<%= "nome".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Nome">Nome
<% if ("nome".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "ciclo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="ciclo">
<input type="hidden" name="direcao" value="<%= "ciclo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Ciclo">Ciclo
<% if ("ciclo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "criador".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="criador">
<input type="hidden" name="direcao" value="<%= "criador".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Criador">Criador
<% if ("criador".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "prioridade".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="prioridade">
<input type="hidden" name="direcao" value="<%= "prioridade".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Prioridade">Prioridade
<% if ("prioridade".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/planoAcao-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="prioridade" value="<%= h(filtroPrioridade) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (planos.isEmpty()) { %>
<tr><td colspan="9">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (PlanoAcao planoAcao : planos) { %>
<tr><td><%= h(planoAcao.getId_plano_acao()) %></td><td><%= h(planoAcao.getNome() == null || planoAcao.getNome().isBlank() ? "—" : planoAcao.getNome()) %></td><td><%= h(planoAcao.getNome_ciclo() == null || planoAcao.getNome_ciclo().isBlank() ? "—" : planoAcao.getNome_ciclo()) %></td><td><%= h(planoAcao.getNome_criador() == null || planoAcao.getNome_criador().isBlank() ? "—" : planoAcao.getNome_criador()) %></td><td><%
String textoPrioridade = "—";
if (planoAcao.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (planoAcao.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (planoAcao.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></td><td><%
String textoStatus = "—";
if (planoAcao.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (planoAcao.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (planoAcao.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></td><td class="coluna-acao"><a href="#ver-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (PlanoAcao planoAcao : planos) { %>
<section id="ver-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver modal-larga">
                                <div class="modal-topo">
                                    <h2>Detalhes do plano de ação</h2>
                                    <a href="#plano" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Nome</span><span class="detalhe-valor"><%= h(planoAcao.getNome() == null || planoAcao.getNome().isBlank() ? "—" : planoAcao.getNome()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Ciclo</span><span class="detalhe-valor"><%= h(planoAcao.getNome_ciclo() == null || planoAcao.getNome_ciclo().isBlank() ? "—" : planoAcao.getNome_ciclo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Criador</span><span class="detalhe-valor"><%= h(planoAcao.getNome_criador() == null || planoAcao.getNome_criador().isBlank() ? "—" : planoAcao.getNome_criador()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Prioridade</span><span class="detalhe-valor"><%
String textoPrioridade = "—";
if (planoAcao.getPrioridade() == Intensidade.ALTO) {
    textoPrioridade = "Alto";
}
else if (planoAcao.getPrioridade() == Intensidade.MEDIO) {
    textoPrioridade = "Médio";
}
else if (planoAcao.getPrioridade() == Intensidade.BAIXO) {
    textoPrioridade = "Baixo";
}
%>
<%= h(textoPrioridade) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (planoAcao.getStatus() == Situacao.NAO_INICIADO) {
    textoStatus = "Não iniciado";
}
else if (planoAcao.getStatus() == Situacao.INICIADO) {
    textoStatus = "Iniciado";
}
else if (planoAcao.getStatus() == Situacao.FINALIZADO) {
    textoStatus = "Finalizado";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Descrição</span><span class="detalhe-valor"><%= h(planoAcao.getDescricao() == null || planoAcao.getDescricao().isBlank() ? "—" : planoAcao.getDescricao()) %></span></div>
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
String valorStatusCadastro = "";
if (recuperarCadastro) {
    valorStatusCadastro = request.getParameter("status");
}
String valorPrioridadeCadastro = "";
if (recuperarCadastro) {
    valorPrioridadeCadastro = request.getParameter("prioridade");
}
String valorIdCriadorCadastro = "";
if (recuperarCadastro) {
    valorIdCriadorCadastro = request.getParameter("id_criador");
}
%>
<section id="cadastrar-plano-de-ação" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Cadastrar plano de ação</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/planoAcao-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-plano-de-ação-nome">Nome</label>
                                        <input type="text" id="cad-plano-de-ação-nome" name="nome" maxlength="100" placeholder="Digite nome" required value="<%= h(recuperarCadastro ? request.getParameter("nome") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-plano-de-ação-id_ciclo">Ciclo</label>
                                        <select id="cad-plano-de-ação-id_ciclo" name="id_ciclo" required>
<option value="">Selecione</option>
<% for (Ciclo opcao : cicloOpcoes) { %>
<option value="<%= opcao.getId_ciclo() %>" <%= String.valueOf(opcao.getId_ciclo()).equals(valorIdCicloCadastro) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-plano-de-ação-descricao">Descrição</label>
                                        <textarea id="cad-plano-de-ação-descricao" name="descricao" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("descricao") : "") %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-plano-de-ação-status">Status</label>
                                        <select id="cad-plano-de-ação-status" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusCadastro) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusCadastro) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-plano-de-ação-prioridade">Prioridade</label>
                                        <select id="cad-plano-de-ação-prioridade" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeCadastro) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-plano-de-ação-id_criador">Criador</label>
                                        <select id="cad-plano-de-ação-id_criador" name="id_criador" required>
<option value="">Selecione</option>
<% for (Colaborador opcao : colaboradorOpcoes) { %>
<option value="<%= opcao.getId_colaborador() %>" <%= String.valueOf(opcao.getId_colaborador()).equals(valorIdCriadorCadastro) ? "selected" : "" %>><%= h(opcao.getNome() + " " + opcao.getSobrenome()) %></option>
<% } %>
</select>
                                    </div>

                                    <div class="botoes-modal">
                                        <a href="#plano" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (PlanoAcao planoAcao : planos) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(planoAcao.getId_plano_acao()).equals(request.getParameter("id-planoAcao"));
String valorIdCicloEdicao = "";
if (recuperarEdicao) {
    valorIdCicloEdicao = request.getParameter("id_ciclo");
} else if (planoAcao.getId_ciclo() != null) {
    valorIdCicloEdicao = planoAcao.getId_ciclo().toString();
}
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (planoAcao.getStatus() != null) {
    valorStatusEdicao = planoAcao.getStatus().toString();
}
String valorPrioridadeEdicao = "";
if (recuperarEdicao) {
    valorPrioridadeEdicao = request.getParameter("prioridade");
} else if (planoAcao.getPrioridade() != null) {
    valorPrioridadeEdicao = planoAcao.getPrioridade().toString();
}
String valorIdCriadorEdicao = "";
if (recuperarEdicao) {
    valorIdCriadorEdicao = request.getParameter("id_criador");
} else if (planoAcao.getId_criador() != null) {
    valorIdCriadorEdicao = planoAcao.getId_criador().toString();
}
%>
<section id="editar-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Editar plano de ação</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/planoAcao-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-planoAcao" value="<%= planoAcao.getId_plano_acao() %>">
                                    <div class="campo">
                                        <label for="edi-plano-de-ação-nome-<%= planoAcao.getId_plano_acao() %>">Nome</label>
                                        <input type="text" id="edi-plano-de-ação-nome-<%= planoAcao.getId_plano_acao() %>" name="nome" maxlength="100" placeholder="Digite nome" required value="<%= h(recuperarEdicao ? request.getParameter("nome") : planoAcao.getNome()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-plano-de-ação-id_ciclo-<%= planoAcao.getId_plano_acao() %>">Ciclo</label>
                                        <select id="edi-plano-de-ação-id_ciclo-<%= planoAcao.getId_plano_acao() %>" name="id_ciclo" required>
<option value="">Selecione</option>
<% for (Ciclo opcao : cicloOpcoes) { %>
<option value="<%= opcao.getId_ciclo() %>" <%= String.valueOf(opcao.getId_ciclo()).equals(valorIdCicloEdicao) ? "selected" : "" %>><%= h(opcao.getNome()) %></option>
<% } %>
</select>
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-plano-de-ação-descricao-<%= planoAcao.getId_plano_acao() %>">Descrição</label>
                                        <textarea id="edi-plano-de-ação-descricao-<%= planoAcao.getId_plano_acao() %>" name="descricao" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("descricao") : planoAcao.getDescricao()) %></textarea>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-plano-de-ação-status-<%= planoAcao.getId_plano_acao() %>">Status</label>
                                        <select id="edi-plano-de-ação-status-<%= planoAcao.getId_plano_acao() %>" name="status" required>
                                            <option value="NAO_INICIADO" <%= "NAO_INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Não iniciado</option>
                                            <option value="INICIADO" <%= "INICIADO".equals(valorStatusEdicao) ? "selected" : "" %>>Iniciado</option>
                                            <option value="FINALIZADO" <%= "FINALIZADO".equals(valorStatusEdicao) ? "selected" : "" %>>Finalizado</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-plano-de-ação-prioridade-<%= planoAcao.getId_plano_acao() %>">Prioridade</label>
                                        <select id="edi-plano-de-ação-prioridade-<%= planoAcao.getId_plano_acao() %>" name="prioridade" required>
                                            <option value="ALTO" <%= "ALTO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Alto</option>
                                            <option value="MEDIO" <%= "MEDIO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Médio</option>
                                            <option value="BAIXO" <%= "BAIXO".equals(valorPrioridadeEdicao) ? "selected" : "" %>>Baixo</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-plano-de-ação-id_criador-<%= planoAcao.getId_plano_acao() %>">Criador</label>
                                        <select id="edi-plano-de-ação-id_criador-<%= planoAcao.getId_plano_acao() %>" name="id_criador" required>
<option value="">Selecione</option>
<% for (Colaborador opcao : colaboradorOpcoes) { %>
<option value="<%= opcao.getId_colaborador() %>" <%= String.valueOf(opcao.getId_colaborador()).equals(valorIdCriadorEdicao) ? "selected" : "" %>><%= h(opcao.getNome() + " " + opcao.getSobrenome()) %></option>
<% } %>
</select>
                                    </div>

                                    <div class="botoes-modal">
                                        <a href="#plano" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (PlanoAcao planoAcao : planos) { %>
<section id="excluir-plano-de-ação-<%= planoAcao.getId_plano_acao() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(planoAcao.getNome() == null || planoAcao.getNome().isBlank() ? "—" : planoAcao.getNome()) %></p>
                                <h2>Excluir plano de ação</h2>
                                <p>Tem certeza que deseja excluir este plano de ação?</p>
                                <form method="post" action="<%= request.getContextPath() %>/planoAcao-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-planoAcao" value="<%= planoAcao.getId_plano_acao() %>">
                                    <div class="botoes-modal">
                                    <a href="#plano" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

            </main>
        </div>
    </body>
</html>
