// Mantém os dados nas etapas do mesmo formulário, sem persistir informações pessoais.
document.querySelectorAll('[data-cadastro]').forEach(function (formulario) {
    const etapas = formulario.querySelectorAll('[data-etapa]');
    const indicador = document.querySelector('[data-etapa-atual]');
    const empresaNome = formulario.querySelector('[data-empresa-nome]');
    const empresaId = formulario.querySelector('[data-empresa-id]');
    if (empresaNome && empresaId) {
        const seletor = empresaNome.closest('[data-empresa-seletor]');
        const lista = seletor.querySelector('[role="listbox"]');
        const vazia = lista.querySelector('[data-empresa-vazia]');
        const opcoes = Array.from(lista.querySelectorAll('button[data-id]'));
        let ativa = -1;
        const selecionada = opcoes.find(opcao => opcao.dataset.id === empresaId.value);
        empresaNome.value = selecionada ? selecionada.dataset.nome : '';
        function atualizarEmpresa() {
            const iguais = opcoes.filter(item => item.dataset.nome === empresaNome.value);
            const opcao = iguais.find(item => item.dataset.id === empresaId.value)
                || (iguais.length === 1 ? iguais[0] : null);
            empresaId.value = opcao ? opcao.dataset.id : '';
            empresaNome.setCustomValidity(empresaNome.value && !opcao
                ? 'Selecione uma empresa da lista.' : '');
            opcoes.forEach(item => item.setAttribute('aria-selected', String(item === opcao)));
        }
        function fecharEmpresas() {
            lista.hidden = true;
            empresaNome.setAttribute('aria-expanded', 'false');
            empresaNome.removeAttribute('aria-activedescendant');
            opcoes.forEach(item => item.classList.remove('opcao-ativa'));
            ativa = -1;
        }
        function mostrarEmpresas(filtrar) {
            const normalizar = texto => texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase();
            const busca = normalizar(empresaNome.value.trim());
            opcoes.forEach(item => {
                item.hidden = filtrar && !normalizar(item.textContent).includes(busca);
                item.classList.remove('opcao-ativa');
            });
            ativa = -1;
            empresaNome.removeAttribute('aria-activedescendant');
            vazia.hidden = opcoes.some(item => !item.hidden);
            lista.hidden = false;
            empresaNome.setAttribute('aria-expanded', 'true');
            posicionarEmpresas();
        }
        function posicionarEmpresas() {
            if (lista.hidden) return;
            const campo = empresaNome.getBoundingClientRect();
            const viewport = window.visualViewport;
            const topo = viewport ? viewport.offsetTop : 0;
            const altura = viewport ? viewport.height : window.innerHeight;
            const abaixo = topo + altura - campo.bottom - 12;
            const acima = campo.top - topo - 12;
            const abrirAcima = abaixo < Math.min(220, lista.scrollHeight) && acima > abaixo;
            lista.classList.toggle('cadastro-empresas-acima', abrirAcima);
            lista.style.maxHeight = Math.max(0, Math.min(220, abrirAcima ? acima : abaixo)) + 'px';
        }
        function escolherEmpresa(opcao) {
            empresaNome.value = opcao.dataset.nome;
            empresaId.value = opcao.dataset.id;
            atualizarEmpresa();
            fecharEmpresas();
        }
        empresaNome.addEventListener('focus', () => mostrarEmpresas(false));
        empresaNome.addEventListener('click', () => mostrarEmpresas(false));
        empresaNome.addEventListener('input', () => {
            empresaId.value = '';
            atualizarEmpresa();
            mostrarEmpresas(true);
        });
        empresaNome.addEventListener('change', atualizarEmpresa);
        empresaNome.addEventListener('blur', atualizarEmpresa);
        empresaNome.addEventListener('keydown', evento => {
            if (evento.key === 'Escape') { fecharEmpresas(); return; }
            if (evento.key === 'Tab') { fecharEmpresas(); return; }
            if (!['ArrowDown', 'ArrowUp', 'Enter'].includes(evento.key)) return;
            if (evento.key === 'Enter' && lista.hidden) return;
            evento.preventDefault();
            if (lista.hidden) mostrarEmpresas(false);
            const visiveis = opcoes.filter(item => !item.hidden);
            if (!visiveis.length) return;
            if (evento.key === 'Enter') {
                escolherEmpresa(visiveis[ativa >= 0 ? ativa : 0]);
                return;
            }
            ativa = ativa < 0 ? (evento.key === 'ArrowDown' ? 0 : visiveis.length - 1)
                : Math.max(0, Math.min(visiveis.length - 1, ativa + (evento.key === 'ArrowDown' ? 1 : -1)));
            opcoes.forEach(item => item.classList.toggle('opcao-ativa', item === visiveis[ativa]));
            empresaNome.setAttribute('aria-activedescendant', visiveis[ativa].id);
            visiveis[ativa].scrollIntoView({ block: 'nearest' });
        });
        lista.addEventListener('mousedown', evento => evento.preventDefault());
        lista.addEventListener('click', evento => {
            const opcao = evento.target.closest('button[data-id]');
            if (opcao) escolherEmpresa(opcao);
        });
        document.addEventListener('click', evento => { if (!seletor.contains(evento.target)) fecharEmpresas(); });
        document.addEventListener('focusin', evento => { if (!seletor.contains(evento.target)) fecharEmpresas(); });
        window.addEventListener('resize', posicionarEmpresas);
        document.addEventListener('scroll', evento => { if (!lista.contains(evento.target)) posicionarEmpresas(); }, true);
        if (window.visualViewport) {
            window.visualViewport.addEventListener('resize', posicionarEmpresas);
            window.visualViewport.addEventListener('scroll', posicionarEmpresas);
        }
        formulario.addEventListener('reset', () => requestAnimationFrame(() => {
            const inicial = opcoes.find(item => item.dataset.id === empresaId.value);
            empresaNome.value = inicial ? inicial.dataset.nome : '';
            atualizarEmpresa();
            fecharEmpresas();
        }));
        atualizarEmpresa();
    }

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
    const erroServidor = formulario.querySelector('.campo-erro');
    const etapaComErro = erroServidor && erroServidor.closest('fieldset');
    mostrarEtapa(etapaComErro ? etapaComErro.dataset.etapa : location.hash === '#etapa-2' ? 2 : 1, false);
});
