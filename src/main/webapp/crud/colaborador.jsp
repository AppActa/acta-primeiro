<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("colaboradorList") == null) {
    response.sendRedirect(request.getContextPath() + "/colaborador-servlet");
    return;
}
List<Colaborador> colaboradores = (List<Colaborador>) request.getAttribute("colaboradorList");
List<Empresa> empresaOpcoes = (List<Empresa>) request.getAttribute("empresaOpcoes");
if (empresaOpcoes == null) empresaOpcoes = new ArrayList<Empresa>();
List<String> areaList = (List<String>) request.getAttribute("areaList");
if (areaList == null) areaList = new ArrayList<String>();
List<String> cargoList = (List<String>) request.getAttribute("cargoList");
if (cargoList == null) cargoList = new ArrayList<String>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroStatus = "";
if (consulta && request.getParameter("status") != null) filtroStatus = request.getParameter("status");
String filtroArea = "";
if (consulta && request.getParameter("area") != null) filtroArea = request.getParameter("area");
String filtroCargo = "";
if (consulta && request.getParameter("cargo") != null) filtroCargo = request.getParameter("cargo");
String filtroGestor = "";
if (consulta && request.getParameter("gestor") != null) filtroGestor = request.getParameter("gestor");
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
if (recuperarCadastro) modalErro = "cadastrar-colaborador";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Colaborador colaborador : colaboradores) {
        if (String.valueOf(colaborador.getId_colaborador()).equals(request.getParameter("id-colaborador"))) {
            modalErro = "editar-colaborador-" + colaborador.getId_colaborador();
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
        <title>ACTA — Colaborador</title>
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
                    <a href="<%= request.getContextPath() %>/empresa-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_empresas.svg" alt="">
                        <span>Empresas</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/endereco-servlet" class="menu-item">
                        <img src="<%= request.getContextPath() %>/assets/images/icone_endereco.svg" alt="">
                        <span>Endereço</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/colaborador-servlet" class="menu-item ativo">
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
                    <a href="<%= request.getContextPath() %>/logout-servlet" class="botao botao-sair">Sair</a>
                </div>
            </aside>
            <main class="conteudo">

                <section id="colaborador" class="pagina">
                    <h1>Colaborador</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-colaborador" form="filtros-colaborador-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-colaborador" aria-expanded="false">Filtros</button>
                            <section id="filtros-colaborador" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-colaborador-form" method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
                                            <div class="campo">
                                                <label for="filtros-colaborador-status">Status</label>
                                                <select id="filtros-colaborador-status" name="status">
                                                    <option value="" <%= "".equals(filtroStatus) ? "selected" : "" %>>Todos</option>
                                                    <option value="ATIVA" <%= "ATIVA".equals(filtroStatus) ? "selected" : "" %>>Ativa</option>
                                                    <option value="INATIVA" <%= "INATIVA".equals(filtroStatus) ? "selected" : "" %>>Inativa</option>
                                                </select>
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-colaborador-area">Área</label>
                                                <input type="text" id="filtros-colaborador-area" name="area" data-lista="sugestoes-colaborador-area-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroArea) %>">
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-colaborador-cargo">Cargo</label>
                                                <input type="text" id="filtros-colaborador-cargo" name="cargo" data-lista="sugestoes-colaborador-cargo-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroCargo) %>">
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-colaborador-gestor">Permissão de gestor</label>
                                                <input type="text" id="filtros-colaborador-gestor" data-lista="sugestoes-permissao-gestor-lista" data-validar-lista data-campo-valor="filtros-colaborador-gestor-valor" autocomplete="off" placeholder="Todos">
<input type="hidden" id="filtros-colaborador-gestor-valor" name="gestor" value="<%= h(filtroGestor) %>">
                                            </div>
                                            <div class="campo periodo-datas">
                                                <span class="rotulo-campo">Contratação</span>
                                                <div class="datas-linha">
                                                    <div class="campo"><label for="filtros-colaborador-dataInicial">De</label><input type="date" id="filtros-colaborador-dataInicial" name="dataInicial" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataInicial) %>"></div>
                                                    <div class="campo"><label for="filtros-colaborador-dataFinal">Até</label><input type="date" id="filtros-colaborador-dataFinal" name="dataFinal" min="1900-01-01" max="2100-12-31" value="<%= h(filtroDataFinal) %>"></div>
                                                </div>
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/colaborador-servlet#colaborador" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-colaborador" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "nome".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="nome">
<input type="hidden" name="direcao" value="<%= "nome".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Nome">Nome
<% if ("nome".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "sobrenome".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="sobrenome">
<input type="hidden" name="direcao" value="<%= "sobrenome".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Sobrenome">Sobrenome
<% if ("sobrenome".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "area".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="area">
<input type="hidden" name="direcao" value="<%= "area".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Área">Área
<% if ("area".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "cargo".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="cargo">
<input type="hidden" name="direcao" value="<%= "cargo".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Cargo">Cargo
<% if ("cargo".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Telefone</th>
                                    <th scope="col">CPF</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "email".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="email">
<input type="hidden" name="direcao" value="<%= "email".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Email">Email
<% if ("email".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "status".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="status">
<input type="hidden" name="direcao" value="<%= "status".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Status">Status
<% if ("status".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "empresa".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="empresa">
<input type="hidden" name="direcao" value="<%= "empresa".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Empresa">Empresa
<% if ("empresa".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "permissao".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="permissao">
<input type="hidden" name="direcao" value="<%= "permissao".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Permissão de gestor">Permissão de gestor
<% if ("permissao".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "contratacao".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/colaborador-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="status" value="<%= h(filtroStatus) %>">
<input type="hidden" name="area" value="<%= h(filtroArea) %>">
<input type="hidden" name="cargo" value="<%= h(filtroCargo) %>">
<input type="hidden" name="gestor" value="<%= h(filtroGestor) %>">
<input type="hidden" name="dataInicial" value="<%= h(filtroDataInicial) %>">
<input type="hidden" name="dataFinal" value="<%= h(filtroDataFinal) %>">
<input type="hidden" name="ordem" value="contratacao">
<input type="hidden" name="direcao" value="<%= "contratacao".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Data de contratação">Data de contratação
<% if ("contratacao".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (colaboradores.isEmpty()) { %>
<tr><td colspan="15">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Colaborador colaborador : colaboradores) { %>
<tr><td><%= h(colaborador.getId_colaborador()) %></td><td><%= h(colaborador.getNome() == null || colaborador.getNome().isBlank() ? "—" : colaborador.getNome()) %></td><td><%= h(colaborador.getSobrenome() == null || colaborador.getSobrenome().isBlank() ? "—" : colaborador.getSobrenome()) %></td><td><%= h(colaborador.getArea() == null || colaborador.getArea().isBlank() ? "—" : colaborador.getArea()) %></td><td><%= h(colaborador.getCargo() == null || colaborador.getCargo().isBlank() ? "—" : colaborador.getCargo()) %></td><td data-formato="telefone"><%= h(colaborador.getTelefone() == null || colaborador.getTelefone().isBlank() ? "—" : colaborador.getTelefone()) %></td><td data-formato="cpf"><%= h(colaborador.getCpf() == null || colaborador.getCpf().isBlank() ? "—" : colaborador.getCpf()) %></td><td><%= h(colaborador.getEmail() == null || colaborador.getEmail().isBlank() ? "—" : colaborador.getEmail()) %></td><td><%
String textoStatus = "—";
if (colaborador.getStatus() == Status.ATIVA) {
    textoStatus = "Ativa";
}
else if (colaborador.getStatus() == Status.INATIVA) {
    textoStatus = "Inativa";
}
%>
<%= h(textoStatus) %></td><td><%= h(colaborador.getNome_empresa() == null || colaborador.getNome_empresa().isBlank() ? "—" : colaborador.getNome_empresa()) %></td><td><%= h(colaborador.getPermissao_gestor() == null ? "—" : (colaborador.getPermissao_gestor() ? "Sim" : "Não")) %></td><td><%= h(colaborador.getDt_contratacao() == null ? "—" : colaborador.getDt_contratacao().toLocalDate().format(formatoData)) %></td><td class="coluna-acao"><a href="#ver-colaborador-<%= colaborador.getId_colaborador() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-colaborador-<%= colaborador.getId_colaborador() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-colaborador-<%= colaborador.getId_colaborador() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Colaborador colaborador : colaboradores) { %>
<section id="ver-colaborador-<%= colaborador.getId_colaborador() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver modal-larga">
                                <div class="modal-topo">
                                    <h2>Detalhes do colaborador</h2>
                                    <a href="#colaborador" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Nome</span><span class="detalhe-valor"><%= h(colaborador.getNome() == null || colaborador.getNome().isBlank() ? "—" : colaborador.getNome()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Sobrenome</span><span class="detalhe-valor"><%= h(colaborador.getSobrenome() == null || colaborador.getSobrenome().isBlank() ? "—" : colaborador.getSobrenome()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Área</span><span class="detalhe-valor"><%= h(colaborador.getArea() == null || colaborador.getArea().isBlank() ? "—" : colaborador.getArea()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Cargo</span><span class="detalhe-valor"><%= h(colaborador.getCargo() == null || colaborador.getCargo().isBlank() ? "—" : colaborador.getCargo()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Telefone</span><span class="detalhe-valor" data-formato="telefone"><%= h(colaborador.getTelefone() == null || colaborador.getTelefone().isBlank() ? "—" : colaborador.getTelefone()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">CPF</span><span class="detalhe-valor" data-formato="cpf"><%= h(colaborador.getCpf() == null || colaborador.getCpf().isBlank() ? "—" : colaborador.getCpf()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Email</span><span class="detalhe-valor"><%= h(colaborador.getEmail() == null || colaborador.getEmail().isBlank() ? "—" : colaborador.getEmail()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Status</span><span class="detalhe-valor"><%
String textoStatus = "—";
if (colaborador.getStatus() == Status.ATIVA) {
    textoStatus = "Ativa";
}
else if (colaborador.getStatus() == Status.INATIVA) {
    textoStatus = "Inativa";
}
%>
<%= h(textoStatus) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Empresa</span><span class="detalhe-valor"><%= h(colaborador.getNome_empresa() == null || colaborador.getNome_empresa().isBlank() ? "—" : colaborador.getNome_empresa()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Permissão de gestor</span><span class="detalhe-valor"><%= h(colaborador.getPermissao_gestor() == null ? "—" : (colaborador.getPermissao_gestor() ? "Sim" : "Não")) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Data de contratação</span><span class="detalhe-valor"><%= h(colaborador.getDt_contratacao() == null ? "—" : colaborador.getDt_contratacao().toLocalDate().format(formatoData)) %></span></div>
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
String valorPermissaoGestorCadastro = "";
if (recuperarCadastro) {
    valorPermissaoGestorCadastro = request.getParameter("permissao_gestor");
}
String valorIdEmpresaCadastro = "";
if (recuperarCadastro) {
    valorIdEmpresaCadastro = request.getParameter("id_empresa");
}
%>
<section id="cadastrar-colaborador" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Cadastrar colaborador</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/colaborador-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-colaborador-nome">Nome</label>
                                        <input type="text" id="cad-colaborador-nome" name="nome" required maxlength="50" value="<%= h(recuperarCadastro ? request.getParameter("nome") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-sobrenome">Sobrenome</label>
                                        <input type="text" id="cad-colaborador-sobrenome" name="sobrenome" required maxlength="50" value="<%= h(recuperarCadastro ? request.getParameter("sobrenome") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-telefone">Telefone</label>
                                        <input type="tel" id="cad-colaborador-telefone" name="telefone" data-mascara="telefone" required maxlength="15" inputmode="numeric" pattern="^(?:\([0-9]{2}\)|[0-9]{2}) ?[0-9]{4,5}-?[0-9]{4}$" value="<%= h(recuperarCadastro ? request.getParameter("telefone") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-cpf">CPF</label>
                                        <input type="text" id="cad-colaborador-cpf" name="cpf" data-mascara="cpf" required maxlength="14" inputmode="numeric" pattern="^[0-9]{3}\.?[0-9]{3}\.?[0-9]{3}-?[0-9]{2}$" value="<%= h(recuperarCadastro ? request.getParameter("cpf") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-senha">Senha</label>
                                        <input type="password" id="cad-colaborador-senha" name="senha" required maxlength="100" minlength="8" autocomplete="new-password" pattern="^(?=.*[A-Za-z])(?=.*\d).{8,}$">
                                    </div>

                                    <div class="campo">
                                        <label for="cad-colaborador-email">Email</label>
                                        <input type="email" id="cad-colaborador-email" name="email" maxlength="80" placeholder="Digite email" required value="<%= h(recuperarCadastro ? request.getParameter("email") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-area">Área</label>
                                        <input type="text" id="cad-colaborador-area" name="area" maxlength="30" placeholder="Digite área" required data-lista="sugestoes-colaborador-area-lista" value="<%= h(recuperarCadastro ? request.getParameter("area") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-cargo">Cargo</label>
                                        <input type="text" id="cad-colaborador-cargo" name="cargo" maxlength="100" placeholder="Digite cargo" required data-lista="sugestoes-colaborador-cargo-lista" value="<%= h(recuperarCadastro ? request.getParameter("cargo") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-dt_contratacao">Data de contratação</label>
                                        <input type="date" id="cad-colaborador-dt_contratacao" name="dt_contratacao" required min="1900-01-01" max="<%= java.time.LocalDate.now(ZoneId.of("America/Sao_Paulo")) %>" value="<%= h(recuperarCadastro ? request.getParameter("dt_contratacao") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-status">Status</label>
                                        <select id="cad-colaborador-status" name="status" required>
                                            <option value="ATIVA" <%= "ATIVA".equals(valorStatusCadastro) ? "selected" : "" %>>Ativa</option>
                                            <option value="INATIVA" <%= "INATIVA".equals(valorStatusCadastro) ? "selected" : "" %>>Inativa</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-permissao_gestor">Permissão de gestor</label>
                                        <input type="text" id="cad-colaborador-permissao_gestor" data-lista="sugestoes-permissao-gestor-lista" data-validar-lista data-campo-valor="cad-colaborador-permissao_gestor-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-colaborador-permissao_gestor-valor" name="permissao_gestor" value="<%= h(valorPermissaoGestorCadastro == null || valorPermissaoGestorCadastro.isBlank() ? "false" : valorPermissaoGestorCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-colaborador-id_empresa">Empresa</label>
                                        <input type="text" id="cad-colaborador-id_empresa" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="cad-colaborador-id_empresa-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-colaborador-id_empresa-valor" name="id_empresa" value="<%= h(valorIdEmpresaCadastro) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#colaborador" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Colaborador colaborador : colaboradores) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(colaborador.getId_colaborador()).equals(request.getParameter("id-colaborador"));
String valorStatusEdicao = "";
if (recuperarEdicao) {
    valorStatusEdicao = request.getParameter("status");
} else if (colaborador.getStatus() != null) {
    valorStatusEdicao = colaborador.getStatus().toString();
}
String valorPermissaoGestorEdicao = "";
if (recuperarEdicao) {
    valorPermissaoGestorEdicao = request.getParameter("permissao_gestor");
} else if (colaborador.getPermissao_gestor() != null) {
    valorPermissaoGestorEdicao = colaborador.getPermissao_gestor().toString();
}
String valorIdEmpresaEdicao = "";
if (recuperarEdicao) {
    valorIdEmpresaEdicao = request.getParameter("id_empresa");
} else if (colaborador.getId_empresa() != null) {
    valorIdEmpresaEdicao = colaborador.getId_empresa().toString();
}
%>
<section id="editar-colaborador-<%= colaborador.getId_colaborador() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-larga">
                                <h2>Editar colaborador</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/colaborador-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-colaborador" value="<%= colaborador.getId_colaborador() %>">
                                    <div class="campo">
                                        <label for="edi-colaborador-nome-<%= colaborador.getId_colaborador() %>">Nome</label>
                                        <input type="text" id="edi-colaborador-nome-<%= colaborador.getId_colaborador() %>" name="nome" required maxlength="50" value="<%= h(recuperarEdicao ? request.getParameter("nome") : colaborador.getNome()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-sobrenome-<%= colaborador.getId_colaborador() %>">Sobrenome</label>
                                        <input type="text" id="edi-colaborador-sobrenome-<%= colaborador.getId_colaborador() %>" name="sobrenome" required maxlength="50" value="<%= h(recuperarEdicao ? request.getParameter("sobrenome") : colaborador.getSobrenome()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-email-<%= colaborador.getId_colaborador() %>">Email</label>
                                        <input type="email" id="edi-colaborador-email-<%= colaborador.getId_colaborador() %>" name="email" required maxlength="80" value="<%= h(recuperarEdicao ? request.getParameter("email") : colaborador.getEmail()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-telefone-<%= colaborador.getId_colaborador() %>">Telefone</label>
                                        <input type="tel" id="edi-colaborador-telefone-<%= colaborador.getId_colaborador() %>" name="telefone" data-mascara="telefone" required maxlength="15" inputmode="numeric" pattern="^(?:\([0-9]{2}\)|[0-9]{2}) ?[0-9]{4,5}-?[0-9]{4}$" value="<%= h(recuperarEdicao ? request.getParameter("telefone") : colaborador.getTelefone()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-cpf-<%= colaborador.getId_colaborador() %>">CPF</label>
                                        <input type="text" id="edi-colaborador-cpf-<%= colaborador.getId_colaborador() %>" name="cpf" data-mascara="cpf" required maxlength="14" inputmode="numeric" pattern="^[0-9]{3}\.?[0-9]{3}\.?[0-9]{3}-?[0-9]{2}$" value="<%= h(recuperarEdicao ? request.getParameter("cpf") : colaborador.getCpf()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-dt_contratacao-<%= colaborador.getId_colaborador() %>">Data de contratação</label>
                                        <input type="date" id="edi-colaborador-dt_contratacao-<%= colaborador.getId_colaborador() %>" name="dt_contratacao" required min="1900-01-01" max="<%= java.time.LocalDate.now(ZoneId.of("America/Sao_Paulo")) %>" value="<%= h(recuperarEdicao ? request.getParameter("dt_contratacao") : colaborador.getDt_contratacao()) %>">
                                    </div>

                                    <div class="campo">
                                        <label for="edi-colaborador-area-<%= colaborador.getId_colaborador() %>">Área</label>
                                        <input type="text" id="edi-colaborador-area-<%= colaborador.getId_colaborador() %>" name="area" maxlength="30" placeholder="Digite área" required data-lista="sugestoes-colaborador-area-lista" value="<%= h(recuperarEdicao ? request.getParameter("area") : colaborador.getArea()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-cargo-<%= colaborador.getId_colaborador() %>">Cargo</label>
                                        <input type="text" id="edi-colaborador-cargo-<%= colaborador.getId_colaborador() %>" name="cargo" maxlength="100" placeholder="Digite cargo" required data-lista="sugestoes-colaborador-cargo-lista" value="<%= h(recuperarEdicao ? request.getParameter("cargo") : colaborador.getCargo()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-status-<%= colaborador.getId_colaborador() %>">Status</label>
                                        <select id="edi-colaborador-status-<%= colaborador.getId_colaborador() %>" name="status" required>
                                            <option value="ATIVA" <%= "ATIVA".equals(valorStatusEdicao) ? "selected" : "" %>>Ativa</option>
                                            <option value="INATIVA" <%= "INATIVA".equals(valorStatusEdicao) ? "selected" : "" %>>Inativa</option>
                                        </select>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-permissao_gestor-<%= colaborador.getId_colaborador() %>">Permissão de gestor</label>
                                        <input type="text" id="edi-colaborador-permissao_gestor-<%= colaborador.getId_colaborador() %>" data-lista="sugestoes-permissao-gestor-lista" data-validar-lista data-campo-valor="edi-colaborador-permissao_gestor-<%= colaborador.getId_colaborador() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-colaborador-permissao_gestor-<%= colaborador.getId_colaborador() %>-valor" name="permissao_gestor" value="<%= h(valorPermissaoGestorEdicao == null || valorPermissaoGestorEdicao.isBlank() ? "false" : valorPermissaoGestorEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-colaborador-id_empresa-<%= colaborador.getId_colaborador() %>">Empresa</label>
                                        <input type="text" id="edi-colaborador-id_empresa-<%= colaborador.getId_colaborador() %>" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="edi-colaborador-id_empresa-<%= colaborador.getId_colaborador() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-colaborador-id_empresa-<%= colaborador.getId_colaborador() %>-valor" name="id_empresa" value="<%= h(valorIdEmpresaEdicao) %>">
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#colaborador" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Colaborador colaborador : colaboradores) { %>
<section id="excluir-colaborador-<%= colaborador.getId_colaborador() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(colaborador.getNome() == null || colaborador.getNome().isBlank() ? "—" : colaborador.getNome()) %></p>
                                <h2>Excluir colaborador</h2>
                                <p>Tem certeza que deseja excluir este colaborador?</p>
                                <form method="post" action="<%= request.getContextPath() %>/colaborador-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-colaborador" value="<%= colaborador.getId_colaborador() %>">
                                    <div class="botoes-modal">
                                    <a href="#colaborador" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>


<div id="sugestoes-colaborador-area-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : areaList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<button type="button" role="option" data-valor="<%= h(sugestao) %>"><%= h(sugestao) %></button>
<% } %>
<% } %>
<button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>

<div id="sugestoes-colaborador-cargo-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : cargoList) { %>
<% if (sugestao != null && !sugestao.isBlank()) { %>
<button type="button" role="option" data-valor="<%= h(sugestao) %>"><%= h(sugestao) %></button>
<% } %>
<% } %>
<button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
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
                <div id="sugestoes-permissao-gestor-lista" class="lista-escolhas" role="listbox" hidden>
<% for (boolean opcao : new boolean[]{false, true}) { %>
<button type="button" role="option" data-valor="<%= opcao %>"><%= opcao ? "Sim" : "Não" %></button>
<% } %>
                <button type="button" role="option" data-limpar>Limpar seleção</button>
<p class="lista-vazia" role="status" hidden>Nenhuma opção encontrada.</p>
</div>
            </main>
        </div>
    </body>
</html>
