document.querySelectorAll('[data-open]').forEach(function (botao) {
    botao.addEventListener('click', function () { document.getElementById(botao.dataset.open).showModal(); });
});
document.querySelectorAll('[data-close]').forEach(function (botao) {
    botao.addEventListener('click', function () { botao.closest('dialog').close(); });
});
const confirmacao = document.getElementById('confirmPassword');
function conferirSenha() {
    confirmacao.setCustomValidity(confirmacao.value === document.getElementById('newPassword').value ? '' : 'As senhas não coincidem.');
}
if (confirmacao) {
    confirmacao.addEventListener('input', conferirSenha);
    document.getElementById('newPassword').addEventListener('input', conferirSenha);
}


document.querySelectorAll('dialog[data-reabrir]').forEach(function (modal) { modal.showModal(); });
