<%-- Perfil independente acessado pela landing page. --%>
<% request.setAttribute("perfilSeparado", Boolean.TRUE); %>
<%@ include file="/WEB-INF/includes/perfil-administrador.jspf" %>
