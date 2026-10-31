document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("config-foto-arquivo");
    const preview = document.getElementById("config-foto-preview");
    const imagemUrl = document.getElementById("config-imagem-url");
    const status = document.getElementById("config-foto-status");
    if (!input || !preview || !imagemUrl || !status) return;

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

        status.textContent = "Preparando envio…";
        input.disabled = true;

        try {
            const contexto = document.body.dataset.contextPath || "";
            const respostaAssinatura = await fetch(`${contexto}/cloudinary-signature`, {
                headers: { "Accept": "application/json" }
            });
            const assinatura = await respostaAssinatura.json();
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
            const resultado = await resposta.json();
            if (!resposta.ok || !resultado.secure_url) {
                throw new Error(resultado.error?.message || "Não foi possível enviar a imagem.");
            }
            imagemUrl.value = resultado.secure_url;
            preview.src = resultado.secure_url.replace(
                "/image/upload/",
                "/image/upload/c_fill,w_256,h_256,q_auto,f_auto/"
            );
            status.textContent = "Imagem enviada. Confirme a edição do perfil para salvar.";
        } catch (erro) {
            status.textContent = erro.message || "Falha ao enviar a imagem. Tente novamente.";
            input.value = "";
        } finally {
            input.disabled = false;
        }
    });
});
