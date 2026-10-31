(function () {
    'use strict';

    var semMovimento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var temMouse = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

    // Botões de confirmação: seguem o mouse, ganham brilho e "ondinha" no clique.
    //  Os demais botões (cancelar, filtros...) só flutuam de leve, via CSS.
    var botoes = document.querySelectorAll('.botao-principal, .botao-excluir, button.botao[type="submit"]');
    botoes.forEach(function (botao) {
        botao.classList.add('botao-animado');
    });

    if (!semMovimento && temMouse) {
        var FORCA = 0.22;
        var LIMITE = 7;

        document.addEventListener('pointermove', function (evento) {
            var botao = evento.target.closest ? evento.target.closest('.botao-animado') : null;
            if (!botao) return;
            var caixa = botao.getBoundingClientRect();
            var x = evento.clientX - caixa.left;
            var y = evento.clientY - caixa.top;
            var dx = (x - caixa.width / 2) * FORCA;
            var dy = (y - caixa.height / 2) * FORCA;
            botao.style.setProperty('--mx', Math.max(-LIMITE, Math.min(LIMITE, dx)).toFixed(1) + 'px');
            botao.style.setProperty('--my', Math.max(-LIMITE, Math.min(LIMITE, dy)).toFixed(1) + 'px');
            botao.style.setProperty('--brilho-x', ((x / caixa.width) * 100).toFixed(0) + '%');
            botao.style.setProperty('--brilho-y', ((y / caixa.height) * 100).toFixed(0) + '%');
        });

        document.addEventListener('pointerout', function (evento) {
            var botao = evento.target.closest ? evento.target.closest('.botao-animado') : null;
            if (!botao || (evento.relatedTarget && botao.contains(evento.relatedTarget))) return;
            botao.style.removeProperty('--mx');
            botao.style.removeProperty('--my');
        });
    }

    if (!semMovimento) {
        document.addEventListener('pointerdown', function (evento) {
            var botao = evento.target.closest ? evento.target.closest('.botao-animado') : null;
            if (!botao) return;
            var caixa = botao.getBoundingClientRect();
            var tamanho = Math.max(caixa.width, caixa.height) * 2;
            var onda = document.createElement('span');
            onda.className = 'onda';
            onda.style.width = onda.style.height = tamanho + 'px';
            onda.style.left = (evento.clientX - caixa.left - tamanho / 2) + 'px';
            onda.style.top = (evento.clientY - caixa.top - tamanho / 2) + 'px';
            botao.appendChild(onda);
            onda.addEventListener('animationend', function () { onda.remove(); });
        });
    }

    // Campos e formulários: o campo escolhido aumenta a borda e brilha;
    // o formulário (ou o cartão) onde ele está também ganha um brilho suave.
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
        // só apaga o brilho do formulário se o foco realmente saiu dele
        if (formulario && !(evento.relatedTarget && formulario.contains(evento.relatedTarget))) {
            formulario.classList.remove('form-ativo');
        }
    });

    // Configurações: cancelar edição, fechar modal, confirmar senha
    var chaveEdicao = document.getElementById('config-editar-perfil');
    var chaveSenha = document.getElementById('config-modal-senha');

    // Ao cancelar a edição, os campos voltam para os dados salvos no banco.
    if (chaveEdicao) {
        chaveEdicao.addEventListener('change', function () {
            if (chaveEdicao.checked) {
                var primeiro = document.querySelector('.config-dados input[name="nome"]');
                if (primeiro) setTimeout(function () { primeiro.focus(); }, 60);
                return;
            }
            document.querySelectorAll('.config-dados [data-original]').forEach(function (campo) {
                campo.value = campo.dataset.original;
                campo.dispatchEvent(new Event('input', { bubbles: true }));   // reaplica a máscara do telefone
            });
            document.querySelectorAll('.config-dados .mensagem-erro').forEach(function (m) { m.remove(); });
        });
    }

    // Ao fechar o modal de senha, os campos são limpos.
    if (chaveSenha) {
        chaveSenha.addEventListener('change', function () {
            var formulario = document.querySelector('.config-modal form');
            if (chaveSenha.checked) {
                var antiga = document.getElementById('alt-senha-antiga');
                if (antiga) setTimeout(function () { antiga.focus(); }, 60);
            } else if (formulario) {
                formulario.reset();
                document.querySelectorAll('.config-modal .mensagem-erro').forEach(function (m) { m.remove(); });
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

    // Campo "confirmar": só aceita o envio se for igual ao campo indicado em data-confirma.
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

    // Mensagem de sucesso some sozinha depois de alguns segundos.
    document.querySelectorAll('.mensagem[data-autoocultar]').forEach(function (mensagem) {
        setTimeout(function () {
            mensagem.classList.add('mensagem-saindo');
            setTimeout(function () { mensagem.remove(); }, 600);
        }, 5000);
    });
})();