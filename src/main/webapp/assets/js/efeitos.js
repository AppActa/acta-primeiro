(function () {
    'use strict';

    document.querySelectorAll('.botao-principal, .botao-excluir, button.botao[type="submit"]').forEach(function (botao) {
        botao.classList.add('botao-animado');
    });

    var SELETOR_CAMPO = 'input:not([type=checkbox]):not([type=radio]):not([type=file]):not([type=hidden]):not([type=button]):not([type=submit]), select, textarea';

    document.addEventListener('focusin', function (evento) {
        if (!evento.target.matches || !evento.target.matches(SELETOR_CAMPO)) return;
        var campo = evento.target.closest('.campo, .config-campo, .pesquisa');
        var formulario = evento.target.closest('form');
        if (campo) campo.classList.add('campo-ativo');
        if (formulario) formulario.classList.add('form-ativo');
    });

    document.addEventListener('focusout', function (evento) {
        var campo = evento.target.closest ? evento.target.closest('.campo, .config-campo, .pesquisa') : null;
        var formulario = evento.target.closest ? evento.target.closest('form') : null;
        if (campo) campo.classList.remove('campo-ativo');
        if (formulario && !(evento.relatedTarget && formulario.contains(evento.relatedTarget))) {
            formulario.classList.remove('form-ativo');
        }
    });

    var chaveEdicao = document.getElementById('config-editar-perfil');
    var chaveSenha = document.getElementById('config-modal-senha');

    if (chaveEdicao) {
        chaveEdicao.addEventListener('change', function () {
            if (chaveEdicao.checked) {
                var primeiro = document.querySelector('.config-dados input[name="nome"]');
                if (primeiro) setTimeout(function () { primeiro.focus(); }, 60);
                return;
            }
            document.querySelectorAll('.config-dados [data-original]').forEach(function (campo) {
                campo.value = campo.dataset.original;
                campo.dispatchEvent(new Event('input', { bubbles: true }));
            });
        });
    }

    if (chaveSenha) {
        chaveSenha.addEventListener('change', function () {
            var formulario = document.querySelector('.config-modal form');
            if (chaveSenha.checked) {
                var antiga = document.getElementById('alt-senha-antiga');
                if (antiga) setTimeout(function () { antiga.focus(); }, 60);
            } else if (formulario) {
                formulario.reset();
            }
        });
    }

    document.addEventListener('keydown', function (evento) {
        if (evento.key !== 'Escape') return;
        if (chaveSenha && chaveSenha.checked) {
            chaveSenha.checked = false;
            chaveSenha.dispatchEvent(new Event('change'));
        } else if (chaveEdicao && chaveEdicao.checked) {
            chaveEdicao.checked = false;
            chaveEdicao.dispatchEvent(new Event('change'));
        }
    });

    document.querySelectorAll('input[data-confirma]').forEach(function (confirmacao) {
        var original = document.getElementById(confirmacao.dataset.confirma);
        if (!original) return;
        function conferir() {
            var diferente = confirmacao.value !== '' && confirmacao.value !== original.value;
            confirmacao.setCustomValidity(diferente ? 'As senhas não conferem.' : '');
        }
        confirmacao.addEventListener('input', conferir);
        original.addEventListener('input', conferir);
    });
})();