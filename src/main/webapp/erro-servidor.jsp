<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erro temporário | ACTA</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/erro.css">
</head>
<body>
    <main class="erro-pagina">
        <section class="erro-conteudo" role="alert">
            <h1>Não foi possível concluir</h1>
            <p>Ocorreu um erro inesperado. Tente novamente em alguns instantes.</p>
            <a class="erro-voltar" href="<%= request.getContextPath() %>/index.html">Voltar ao início</a>
        </section>
    </main>
</body>
</html>
