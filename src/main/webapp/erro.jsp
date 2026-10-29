<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    // Escape de texto e atributos HTML, inclusive valores vindos do request.
    private String h(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private <T> java.util.List<T> lista(jakarta.servlet.http.HttpServletRequest req,
                                       String atributo, Class<T> tipo) {
        java.util.List<T> resultado = new java.util.ArrayList<>();
        Object valor = req.getAttribute(atributo);
        if (valor instanceof java.util.List<?>) {
            for (Object item : (java.util.List<?>) valor) {
                if (tipo.isInstance(item)) resultado.add(tipo.cast(item));
            }
        }
        return resultado;
    }

    private String parametroFiltro(jakarta.servlet.http.HttpServletRequest req, String nome) {
        // Em POST, campos como status pertencem ao cadastro/edição, não aos filtros.
        if (!"GET".equalsIgnoreCase(req.getMethod())) return "";
        String valor = req.getParameter(nome);
        return valor == null ? "" : valor;
    }

    private Object valorFormulario(jakarta.servlet.http.HttpServletRequest req,
                                   String nome, Object atual, boolean recuperar) {
        return recuperar ? req.getParameter(nome) : atual;
    }

    private String selecionado(Object atual, Object opcao) {
        String valor = atual == null ? "" : atual.toString();
        return valor.equals(opcao == null ? "" : opcao.toString()) ? "selected" : "";
    }

    private String exibir(Object valor) {
        if (valor == null || valor.toString().isBlank()) return "—";
        if (valor instanceof Boolean) return Boolean.TRUE.equals(valor) ? "Sim" : "Não";
        if (valor instanceof java.sql.Date) {
            return ((java.sql.Date) valor).toLocalDate()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (valor instanceof java.time.OffsetDateTime) {
            return ((java.time.OffsetDateTime) valor).atZoneSameInstant(java.time.ZoneId.of("America/Sao_Paulo"))
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        if (valor instanceof Enum<?>) {
            return switch (((Enum<?>) valor).name()) {
                case "NAO_INICIADO" -> "Não iniciado";
                case "INICIADO" -> "Iniciado";
                case "FINALIZADO" -> "Finalizado";
                case "ATIVA" -> "Ativa";
                case "INATIVA" -> "Inativa";
                case "EM_ANALISE" -> "Em análise";
                case "EM_RESOLUCAO" -> "Em resolução";
                case "RESOLVIDO" -> "Resolvido";
                case "ABAIXO_DO_ESPERADO" -> "Abaixo do esperado";
                case "ACIMA_DO_ESPERADO" -> "Acima do esperado";
                case "REGULAR" -> "Regular";
                case "ALTO" -> "Alto";
                case "MEDIO" -> "Médio";
                case "BAIXO" -> "Baixo";
                case "GRANDE" -> "Grande";
                case "MEDIA" -> "Média";
                case "PEQUENA" -> "Pequena";
                default -> valor.toString();
            };
        }
        return valor.toString();
    }

    private String urlOrdem(jakarta.servlet.http.HttpServletRequest req, String servlet,
                             String coluna, String[] filtros) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append(servlet).append('?');
        for (String filtro : filtros) {
            if ("ordem".equals(filtro) || "direcao".equals(filtro)) continue;
            String valor = parametroFiltro(req, filtro);
            if (!valor.isBlank()) url.append(filtro).append('=')
                    .append(java.net.URLEncoder.encode(valor, java.nio.charset.StandardCharsets.UTF_8)).append('&');
        }
        String direcao = coluna.equals(parametroFiltro(req, "ordem"))
                && !"DESC".equalsIgnoreCase(parametroFiltro(req, "direcao")) ? "DESC" : "ASC";
        return url.append("ordem=").append(coluna).append("&direcao=").append(direcao).toString();
    }

    private String triangulo(jakarta.servlet.http.HttpServletRequest req, String coluna) {
        if (!coluna.equals(parametroFiltro(req, "ordem"))) return "";
        return "DESC".equalsIgnoreCase(parametroFiltro(req, "direcao")) ? " ▼" : " ▲";
    }

    private String ariaOrdem(jakarta.servlet.http.HttpServletRequest req, String coluna) {
        if (!coluna.equals(parametroFiltro(req, "ordem"))) return "none";
        return "DESC".equalsIgnoreCase(parametroFiltro(req, "direcao")) ? "descending" : "ascending";
    }
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/assets/images/logo_compacta.svg" type="image/x-icon">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/erro.css">
    <title>Erro</title>
</head>
<body>

    <main class="erro-pagina">

        <img src="<%= request.getContextPath() %>/assets/images/logo_oficial.svg" alt="ACTA" class="erro-logo">

        <section class="erro-conteudo">
            <h1>Oops!</h1>
            <div class="gato-wrapper">
                <img src="<%= request.getContextPath() %>/assets/images/cato_triste.svg" alt="Gato triste ao lado de uma espinha de peixe" class="erro-imagem">
                <div class="sombra"></div>
            </div>
            <p role="alert"><%= h(request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_MESSAGE) == null ? "Não foi possível concluir a operação." : request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_MESSAGE)) %></p><p><a href="<%= request.getContextPath() %>/empresa-servlet">Voltar aos CRUDs</a></p>    
        </section>

    </main>

</body>
</html>