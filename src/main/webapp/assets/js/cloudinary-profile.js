document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("config-foto-arquivo");
    const preview = document.getElementById("config-foto-preview");
    const imagemUrl = document.getElementById("config-imagem-url");
    const status = document.getElementById("config-foto-status");
    if (!input || !preview || !imagemUrl || !status) return;
    const salvarFoto = input.closest("form")?.querySelector("[data-salvar-foto]");

    async function lerRespostaJson(resposta, mensagemPadrao, verificarSessao = false) {
        if (verificarSessao && resposta.status === 401) {
            throw new Error("Sua sessão expirou. Atualize a página e entre novamente para enviar a foto.");
        }
        if (verificarSessao && resposta.status === 403) {
            throw new Error("Não foi possível autorizar o envio. Atualize a página e tente novamente.");
        }
        const tipo = resposta.headers.get("content-type") || "";
        if (!tipo.toLowerCase().includes("application/json")) {
            throw new Error(mensagemPadrao);
        }
        try {
            return await resposta.json();
        } catch {
            throw new Error(mensagemPadrao);
        }
    }

    document.querySelectorAll('[data-escolher-foto]').forEach(function (botao) {
        botao.addEventListener('click', function () {
            const editar = document.getElementById('config-editar-perfil');
            if (editar) editar.checked = true;
            input.click();
        });
    });
    input.addEventListener("change", async () => {
        const arquivo = input.files && input.files[0];
        if (!arquivo) return;

        if (!/^image\/(png|jpeg|webp)$/.test(arquivo.type)) {
            status.textContent = "Escolha uma imagem PNG, JPEG ou WebP.";
            input.value = "";
            return;
        }
        if (arquivo.size > 5 * 1024 * 1024) {
            status.textContent = "A imagem deve ter no máximo 5 MB.";
            input.value = "";
            return;
        }

        if (salvarFoto) salvarFoto.disabled = true;
        status.textContent = "Preparando envio…";
        input.disabled = true;

        try {
            const contexto = document.body.dataset.contextPath || "";
            const tokenCsrf = document.querySelector('meta[name="csrf-token"]')?.content;
            if (!tokenCsrf) throw new Error("Token de segurança ausente. Atualize a página.");
            const respostaAssinatura = await fetch(`${contexto}/cloudinary-signature`, {
                method: "POST",
                headers: { "Accept": "application/json", "X-CSRF-Token": tokenCsrf }
            });
            const assinatura = await lerRespostaJson(respostaAssinatura,
                "O serviço de fotos está temporariamente indisponível. Tente novamente em instantes.", true);
            if (!respostaAssinatura.ok) {
                throw new Error(assinatura.error || "Não foi possível preparar o envio da imagem.");
            }

            const formData = new FormData();
            formData.append("file", arquivo);
            formData.append("api_key", assinatura.apiKey);
            formData.append("timestamp", assinatura.timestamp);
            formData.append("folder", assinatura.folder);
            formData.append("signature", assinatura.signature);
            status.textContent = "Enviando imagem…";

            const resposta = await fetch(`https://api.cloudinary.com/v1_1/${encodeURIComponent(assinatura.cloudName)}/image/upload`, {
                method: "POST",
                body: formData
            });
            const resultado = await lerRespostaJson(resposta,
                "O serviço de imagens não respondeu como esperado. Tente novamente em instantes.");
            if (!resposta.ok || !resultado.secure_url) {
                throw new Error(resultado.error?.message || "Não foi possível enviar a imagem.");
            }
            imagemUrl.value = resultado.secure_url;
            preview.src = resultado.secure_url.replace(
                "/image/upload/",
                "/image/upload/c_fill,w_256,h_256,q_auto,f_auto/"
            );
            if (salvarFoto) salvarFoto.disabled = false;
            status.textContent = salvarFoto ? "Imagem enviada. Clique em Salvar foto para concluir." : "Imagem enviada. Confirme a edição do perfil para salvar.";
        } catch (erro) {
            status.textContent = erro.message || "Falha ao enviar a imagem. Tente novamente.";
            input.value = "";
        } finally {
            input.disabled = false;
        }
    });
});
