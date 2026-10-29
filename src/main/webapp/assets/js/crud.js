// Filtros e ordenação dos HTMLs; nas JSPs, os links de ordenação vêm do servidor.
const formularioFiltros = document.querySelector('form[method="get"]');
const painelFiltros = document.querySelector('.filtros-painel');
const botaoFiltros = document.querySelector('[data-abrir-filtros]');
const modalFiltros = painelFiltros.querySelector('.modal-filtros');
const colunas = document.querySelectorAll('[data-ordenar]');
const ordem = formularioFiltros.elements.namedItem('ordem');
const direcao = formularioFiltros.elements.namedItem('direcao');
const parametros = new URLSearchParams(window.location.search);
for (const campo of formularioFiltros.elements) {
    if (campo.name && parametros.has(campo.name)) campo.value = parametros.get(campo.name);
}

function posicionarFiltros() {
    if (painelFiltros.hidden) return;
    const viewport = window.visualViewport;
    const largura = viewport ? viewport.width : window.innerWidth;
    const altura = viewport ? viewport.height : window.innerHeight;
    const origemX = viewport ? viewport.offsetLeft : 0;
    const origemY = viewport ? viewport.offsetTop : 0;
    const botao = botaoFiltros.getBoundingClientRect();
    const abaixo = origemY + altura - botao.bottom - 20;
    painelFiltros.style.width = Math.max(0, Math.min(420, largura - 24)) + 'px';
    modalFiltros.style.maxHeight = Math.max(0, abaixo >= 180 ? abaixo : altura - 24) + 'px';
    painelFiltros.style.left = Math.max(origemX + 12, Math.min(botao.left, origemX + largura - painelFiltros.offsetWidth - 12)) + 'px';
    painelFiltros.style.top = Math.max(origemY + 12, Math.min(botao.bottom + 8, origemY + altura - painelFiltros.offsetHeight - 12)) + 'px';
}

function exibirFiltros(abrir, devolverFoco = false) {
    painelFiltros.hidden = !abrir;
    botaoFiltros.setAttribute('aria-expanded', String(abrir));
    posicionarFiltros();
    if (devolverFoco) botaoFiltros.focus();
}

function mostrarOrdenacao() {
    for (const coluna of colunas) {
        coluna.closest('th').setAttribute('aria-sort', coluna.dataset.ordenar === ordem.value
            ? (direcao.value === 'DESC' ? 'descending' : 'ascending') : 'none');
    }
}

// Um único evento atende aos botões, às colunas e ao clique fora do painel.
document.addEventListener('click', evento => {
    const botao = evento.target.closest('button');
    if (botao === botaoFiltros) exibirFiltros(painelFiltros.hidden);
    else if (botao && botao.hasAttribute('data-fechar-filtros')) exibirFiltros(false, true);
    else if (!painelFiltros.contains(evento.target)) exibirFiltros(false);

    if (botao && botao.hasAttribute('data-ordenar')) {
        direcao.value = ordem.value === botao.dataset.ordenar && direcao.value === 'ASC' ? 'DESC' : 'ASC';
        ordem.value = botao.dataset.ordenar;
        mostrarOrdenacao();
        formularioFiltros.requestSubmit();
    }
});
document.addEventListener('keydown', evento => {
    if (evento.key === 'Escape' && !painelFiltros.hidden) exibirFiltros(false, true);
});
for (const evento of ['resize', 'scroll']) {
    window.addEventListener(evento, posicionarFiltros, true);
    if (window.visualViewport) window.visualViewport.addEventListener(evento, posicionarFiltros);
}
formularioFiltros.addEventListener('invalid', () => exibirFiltros(true), true);
mostrarOrdenacao();

// Reabre o formulário identificado pelo JSP quando há erro de validação.
const modalPendente = document.body.dataset.reabrir;
if (modalPendente && document.getElementById(modalPendente)) window.location.hash = modalPendente;
