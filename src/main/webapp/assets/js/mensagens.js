// Mantém as mensagens de validação junto ao campo, inclusive sem enviar o formulário.
document.querySelectorAll('form').forEach(function (formulario) {
    function associar(campo, mensagem) {
        campo.setAttribute('aria-invalid', 'true');
        const descricoes = (campo.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (!descricoes.includes(mensagem.id)) descricoes.push(mensagem.id);
        campo.setAttribute('aria-describedby', descricoes.join(' '));
    }
    formulario.querySelectorAll('.campo-erro').forEach(function (mensagem) {
        const campo = mensagem.parentElement.querySelector('input:not([type="hidden"]), select, textarea');
        if (campo) associar(campo, mensagem);
    });
    formulario.addEventListener('invalid', function (evento) {
        evento.preventDefault();
        const campo = evento.target;
        let mensagem = campo.parentElement.querySelector('.campo-erro');
        if (!mensagem) {
            mensagem = document.createElement('small');
            mensagem.className = 'campo-erro';
            mensagem.id = 'erro-' + campo.id;
            mensagem.setAttribute('role', 'alert');
            campo.insertAdjacentElement('afterend', mensagem);
        }
        mensagem.textContent = campo.validationMessage;
        associar(campo, mensagem);
    }, true);
    formulario.addEventListener('input', function (evento) {
        const campo = evento.target;
        if (!campo.matches('input, select, textarea')) return;
        const mensagem = campo.parentElement.querySelector('.campo-erro');
        if (mensagem) mensagem.remove();
        campo.removeAttribute('aria-invalid');
        const descricoes = (campo.getAttribute('aria-describedby') || '').split(/\s+/)
            .filter(function (id) { return id && !id.startsWith('erro-'); });
        if (descricoes.length) campo.setAttribute('aria-describedby', descricoes.join(' '));
        else campo.removeAttribute('aria-describedby');
    });
});
