// Compartilha o mesmo menu recolhível entre perfis, empresa e tabelas CRUD.
document.querySelectorAll('.sidebar').forEach(function (sidebar) {
    const botao = sidebar.querySelector('#menuToggle');
    const menu = sidebar.querySelector('#nav');
    if (!botao || !menu) return;

    function abrir(aberto) {
        menu.classList.toggle('open', aberto);
        botao.setAttribute('aria-expanded', String(aberto));
        botao.setAttribute('aria-label', aberto ? 'Fechar menu' : 'Abrir menu');
    }

    botao.addEventListener('click', function () {
        abrir(botao.getAttribute('aria-expanded') !== 'true');
    });
    sidebar.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape' && menu.classList.contains('open')) {
            abrir(false);
            botao.focus();
        }
    });
    menu.addEventListener('click', function (evento) {
        if (evento.target.closest('a')) abrir(false);
    });
    window.matchMedia('(max-width: 760px)').addEventListener('change', function () {
        abrir(false);
    });
});
