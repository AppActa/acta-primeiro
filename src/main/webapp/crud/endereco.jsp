<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,java.util.ArrayList,java.time.ZoneId,java.time.format.DateTimeFormatter,br.com.acta.model.*,br.com.acta.enums.*" %>
<%!
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
if (request.getAttribute("enderecoList") == null) {
    response.sendRedirect(request.getContextPath() + "/endereco-servlet");
    return;
}
List<Endereco> enderecos = (List<Endereco>) request.getAttribute("enderecoList");
List<Empresa> empresaOpcoes = (List<Empresa>) request.getAttribute("empresaOpcoes");
if (empresaOpcoes == null) empresaOpcoes = new ArrayList<Empresa>();
List<String> cidadeList = (List<String>) request.getAttribute("cidadeList");
if (cidadeList == null) cidadeList = new ArrayList<String>();

boolean consulta = "GET".equalsIgnoreCase(request.getMethod());
String filtroBusca = "";
if (consulta && request.getParameter("busca") != null) filtroBusca = request.getParameter("busca");
String filtroEstado = "";
if (consulta && request.getParameter("estado") != null) filtroEstado = request.getParameter("estado");
String filtroCidade = "";
if (consulta && request.getParameter("cidade") != null) filtroCidade = request.getParameter("cidade");
String ordem = "";
if (consulta && request.getParameter("ordem") != null) ordem = request.getParameter("ordem");
String direcao = "ASC";
if (consulta && "DESC".equalsIgnoreCase(request.getParameter("direcao"))) direcao = "DESC";

boolean recuperarCadastro = request.getAttribute("mensagemErro") != null && "inserir".equals(request.getParameter("acao"));
String modalErro = "";
if (recuperarCadastro) modalErro = "cadastrar-endereco";
if (request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao"))) {
    for (Endereco endereco : enderecos) {
        if (String.valueOf(endereco.getId_endereco()).equals(request.getParameter("id-endereco"))) {
            modalErro = "editar-endereco-" + endereco.getId_endereco();
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
        <title>ACTA — Endereço</title>
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
                    <a href="<%= request.getContextPath() %>/endereco-servlet" class="menu-item ativo">
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

                <section id="endereco" class="pagina">
                    <h1>Endereço</h1>

                    <% if (request.getAttribute("mensagemErro") != null) { %>
<p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p>
<% } %>
                    <% if (request.getAttribute("mensagemSucesso") != null) { %>
<p class="mensagem" role="status"><%= h(request.getAttribute("mensagemSucesso")) %></p>
<% } %>
                    <div class="barra-acoes">
                        <div class="pesquisa">
                            <input type="search" name="busca" id="pesquisa-endereco" form="filtros-endereco-form" placeholder="Pesquisar" aria-label="Pesquisar registros" value="<%= h(filtroBusca) %>">
                        </div>
                        <div class="filtro-container">
                            <button type="button" class="botao botao-filtro" data-abrir-filtros aria-controls="filtros-endereco" aria-expanded="false">Filtros</button>
                            <section id="filtros-endereco" class="filtros-painel" aria-label="Filtros" hidden>
                                <div class="filtros-corpo">
                                    <div class="modal modal-ver modal-filtros">
                                        <div class="modal-topo">
                                            <h2>Filtros</h2>
                                                <button type="button" class="modal-fechar" data-fechar-filtros aria-label="Fechar filtros">&times;</button>
                                        </div>
                                        <form class="formulario" id="filtros-endereco-form" method="get" action="<%= request.getContextPath() %>/endereco-servlet">
                                            <div class="campo">
                                                <label for="filtros-endereco-estado">Estado</label>
                                                <input type="text" id="filtros-endereco-estado" name="estado" maxlength="2" pattern="[A-Za-z]{2}" placeholder="Ex: SP" value="<%= h(filtroEstado) %>">
                                            </div>
                                            <div class="campo">
                                                <label for="filtros-endereco-cidade">Cidade</label>
                                                <input type="text" id="filtros-endereco-cidade" name="cidade" data-lista="sugestoes-endereco-cidade-lista" autocomplete="off" placeholder="Todos" value="<%= h(filtroCidade) %>">
                                            </div>
                                            <input type="hidden" name="ordem" value="<%= h(ordem) %>">
                                            <input type="hidden" name="direcao" value="<%= h(direcao) %>">
                                            <div class="botoes-modal">
                                                <a href="<%= request.getContextPath() %>/endereco-servlet#endereco" class="botao botao-cancelar">Limpar filtros</a>
                                                <button type="submit" class="botao botao-principal">Aplicar</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </div>
                        <a href="#cadastrar-endereco" class="botao botao-principal">Cadastrar</a>
                    </div>
                    <div class="tabela-container">
                        <table>
                            <thead>
                                <tr>

                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "id".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="id">
<input type="hidden" name="direcao" value="<%= "id".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por #">#
<% if ("id".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "empresa".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="empresa">
<input type="hidden" name="direcao" value="<%= "empresa".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Empresa">Empresa
<% if ("empresa".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Unidade</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "rua".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="rua">
<input type="hidden" name="direcao" value="<%= "rua".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Rua">Rua
<% if ("rua".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col">Número</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "bairro".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="bairro">
<input type="hidden" name="direcao" value="<%= "bairro".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Bairro">Bairro
<% if ("bairro".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "cidade".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="cidade">
<input type="hidden" name="direcao" value="<%= "cidade".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Cidade">Cidade
<% if ("cidade".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "estado".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="estado">
<input type="hidden" name="direcao" value="<%= "estado".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por Estado">Estado
<% if ("estado".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-ordenavel" aria-sort="<%= "cep".equals(ordem) ? ("DESC".equals(direcao) ? "descending" : "ascending") : "none" %>">
<form method="get" action="<%= request.getContextPath() %>/endereco-servlet">
<input type="hidden" name="busca" value="<%= h(filtroBusca) %>">
<input type="hidden" name="estado" value="<%= h(filtroEstado) %>">
<input type="hidden" name="cidade" value="<%= h(filtroCidade) %>">
<input type="hidden" name="ordem" value="cep">
<input type="hidden" name="direcao" value="<%= "cep".equals(ordem) && "ASC".equals(direcao) ? "DESC" : "ASC" %>">
<button type="submit" class="botao-ordenacao" aria-label="Ordenar por CEP">CEP
<% if ("cep".equals(ordem)) { %><span aria-hidden="true"><% if ("DESC".equals(direcao)) { %> ▼<% } else { %> ▲<% } %></span><% } %>
</button>
</form>
</th>
                                    <th scope="col" class="coluna-acao">Ver</th>
                                    <th scope="col" class="coluna-acao">Editar</th>
                                    <th scope="col" class="coluna-acao">Excluir</th>
                                </tr>
                            </thead>
                            <tbody>
<% if (enderecos.isEmpty()) { %>
<tr><td colspan="12">Nenhum registro encontrado.</td></tr>
<% } %>
<% for (Endereco endereco : enderecos) { %>
<tr><td><%= h(endereco.getId_endereco()) %></td><td><%= h(endereco.getNome_empresa() == null || endereco.getNome_empresa().isBlank() ? "—" : endereco.getNome_empresa()) %></td><td><%= h(endereco.getUnidade() == null || endereco.getUnidade().isBlank() ? "—" : endereco.getUnidade()) %></td><td><%= h(endereco.getRua() == null || endereco.getRua().isBlank() ? "—" : endereco.getRua()) %></td><td><%= h(endereco.getNumero() == null || endereco.getNumero().isBlank() ? "—" : endereco.getNumero()) %></td><td><%= h(endereco.getBairro() == null || endereco.getBairro().isBlank() ? "—" : endereco.getBairro()) %></td><td><%= h(endereco.getCidade() == null || endereco.getCidade().isBlank() ? "—" : endereco.getCidade()) %></td><td><%= h(endereco.getEstado() == null || endereco.getEstado().isBlank() ? "—" : endereco.getEstado()) %></td><td data-formato="cep"><%= h(endereco.getCep() == null || endereco.getCep().isBlank() ? "—" : endereco.getCep()) %></td><td class="coluna-acao"><a href="#ver-endereco-<%= endereco.getId_endereco() %>" class="icone-acao" aria-label="Ver"><img src="<%= request.getContextPath() %>/assets/images/icone_ver.svg" alt="Ver"></a></td><td class="coluna-acao"><a href="#editar-endereco-<%= endereco.getId_endereco() %>" class="icone-acao" aria-label="Editar"><img src="<%= request.getContextPath() %>/assets/images/icone_editar.svg" alt="Editar"></a></td><td class="coluna-acao"><a href="#excluir-endereco-<%= endereco.getId_endereco() %>" class="icone-acao" aria-label="Excluir"><img src="<%= request.getContextPath() %>/assets/images/icone_excluir.svg" alt="Excluir"></a></td></tr>
<% } %>
</tbody>
                        </table>
                    </div>

                    <% for (Endereco endereco : enderecos) { %>
<section id="ver-endereco-<%= endereco.getId_endereco() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-ver">
                                <div class="modal-topo">
                                    <h2>Detalhes do endereço</h2>
                                    <a href="#endereco" class="modal-fechar" aria-label="Fechar">&times;</a>
                                </div>
                                <div class="detalhes">
                                    <div class="detalhe"><span class="detalhe-label">Empresa</span><span class="detalhe-valor"><%= h(endereco.getNome_empresa() == null || endereco.getNome_empresa().isBlank() ? "—" : endereco.getNome_empresa()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Unidade</span><span class="detalhe-valor"><%= h(endereco.getUnidade() == null || endereco.getUnidade().isBlank() ? "—" : endereco.getUnidade()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Rua</span><span class="detalhe-valor"><%= h(endereco.getRua() == null || endereco.getRua().isBlank() ? "—" : endereco.getRua()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Número</span><span class="detalhe-valor"><%= h(endereco.getNumero() == null || endereco.getNumero().isBlank() ? "—" : endereco.getNumero()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Bairro</span><span class="detalhe-valor"><%= h(endereco.getBairro() == null || endereco.getBairro().isBlank() ? "—" : endereco.getBairro()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Cidade</span><span class="detalhe-valor"><%= h(endereco.getCidade() == null || endereco.getCidade().isBlank() ? "—" : endereco.getCidade()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">Estado</span><span class="detalhe-valor"><%= h(endereco.getEstado() == null || endereco.getEstado().isBlank() ? "—" : endereco.getEstado()) %></span></div>
                                    <div class="detalhe"><span class="detalhe-label">CEP</span><span class="detalhe-valor" data-formato="cep"><%= h(endereco.getCep() == null || endereco.getCep().isBlank() ? "—" : endereco.getCep()) %></span></div>
                                    <div class="detalhe detalhe-largo"><span class="detalhe-label">Complemento</span><span class="detalhe-valor"><%= h(endereco.getComplemento() == null || endereco.getComplemento().isBlank() ? "—" : endereco.getComplemento()) %></span></div>
                                </div>
                            </div>
                        </div>
                    </section>
<% } %>

                    <%
String valorIdEmpresaCadastro = "";
if (recuperarCadastro) {
    valorIdEmpresaCadastro = request.getParameter("id_empresa");
}
%>
<section id="cadastrar-endereco" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Cadastrar endereço</h2>
<% if (recuperarCadastro) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/endereco-servlet">
                                    <input type="hidden" name="acao" value="inserir">
                                    <div class="campo">
                                        <label for="cad-endereco-id_empresa">Empresa</label>
                                        <input type="text" id="cad-endereco-id_empresa" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="cad-endereco-id_empresa-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="cad-endereco-id_empresa-valor" name="id_empresa" value="<%= h(valorIdEmpresaCadastro) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-unidade">Unidade</label>
                                        <input type="text" id="cad-endereco-unidade" name="unidade" maxlength="30" placeholder="Digite unidade" required value="<%= h(recuperarCadastro ? request.getParameter("unidade") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-cep">CEP</label>
                                        <input type="text" id="cad-endereco-cep" name="cep" data-mascara="cep" maxlength="9" inputmode="numeric" placeholder="00000-000" required pattern="^[0-9]{5}-?[0-9]{3}$" value="<%= h(recuperarCadastro ? request.getParameter("cep") : "") %>">
                                        <small data-viacep-status role="status" hidden></small>
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-rua">Rua</label>
                                        <input type="text" id="cad-endereco-rua" name="rua" maxlength="50" placeholder="Digite rua" required value="<%= h(recuperarCadastro ? request.getParameter("rua") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-numero">Número</label>
                                        <input type="text" id="cad-endereco-numero" name="numero" maxlength="10" placeholder="Digite número" required pattern="^(\d{1,6}[A-Za-z]?|[Ss]/[Nn])$" value="<%= h(recuperarCadastro ? request.getParameter("numero") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-bairro">Bairro</label>
                                        <input type="text" id="cad-endereco-bairro" name="bairro" maxlength="30" placeholder="Digite bairro" required value="<%= h(recuperarCadastro ? request.getParameter("bairro") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-cidade">Cidade</label>
                                        <input type="text" id="cad-endereco-cidade" name="cidade" maxlength="30" placeholder="Digite cidade" required data-lista="sugestoes-endereco-cidade-lista" value="<%= h(recuperarCadastro ? request.getParameter("cidade") : "") %>">
                                    </div>
                                    <div class="campo">
                                        <label for="cad-endereco-estado">Estado (UF)</label>
                                        <input type="text" id="cad-endereco-estado" name="estado" maxlength="2" pattern="[A-Za-z]{2}" placeholder="Ex: SP" required value="<%= h(recuperarCadastro ? request.getParameter("estado") : "") %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="cad-endereco-complemento">Complemento</label>
                                        <textarea id="cad-endereco-complemento" name="complemento" rows="3" maxlength="2000"><%= h(recuperarCadastro ? request.getParameter("complemento") : "") %></textarea>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#endereco" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Cadastrar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>

                    <% for (Endereco endereco : enderecos) {
boolean recuperarEdicao = request.getAttribute("mensagemErro") != null && "atualizar".equals(request.getParameter("acao")) && String.valueOf(endereco.getId_endereco()).equals(request.getParameter("id-endereco"));
String valorIdEmpresaEdicao = "";
if (recuperarEdicao) {
    valorIdEmpresaEdicao = request.getParameter("id_empresa");
} else if (endereco.getId_empresa() != null) {
    valorIdEmpresaEdicao = endereco.getId_empresa().toString();
}
%>
<section id="editar-endereco-<%= endereco.getId_endereco() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-grande">
                                <h2>Editar endereço</h2>
<% if (recuperarEdicao) { %><p class="mensagem mensagem-erro" role="alert"><%= h(request.getAttribute("mensagemErro")) %></p><% } %>
                                <form class="formulario" method="post" action="<%= request.getContextPath() %>/endereco-servlet">
                                    <input type="hidden" name="acao" value="atualizar">

                                    <input type="hidden" name="id-endereco" value="<%= endereco.getId_endereco() %>">
                                    <div class="campo">
                                        <label for="edi-endereco-id_empresa-<%= endereco.getId_endereco() %>">Empresa</label>
                                        <input type="text" id="edi-endereco-id_empresa-<%= endereco.getId_endereco() %>" data-lista="sugestoes-fk-empresa-lista" data-validar-lista data-campo-valor="edi-endereco-id_empresa-<%= endereco.getId_endereco() %>-valor" autocomplete="off" placeholder="Selecione" required>
<input type="hidden" id="edi-endereco-id_empresa-<%= endereco.getId_endereco() %>-valor" name="id_empresa" value="<%= h(valorIdEmpresaEdicao) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-unidade-<%= endereco.getId_endereco() %>">Unidade</label>
                                        <input type="text" id="edi-endereco-unidade-<%= endereco.getId_endereco() %>" name="unidade" maxlength="30" placeholder="Digite unidade" required value="<%= h(recuperarEdicao ? request.getParameter("unidade") : endereco.getUnidade()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-cep-<%= endereco.getId_endereco() %>">CEP</label>
                                        <input type="text" id="edi-endereco-cep-<%= endereco.getId_endereco() %>" name="cep" data-mascara="cep" maxlength="9" inputmode="numeric" placeholder="00000-000" required pattern="^[0-9]{5}-?[0-9]{3}$" value="<%= h(recuperarEdicao ? request.getParameter("cep") : endereco.getCep()) %>">
                                        <small data-viacep-status role="status" hidden></small>
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-rua-<%= endereco.getId_endereco() %>">Rua</label>
                                        <input type="text" id="edi-endereco-rua-<%= endereco.getId_endereco() %>" name="rua" maxlength="50" placeholder="Digite rua" required value="<%= h(recuperarEdicao ? request.getParameter("rua") : endereco.getRua()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-numero-<%= endereco.getId_endereco() %>">Número</label>
                                        <input type="text" id="edi-endereco-numero-<%= endereco.getId_endereco() %>" name="numero" maxlength="10" placeholder="Digite número" required pattern="^(\d{1,6}[A-Za-z]?|[Ss]/[Nn])$" value="<%= h(recuperarEdicao ? request.getParameter("numero") : endereco.getNumero()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-bairro-<%= endereco.getId_endereco() %>">Bairro</label>
                                        <input type="text" id="edi-endereco-bairro-<%= endereco.getId_endereco() %>" name="bairro" maxlength="30" placeholder="Digite bairro" required value="<%= h(recuperarEdicao ? request.getParameter("bairro") : endereco.getBairro()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-cidade-<%= endereco.getId_endereco() %>">Cidade</label>
                                        <input type="text" id="edi-endereco-cidade-<%= endereco.getId_endereco() %>" name="cidade" maxlength="30" placeholder="Digite cidade" required data-lista="sugestoes-endereco-cidade-lista" value="<%= h(recuperarEdicao ? request.getParameter("cidade") : endereco.getCidade()) %>">
                                    </div>
                                    <div class="campo">
                                        <label for="edi-endereco-estado-<%= endereco.getId_endereco() %>">Estado (UF)</label>
                                        <input type="text" id="edi-endereco-estado-<%= endereco.getId_endereco() %>" name="estado" maxlength="2" pattern="[A-Za-z]{2}" placeholder="Ex: SP" required value="<%= h(recuperarEdicao ? request.getParameter("estado") : endereco.getEstado()) %>">
                                    </div>
                                    <div class="campo campo-largo">
                                        <label for="edi-endereco-complemento-<%= endereco.getId_endereco() %>">Complemento</label>
                                        <textarea id="edi-endereco-complemento-<%= endereco.getId_endereco() %>" name="complemento" rows="3" maxlength="2000"><%= h(recuperarEdicao ? request.getParameter("complemento") : endereco.getComplemento()) %></textarea>
                                    </div>
                                    <div class="botoes-modal">
                                        <a href="#endereco" class="botao botao-cancelar">Cancelar</a>
                                        <button type="submit" class="botao botao-principal">Confirmar edição</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>

                    <% for (Endereco endereco : enderecos) { %>
<section id="excluir-endereco-<%= endereco.getId_endereco() %>" class="pagina modal-pagina">
                        <div class="modal-fundo">
                            <div class="modal modal-exclusao">
<p><%= h(endereco.getNome_empresa() == null || endereco.getNome_empresa().isBlank() ? "—" : endereco.getNome_empresa()) %></p>
                                <h2>Excluir endereço</h2>
                                <p>Tem certeza que deseja excluir este endereço?</p>
                                <form method="post" action="<%= request.getContextPath() %>/endereco-servlet">
                                    <input type="hidden" name="acao" value="excluir">

                                    <input type="hidden" name="id-endereco" value="<%= endereco.getId_endereco() %>">
                                    <div class="botoes-modal">
                                    <a href="#endereco" class="botao botao-cancelar">Cancelar</a>
                                    <button type="submit" class="botao botao-excluir">Confirmar exclusão</button>
                                </div>
                                </form>
                            </div>
                        </div>
                    </section>
<% } %>
                </section>

<div id="sugestoes-endereco-cidade-lista" class="lista-escolhas" role="listbox" hidden>
<% for (String sugestao : cidadeList) { %>
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
            </main>
        </div>
    </body>
</html>
