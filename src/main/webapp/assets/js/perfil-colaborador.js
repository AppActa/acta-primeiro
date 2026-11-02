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
confirmacao.addEventListener('input', conferirSenha);
document.getElementById('newPassword').addEventListener('input', conferirSenha);
document.getElementById('menuToggle').addEventListener('click', function () {
    const aberto = this.getAttribute('aria-expanded') !== 'true';
    this.setAttribute('aria-expanded', String(aberto));
    document.getElementById('nav').classList.toggle('open', aberto);
});
