// Mantém os dados nas etapas do mesmo formulário, sem persistir informações pessoais.
document.querySelectorAll('[data-cadastro]').forEach(function (formulario) {
    const etapas = formulario.querySelectorAll('[data-etapa]');
    const indicador = document.querySelector('[data-etapa-atual]');

    function mostrarEtapa(numero, focar) {
        etapas.forEach(function (etapa) {
            const ativa = etapa.dataset.etapa === String(numero);
            etapa.hidden = !ativa;
            etapa.disabled = !ativa;
        });
        indicador.textContent = String(numero);
        if (focar) {
            const campo = formulario.querySelector('fieldset:not([hidden]) input');
            if (campo) campo.focus();
        }
    }

    formulario.querySelector('[data-continuar]').addEventListener('click', function () {
        const campos = etapas[0].querySelectorAll('input, select');
        for (const campo of campos) {
            if (!campo.reportValidity()) return;
        }
        mostrarEtapa(2, true);
    });
    formulario.querySelector('[data-voltar]').addEventListener('click', function () {
        mostrarEtapa(1, true);
    });
    formulario.addEventListener('submit', function (evento) {
        const empresa = evento.submitter && evento.submitter.hasAttribute('data-cadastrar-empresa');
        formulario.elements.acao.value = empresa ? 'empresa' : 'inserir';
        if (!empresa && !etapas[0].hidden) {
            evento.preventDefault();
            formulario.querySelector('[data-continuar]').click();
            return;
        }
        etapas.forEach(function (etapa) { etapa.disabled = false; });
        if (!empresa && !formulario.checkValidity()) {
            evento.preventDefault();
            const campo = formulario.querySelector('input:invalid, select:invalid, textarea:invalid');
            mostrarEtapa(campo.closest('fieldset').dataset.etapa, false);
            campo.reportValidity();
        }
    });
    const contratacao = formulario.elements.namedItem('dt_contratacao');
    if (contratacao) {
        const hoje = new Date();
        contratacao.max = hoje.getFullYear() + '-'
            + String(hoje.getMonth() + 1).padStart(2, '0') + '-'
            + String(hoje.getDate()).padStart(2, '0');
    }
    // Reabre os dados profissionais quando o usuário retorna da tela de empresa.
    mostrarEtapa(location.hash === '#etapa-2' ? 2 : 1, false);
});
