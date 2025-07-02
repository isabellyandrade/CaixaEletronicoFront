package org.example.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.model.Usuario;
import org.example.model.conta.Conta;
import org.example.patterns.command.*;
import org.example.patterns.factory.ContaCorrenteFactory;
import org.example.patterns.factory.ContaEmpresarialFactory;
import org.example.patterns.factory.ContaFactory;
import org.example.patterns.factory.ContaPoupancaFactory;
import org.example.patterns.proxy.ContaProxy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CaixaEletronicoController {
    private final Map<String, Usuario> usuariosDb = new HashMap<>();

    // O construtor agora só precisa do 'app'
    public CaixaEletronicoController    (Javalin app) {
        cadastrarUsuariosIniciais();

        app.post("/usuarios", this::cadastrarNovoUsuario);
        app.post("/login", this::efetuarLogin);
        app.post("/operacoes/deposito", this::efetuarDeposito);
        app.post("/operacoes/saque", this::efetuarSaque);
        app.post("/operacoes/transferencia", this::efetuarTransferencia);
        app.get("/saldo", this::consultarSaldo);
        app.get("/extrato", this::consultarExtrato);
    }

    private void efetuarLogin(Context ctx) {
        // Usamos o método nativo do Javalin, que já usa o Gson que configuramos
        LoginRequest loginReq = ctx.bodyAsClass(LoginRequest.class);
        Usuario user = usuariosDb.get(loginReq.nome.toLowerCase());

        if (user != null && user.autenticar(loginReq.senha)) {
            String token = user.getNome() + "-token-secreto";
            // A chamada ctx.json(...) vai usar nosso Gson automaticamente, sem warnings!
            ctx.json(new LoginResponse(true, "Login bem-sucedido!", token));
        } else {
            ctx.status(401).json(new LoginResponse(false, "Usuário ou senha inválidos.", null));
        }
    }

    private void efetuarSaque(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;

        Conta contaProxy = new ContaProxy(user.getConta(), user);
        OperacaoRequest saqueReq = ctx.bodyAsClass(OperacaoRequest.class);

        Command saqueCommand = new SaqueCommand(contaProxy, saqueReq.valor);
        boolean sucesso = saqueCommand.execute();

        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxy.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha no saque. Verifique o valor ou saldo."));
        }
    }

    // ... todos os outros métodos do controller seguem o mesmo padrão,
    // usando ctx.bodyAsClass() em vez de gson.fromJson() ...

    // Deixei os outros métodos aqui para você poder copiar e colar tudo de uma vez.
    private void cadastrarNovoUsuario(Context ctx) {
        CadastroRequest req = ctx.bodyAsClass(CadastroRequest.class);
        String nome = req.nome.toLowerCase();
        if (usuariosDb.containsKey(nome)) {
            ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Erro: Nome de usuário já existe."));
            return;
        }
        ContaFactory factory;
        switch (req.tipoConta) {
            case 1: factory = new ContaCorrenteFactory(); break;
            case 2: factory = new ContaPoupancaFactory(); break;
            case 3: factory = new ContaEmpresarialFactory(); break;
            default:
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Tipo de conta inválido."));
                return;
        }
        Usuario novoUsuario = new Usuario(nome, req.senha, factory);
        usuariosDb.put(nome, novoUsuario);
        ctx.status(201).json(Map.of("sucesso", true, "mensagem", "Usuário '" + nome + "' cadastrado com sucesso!"));
    }

    private void efetuarDeposito(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        OperacaoRequest depositoReq = ctx.bodyAsClass(OperacaoRequest.class);
        Command depositoCommand = new DepositoCommand(contaProxy, depositoReq.valor);
        boolean sucesso = depositoCommand.execute();
        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxy.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha no depósito. Valor inválido."));
        }
    }

    private void efetuarTransferencia(Context ctx) {
        Usuario userOrigem = autenticarViaToken(ctx);
        if (userOrigem == null) return;
        TransferenciaRequest req = ctx.bodyAsClass(TransferenciaRequest.class);
        Usuario userDestino = usuariosDb.get(req.usuarioDestino.toLowerCase());
        if (userDestino == null) {
            ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Usuário de destino não encontrado."));
            return;
        }
        if (Objects.equals(userOrigem.getNome(), userDestino.getNome())) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Não é possível transferir para si mesmo."));
            return;
        }
        Conta contaProxyOrigem = new ContaProxy(userOrigem.getConta(), userOrigem);
        Command transferenciaCommand = new TransferenciaCommand(contaProxyOrigem, userDestino.getConta(), req.valor);
        boolean sucesso = transferenciaCommand.execute();
        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxyOrigem.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha na transferência. Verifique o valor ou saldo."));
        }
    }

    private void consultarSaldo(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        ctx.json(Map.of("sucesso", true, "saldo", contaProxy.getSaldo()));
    }

    // A versão corrigida
    private void consultarExtrato(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        Conta contaProxy = new ContaProxy(user.getConta(), user);

        // 1. Pegamos a lista de objetos Transacao, como antes.
        List<org.example.model.Transacao> historico = contaProxy.getHistoricoTransacoes();

        // 2. Usamos o poder do Java para transformar cada objeto Transacao em sua representação de String.
        List<String> historicoFormatado = historico.stream()
                .map(Object::toString) // Para cada transação na lista, chama o método toString()
                .collect(java.util.stream.Collectors.toList());

        // 3. Enviamos a lista de Strings já formatadas no JSON.
        ctx.json(Map.of(
                "sucesso", true,
                "extrato", historicoFormatado, // << Enviando a lista de Strings simples
                "saldo", contaProxy.getSaldo()
        ));
    }

    private Usuario autenticarViaToken(Context ctx) {
        String token = ctx.header("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            ctx.status(401).json(Map.of("sucesso", false, "mensagem", "Token de autorização ausente ou mal formatado."));
            return null;
        }
        String nomeUsuario = token.replace("Bearer ", "").replace("-token-secreto", "");
        Usuario user = usuariosDb.get(nomeUsuario);
        if (user == null) {
            ctx.status(401).json(Map.of("sucesso", false, "mensagem", "Token inválido."));
            return null;
        }
        return user;
    }

    private void cadastrarUsuariosIniciais() {
        Usuario user1 = new Usuario("joao", "123", new ContaCorrenteFactory());
        user1.getConta().depositar(1500);
        usuariosDb.put(user1.getNome(), user1);
        Usuario user2 = new Usuario("maria", "456", new ContaPoupancaFactory());
        user2.getConta().depositar(3250.50);
        usuariosDb.put(user2.getNome(), user2);
    }

    // Classes DTO
    static class CadastroRequest { String nome; String senha; int tipoConta; }
    static class LoginRequest { String nome; String senha; }
    static class LoginResponse {
        boolean sucesso; String mensagem; String token;
        LoginResponse(boolean s, String m, String t) { sucesso = s; mensagem = m; token = t; }
    }
    static class OperacaoRequest { double valor; }
    static class TransferenciaRequest { double valor; String usuarioDestino; }
}