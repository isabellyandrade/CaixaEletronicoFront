const apiUrl = 'http://localhost:7070';

// Referências aos elementos HTML
const deslogadoDiv = document.getElementById('area-deslogada');
const logadoDiv = document.getElementById('area-logada');
const loginForm = document.getElementById('form-login');
const cadastroForm = document.getElementById('form-cadastro');

// Funções para alternar as telas
function mostrarCadastro() {
    loginForm.style.display = 'none';
    cadastroForm.style.display = 'block';
}
function mostrarLogin() {
    cadastroForm.style.display = 'none';
    loginForm.style.display = 'block';
}

// Função de Cadastro
async function fazerCadastro() {
    const nome = document.getElementById('cadastro-username').value;
    const senha = document.getElementById('cadastro-password').value;
    const tipoConta = parseInt(document.getElementById('cadastro-tipo-conta').value);
    const mensagemEl = document.getElementById('mensagem-cadastro');
    
    const response = await fetch(`${apiUrl}/usuarios`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ nome, senha, tipoConta })
    });
    const data = await response.json();
    mensagemEl.style.color = response.ok ? 'green' : 'red';
    mensagemEl.textContent = data.mensagem;
    if(response.ok) {
        setTimeout(mostrarLogin, 2000);
    }
}

// Função de Login
async function fazerLogin() {
    const nome = document.getElementById('login-username').value;
    const senha = document.getElementById('login-password').value;
    const mensagemEl = document.getElementById('mensagem-login');

    try {
        const response = await fetch(`${apiUrl}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, senha })
        });

        const data = await response.json();
        if (response.ok) {
            localStorage.setItem('authToken', data.token);
            deslogadoDiv.style.display = 'none';
            logadoDiv.style.display = 'block';
            document.getElementById('boas-vindas').textContent = `Olá, ${nome}!`;
            consultarSaldo();
        } else {
            mensagemEl.textContent = data.mensagem;
        }
    } catch (error) {
        mensagemEl.textContent = 'Não foi possível conectar ao servidor. O Back-End está rodando?';
    }
}

// Funções de Operações
async function fazerDeposito() {
    const valor = parseFloat(document.getElementById('valor-operacao').value);
    await fazerOperacao(`${apiUrl}/operacoes/deposito`, { valor });
}

async function fazerSaque() {
    const valor = parseFloat(document.getElementById('valor-operacao').value);
    await fazerOperacao(`${apiUrl}/operacoes/saque`, { valor });
}

async function fazerTransferencia() {
    const valor = parseFloat(document.getElementById('valor-transferencia').value);
    const usuarioDestino = document.getElementById('usuario-destino').value;
    await fazerOperacao(`${apiUrl}/operacoes/transferencia`, { valor, usuarioDestino });
}

async function fazerOperacao(url, corpo) {
    const token = localStorage.getItem('authToken');
    const mensagemEl = document.getElementById('mensagem-operacao');

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(corpo)
        });

        const data = await response.json();
        mensagemEl.style.color = response.ok ? 'green' : 'red';
        mensagemEl.textContent = response.ok ? 'Operação realizada com sucesso!' : data.mensagem;
        
        if (response.ok) {
            consultarSaldo(); // Atualiza o saldo na tela
            // Limpa os campos de input de operação
            document.getElementById('valor-operacao').value = '';
            document.getElementById('valor-transferencia').value = '';
            document.getElementById('usuario-destino').value = '';
        }
    } catch (error) {
        mensagemEl.style.color = 'red';
        mensagemEl.textContent = 'Erro de conexão ao realizar operação.';
    }
}

async function consultarSaldo() {
    const token = localStorage.getItem('authToken');
    try {
        const response = await fetch(`${apiUrl}/saldo`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        if (response.ok) {
            const saldoFormatado = data.saldo.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
            document.getElementById('saldo-atual').textContent = saldoFormatado;
        }
    } catch (error) {
        console.error('Erro ao buscar saldo:', error);
    }
}

async function consultarExtrato() {
    const token = localStorage.getItem('authToken');
    const extratoDiv = document.getElementById('extrato-lista');
    const mensagemEl = document.getElementById('mensagem-operacao');

    try {
        const response = await fetch(`${apiUrl}/extrato`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        if (response.ok) {
            extratoDiv.innerHTML = '';
            if (data.extrato && data.extrato.length > 0) {
                // O método toString() da sua classe Transacao.java já formata a string
                data.extrato.forEach(transacaoString => {
                    const p = document.createElement('p');
                    p.className = 'transacao';
                    p.textContent = transacaoString;
                    extratoDiv.appendChild(p);
                });
            } else {
                extratoDiv.textContent = 'Nenhuma transação neste período.';
            }
            extratoDiv.style.display = 'block';
            mensagemEl.textContent = '';
        } else {
            mensagemEl.style.color = 'red';
            mensagemEl.textContent = 'Erro ao buscar extrato.';
        }
    } catch(error) {
        mensagemEl.style.color = 'red';
        mensagemEl.textContent = 'Erro de conexão ao buscar extrato.';
    }
}

function deslogar() {
    localStorage.removeItem('authToken');
    logadoDiv.style.display = 'none';
    deslogadoDiv.style.display = 'block';
    mostrarLogin(); // Garante que a tela de login seja mostrada
}