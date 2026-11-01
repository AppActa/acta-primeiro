// A máscara é visual. Os Servlets continuam validando os valores recebidos.
function normalizarValor(valor, tipo) {
    return tipo === 'cnpj' ? valor.replace(/[^a-z0-9]/gi, '').toUpperCase() : valor.replace(/\D/g, '');
}

function formatarCPF(valor) {
    return normalizarValor(valor, 'cpf').slice(0, 11)
        .replace(/^(\d{3})(\d)/, '$1.$2').replace(/^(\d{3}\.\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3}\.\d{3}\.\d{3})(\d)/, '$1-$2');
}

function formatarCNPJ(valor) {
    return normalizarValor(valor, 'cnpj').slice(0, 14)
        .replace(/^(.{2})(.)/, '$1.$2').replace(/^(.{6})(.)/, '$1.$2')
        .replace(/^(.{10})(.)/, '$1/$2').replace(/^(.{15})(.)/, '$1-$2');
}

function formatarCEP(valor) {
    return normalizarValor(valor, 'cep').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2');
}

function formatarTelefone(valor) {
    const numero = normalizarValor(valor, 'telefone').slice(0, 11);
    if (!numero) return '';
    if (numero.length <= 2) return '(' + numero;
    const parte = numero.slice(2);
    const corte = numero.length > 10 ? 5 : 4;
    return '(' + numero.slice(0, 2) + ') ' + parte.slice(0, corte)
        + (parte.length > corte ? '-' + parte.slice(corte) : '');
}

function formatarValor(valor, tipo) {
    if (tipo === 'cpf') return formatarCPF(valor);
    if (tipo === 'cnpj') return formatarCNPJ(valor);
    if (tipo === 'cep') return formatarCEP(valor);
    if (tipo === 'telefone') return formatarTelefone(valor);
    return valor;
}

document.querySelectorAll('input[data-mascara]').forEach(function (campo) {
    const tipo = campo.dataset.mascara;
    let anterior = '';
    let inicio = 0;
    let fim = 0;
    campo.value = formatarValor(campo.value, tipo);
    campo.addEventListener('beforeinput', function () {
        anterior = campo.value;
        inicio = campo.selectionStart;
        fim = campo.selectionEnd;
    });
    campo.addEventListener('input', function (evento) {
        let numero = normalizarValor(campo.value, tipo);
        let posicao = normalizarValor(campo.value.slice(0, campo.selectionStart), tipo).length;
        // Apagar um separador também apaga o caractere adjacente, sem prender o cursor.
        if (inicio === fim && numero === normalizarValor(anterior, tipo)
                && (evento.inputType === 'deleteContentBackward' || evento.inputType === 'deleteContentForward')) {
            const indice = normalizarValor(anterior.slice(0, inicio), tipo).length
                - (evento.inputType === 'deleteContentBackward' ? 1 : 0);
            if (indice >= 0) {
                numero = numero.slice(0, indice) + numero.slice(indice + 1);
                posicao = indice;
            }
        }
        campo.value = formatarValor(numero, tipo);
        let cursor = 0;
        while (cursor < campo.value.length && normalizarValor(campo.value.slice(0, cursor), tipo).length < posicao) cursor++;
        campo.setSelectionRange(cursor, cursor);
    });
});

document.querySelectorAll('[data-formato]').forEach(function (elemento) {
    const tipo = elemento.dataset.formato;
    const valor = normalizarValor(elemento.textContent, tipo);
    const tamanho = {cpf: 11, cnpj: 14, cep: 8};
    if (valor.length === tamanho[tipo] || (tipo === 'telefone' && (valor.length === 10 || valor.length === 11))) {
        elemento.textContent = formatarValor(valor, tipo);
    }
});

document.querySelectorAll('form').forEach(function (formulario) {
    const campos = formulario.querySelectorAll('input[data-mascara]');
    if (!campos.length) return;
    // Normaliza os dados enviados sem remover a máscara visível ou interferir na validação HTML.
    formulario.addEventListener('formdata', function (evento) {
        campos.forEach(function (campo) {
            if (campo.name && !campo.disabled) evento.formData.set(campo.name, normalizarValor(campo.value, campo.dataset.mascara));
        });
    });
    formulario.addEventListener('reset', function () {
        requestAnimationFrame(function () {
            campos.forEach(function (campo) { campo.value = formatarValor(campo.value, campo.dataset.mascara); });
        });
    });
});

const cacheViaCep = {};
document.querySelectorAll('input[data-mascara="cep"]').forEach(function (campo) {
    const formulario = campo.form;
    const aviso = campo.parentElement.querySelector('[data-viacep-status]');
    let ultimoCep = normalizarValor(campo.value, 'cep');
    let consulta = 0;
    let controle;

    function informar(mensagem) {
        if (aviso) {
            aviso.textContent = mensagem;
            aviso.hidden = !mensagem;
        }
    }

    campo.addEventListener('input', async function () {
        const cep = normalizarValor(campo.value, 'cep');
        if (cep === ultimoCep) return;
        ultimoCep = cep;
        const atual = ++consulta;
        if (controle) controle.abort();
        informar('');
        if (cep.length !== 8) return;

        const nomes = {logradouro: 'rua', bairro: 'bairro', localidade: 'cidade', uf: 'estado', complemento: 'complemento'};
        const valoresAnteriores = {};
        for (const chave in nomes) valoresAnteriores[chave] = formulario.elements.namedItem(nomes[chave]).value;
        informar('Consultando CEP…');
        controle = new AbortController();
        const requisicao = controle;
        const tempo = setTimeout(function () { requisicao.abort(); }, 8000);
        try {
            let dados = cacheViaCep[cep];
            if (!dados) {
                const resposta = await fetch('https://viacep.com.br/ws/' + cep + '/json/', {signal: requisicao.signal});
                if (!resposta.ok) throw new Error('Falha na consulta');
                dados = await resposta.json();
            }
            if (atual !== consulta || normalizarValor(campo.value, 'cep') !== cep) return;
            if (dados && (dados.erro === true || dados.erro === 'true')) {
                cacheViaCep[cep] = dados;
                informar('CEP não encontrado. Preencha o endereço manualmente.');
                return;
            }
            if (!dados || typeof dados.cep !== 'string' || normalizarValor(dados.cep, 'cep') !== cep
                    || typeof dados.localidade !== 'string' || !dados.localidade.trim()
                    || typeof dados.uf !== 'string' || !/^[A-Z]{2}$/.test(dados.uf)) throw new Error('Resposta inválida');
            cacheViaCep[cep] = dados;
            let excedeuLimite = false;
            for (const chave in nomes) {
                const destino = formulario.elements.namedItem(nomes[chave]);
                const valor = dados[chave];
                if (typeof valor !== 'string' || !valor.trim() || destino.value !== valoresAnteriores[chave]) continue;
                if (chave === 'complemento' && destino.value.trim()) continue;
                if (destino.maxLength > 0 && valor.length > destino.maxLength) {
                    excedeuLimite = true;
                    continue;
                }
                destino.value = valor;
            }
            informar(excedeuLimite ? 'Alguns dados excedem o limite do campo. Revise e preencha manualmente.' : 'Endereço preenchido. Revise os dados.');
        } catch (erro) {
            if (atual === consulta) informar('Não foi possível consultar o CEP. Preencha o endereço manualmente.');
        } finally {
            clearTimeout(tempo);
        }
    });
    campo.addEventListener('blur', function () {
        const tamanho = normalizarValor(campo.value, 'cep').length;
        if (tamanho > 0 && tamanho < 8) informar('Informe os oito dígitos do CEP.');
    });
    formulario.addEventListener('reset', function () {
        consulta++;
        if (controle) controle.abort();
        informar('');
        requestAnimationFrame(function () { ultimoCep = normalizarValor(campo.value, 'cep'); });
    });
});
