// Mantém a sequência dos ENUMs declarados no script do banco.
const sequenciasOrdenacao = {
    prioridade: ['Alto', 'Médio', 'Baixo'],
    peso: ['Alto', 'Médio', 'Baixo'],
    severidade: ['Alto', 'Médio', 'Baixo'],
    tamanho: ['Grande', 'Pequena', 'Média'],
    etapa: ['PLAN', 'DO', 'CHECK', 'ACT'],
    fase: ['PLAN', 'DO', 'CHECK', 'ACT'],
    fase_origem: ['PLAN', 'DO', 'CHECK', 'ACT'],
    permissao: ['Não', 'Sim'],
    status: ['Ativa', 'Inativa', 'Não iniciado', 'Iniciado', 'Finalizado',
        'Abaixo do esperado', 'Regular', 'Acima do esperado',
        'Em análise', 'Em resolução', 'Resolvido']
};
const camposDataOrdenacao = new Set(['inicio', 'fim', 'entrega', 'prazo', 'contratacao', 'criado_em', 'encontrado_em']);
const comparadorTextoOrdenacao = new Intl.Collator('pt-BR', { sensitivity: 'base' });

function valorOrdenacao(celula, campo) {
    const texto = celula.textContent.trim();
    if (!texto || texto === '—') return null;
    if (campo === 'id') return BigInt(texto);
    if (camposDataOrdenacao.has(campo)) {
        // dd/MM/yyyy e dd/MM/yyyy HH:mm: compara ano antes de mês e dia.
        const data = /^(\d{2})\/(\d{2})\/(\d{4})(?:\s+(\d{2}):(\d{2}))?$/.exec(texto);
        if (data) return Number(data[3] + data[2] + data[1] + (data[4] || '00') + (data[5] || '00'));
    }
    const posicao = sequenciasOrdenacao[campo]?.indexOf(texto);
    if (posicao !== undefined && posicao >= 0) return posicao;
    return texto;
}

function compararValoresOrdenacao(a, b) {
    // Como no PostgreSQL: nulos no fim em ASC e no início em DESC.
    if (a === b) return 0;
    if (a === null) return 1;
    if (b === null) return -1;
    if (typeof a === 'string' && typeof b === 'string') return comparadorTextoOrdenacao.compare(a, b);
    return a < b ? -1 : a > b ? 1 : 0;
}

// Ordena as linhas já carregadas sem enviar os formulários dos cabeçalhos.
for (const formulario of document.querySelectorAll('thead th.coluna-ordenavel form')) {
    const campo = formulario.elements.namedItem('ordem')?.value;
    if (!campo) continue;
    formulario.addEventListener('submit', evento => {
        const tabela = formulario.closest('table');
        const corpo = tabela.tBodies[0];
        if (!corpo) return;
        const linhas = Array.from(corpo.rows);
        // A linha de "nenhum registro" ocupa várias colunas e não tem ID.
        const registros = linhas.filter(linha => linha.cells[0]?.colSpan === 1);
        const coluna = formulario.closest('th');
        const indice = coluna.cellIndex;
        const valores = new Map(registros.map(linha => [linha, valorOrdenacao(linha.cells[indice], campo)]));
        evento.preventDefault();
        const crescente = coluna.getAttribute('aria-sort') !== 'ascending';
        registros.sort((a, b) => {
            const comparacao = compararValoresOrdenacao(valores.get(a), valores.get(b));
            return crescente ? comparacao : -comparacao;
        });
        const fragmento = document.createDocumentFragment();
        for (const linha of registros) fragmento.appendChild(linha);
        corpo.appendChild(fragmento);

        for (const cabecalho of tabela.querySelectorAll('thead th.coluna-ordenavel')) {
            cabecalho.setAttribute('aria-sort', 'none');
            cabecalho.querySelectorAll('.botao-ordenacao span[aria-hidden]').forEach(seta => seta.remove());
            const direcaoColuna = cabecalho.querySelector('input[name="direcao"]');
            if (direcaoColuna) direcaoColuna.value = 'ASC';
        }
        coluna.setAttribute('aria-sort', crescente ? 'ascending' : 'descending');
        formulario.elements.namedItem('direcao').value = crescente ? 'DESC' : 'ASC';
        const seta = document.createElement('span');
        seta.setAttribute('aria-hidden', 'true');
        seta.textContent = crescente ? ' ▲' : ' ▼';
        formulario.querySelector('.botao-ordenacao').appendChild(seta);
        // Mantém a escolha quando uma busca ou filtro recarregar os dados.
        const filtros = document.querySelector('.filtros-painel form');
        if (filtros?.elements.namedItem('ordem')) filtros.elements.namedItem('ordem').value = campo;
        if (filtros?.elements.namedItem('direcao')) filtros.elements.namedItem('direcao').value = crescente ? 'ASC' : 'DESC';
    });
}

// As buscas e os filtros continuam consultando o servidor.
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
    else if (!painelFiltros.contains(evento.target) && !evento.target.closest('.lista-escolhas')) exibirFiltros(false);

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

// As opções desta lista já foram geradas pelo for no JSP.
let campoDaLista = null;
let listaAberta = null;
let indiceDaOpcao = -1;
let numeroDaOpcao = 0;
for (const opcao of document.querySelectorAll('.lista-escolhas button')) {
    opcao.id = 'opcao-crud-' + numeroDaOpcao++;
    opcao.tabIndex = -1;
}

function fecharLista() {
    if (listaAberta) listaAberta.hidden = true;
    if (campoDaLista) {
        campoDaLista.setAttribute('aria-expanded', 'false');
        campoDaLista.removeAttribute('aria-activedescendant');
    }
    listaAberta = null;
    campoDaLista = null;
}

function mostrarLista(campo, filtrar) {
    fecharLista();
    campoDaLista = campo;
    listaAberta = document.getElementById(campo.dataset.lista);
    indiceDaOpcao = -1;
    let encontradas = 0;
    for (const opcao of listaAberta.querySelectorAll('button')) {
        const limpar = opcao.hasAttribute('data-limpar');
        opcao.hidden = limpar ? campo.required
            : filtrar && !opcao.textContent.toLocaleLowerCase().includes(campo.value.trim().toLocaleLowerCase());
        if (!limpar && !opcao.hidden) encontradas++;
        opcao.classList.remove('opcao-ativa');
        opcao.setAttribute('aria-selected', String(!limpar && opcao.textContent.trim() === campo.value));
    }
    listaAberta.querySelector('.lista-vazia').hidden = encontradas > 0;
    listaAberta.hidden = false;
    campo.setAttribute('aria-expanded', 'true');

    posicionarLista();
}

function posicionarLista() {
    if (!listaAberta || !campoDaLista) return;
    const posicao = campoDaLista.getBoundingClientRect();
    const viewport = window.visualViewport;
    const esquerda = viewport ? viewport.offsetLeft : 0;
    const topo = viewport ? viewport.offsetTop : 0;
    const largura = viewport ? viewport.width : window.innerWidth;
    const altura = viewport ? viewport.height : window.innerHeight;
    const abaixo = topo + altura - posicao.bottom - 12;
    const acima = posicao.top - topo - 12;
    listaAberta.style.width = Math.min(posicao.width, largura - 16) + 'px';
    listaAberta.style.maxHeight = Math.max(0, Math.min(220, Math.max(acima, abaixo))) + 'px';
    listaAberta.style.left = Math.max(esquerda + 8, Math.min(posicao.left, esquerda + largura - listaAberta.offsetWidth - 8)) + 'px';
    const inicio = abaixo >= Math.min(150, acima) ? posicao.bottom + 4 : posicao.top - listaAberta.offsetHeight - 4;
    listaAberta.style.top = Math.max(topo + 8, Math.min(inicio, topo + altura - listaAberta.offsetHeight - 8)) + 'px';
}

function validarEscolha(campo, avisar = true) {
    if (!campo.hasAttribute('data-validar-lista')) return;
    const destino = campo.dataset.campoValor ? document.getElementById(campo.dataset.campoValor) : null;
    let escolhida = null;
    for (const opcao of document.getElementById(campo.dataset.lista).querySelectorAll('button[data-valor]')) {
        if (opcao.textContent.trim() !== campo.value) continue;
        if (escolhida && escolhida.dataset.valor !== opcao.dataset.valor) {
            if (destino) destino.value = '';
            campo.setCustomValidity(avisar ? 'Há nomes iguais. Escolha uma opção identificada na lista.' : '');
            return;
        }
        escolhida = opcao;
    }
    if (destino) destino.value = escolhida ? escolhida.dataset.valor : '';
    campo.setCustomValidity(avisar && campo.value && !escolhida ? 'Selecione uma opção da lista.' : '');
}

for (const campo of document.querySelectorAll('input[data-lista]')) {
    const lista = document.getElementById(campo.dataset.lista);
    const destino = campo.dataset.campoValor ? document.getElementById(campo.dataset.campoValor) : null;
    if (destino && destino.value) {
        for (const opcao of lista.querySelectorAll('button[data-valor]')) {
            if (opcao.dataset.valor === destino.value) campo.value = opcao.textContent.trim();
        }
    }
    campo.defaultValue = campo.value;
    campo.setAttribute('role', 'combobox');
    campo.setAttribute('aria-autocomplete', 'list');
    campo.setAttribute('aria-controls', lista.id);
    campo.setAttribute('aria-expanded', 'false');
    if (destino && destino.value && !campo.value) campo.setCustomValidity('O registro vinculado não está disponível. Selecione uma opção.');
    else validarEscolha(campo);

    campo.addEventListener('focus', () => mostrarLista(campo, false));
    campo.addEventListener('click', () => mostrarLista(campo, false));
    campo.addEventListener('input', () => {
        validarEscolha(campo, false);
        mostrarLista(campo, true);
    });
    campo.addEventListener('change', () => validarEscolha(campo));
    campo.addEventListener('blur', () => validarEscolha(campo));
    campo.addEventListener('keydown', evento => {
        if (evento.key === 'Escape' && listaAberta) {
            evento.stopPropagation();
            fecharLista();
        } else if (evento.key === 'ArrowDown' || evento.key === 'ArrowUp' || evento.key === 'Enter') {
            if (evento.key === 'Enter' && !listaAberta) return;
            if (evento.key === 'Enter' && !campo.hasAttribute('data-validar-lista') && indiceDaOpcao < 0) {
                fecharLista();
                return;
            }
            evento.preventDefault();
            if (!listaAberta) mostrarLista(campo, false);
            const opcoes = listaAberta.querySelectorAll('button:not([hidden])');
            if (!opcoes.length) return;
            if (evento.key === 'Enter') {
                const opcao = indiceDaOpcao >= 0 ? opcoes[indiceDaOpcao]
                    : Array.from(opcoes).find(item => !item.hasAttribute('data-limpar') && item.textContent.trim() === campo.value)
                    || Array.from(opcoes).find(item => !item.hasAttribute('data-limpar'));
                if (opcao) opcao.click();
            } else {
                indiceDaOpcao = Math.max(0, Math.min(opcoes.length - 1, indiceDaOpcao + (evento.key === 'ArrowDown' ? 1 : -1)));
                for (const opcao of opcoes) opcao.classList.toggle('opcao-ativa', opcao === opcoes[indiceDaOpcao]);
                campo.setAttribute('aria-activedescendant', opcoes[indiceDaOpcao].id);
                opcoes[indiceDaOpcao].scrollIntoView({ block: 'nearest' });
            }
        }
    });
    campo.form.addEventListener('submit', evento => {
        if (evento.defaultPrevented) return;
        validarEscolha(campo);
        if (!campo.checkValidity()) {
            evento.preventDefault();
            campo.reportValidity();
        }
    });
    campo.form.addEventListener('reset', () => requestAnimationFrame(() => {
        validarEscolha(campo);
        fecharLista();
    }));
}

document.addEventListener('mousedown', evento => {
    if (evento.target.closest('.lista-escolhas button')) evento.preventDefault();
});
document.addEventListener('click', evento => {
    const opcao = evento.target.closest('.lista-escolhas button');
    if (opcao && campoDaLista) {
        const campo = campoDaLista;
        campo.value = opcao.hasAttribute('data-limpar') ? '' : opcao.textContent.trim();
        campo.dispatchEvent(new Event('input', { bubbles: true }));
        campo.dispatchEvent(new Event('change', { bubbles: true }));
        fecharLista();
    } else if (evento.target !== campoDaLista) fecharLista();
});
document.addEventListener('focusin', evento => {
    if (evento.target !== campoDaLista && !listaAberta?.contains(evento.target)) fecharLista();
});
document.addEventListener('scroll', evento => {
    if (!listaAberta?.contains(evento.target)) posicionarLista();
}, true);
window.addEventListener('hashchange', fecharLista);
window.addEventListener('resize', posicionarLista);
if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', posicionarLista);
    window.visualViewport.addEventListener('scroll', posicionarLista);
}
