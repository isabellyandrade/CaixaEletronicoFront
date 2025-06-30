package org.example.controller;

import com.google.gson.Gson;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * =================================================================================
 * CLASSE: CaixaEletronicoController (CAMADA CONTROLLER - MVC)
 * =================================================================================
 * RESPONSABILIDADE:
 * Atuar como o "maestro" aplicacao. Ele eh a ponte entre o mundo
 * externo (requisicões HTTP do Front-End) e a lógica de negócio interna (Model).
 * Ele nao contehm regras de negócio, mas orquestra a chamada a outros componentes
 * que as contem.
 */
public class CaixaEletronicoController {

    /**
     * Simula nosso banco de dados de usuarios. Em um sistema real, isso seria
     * substituído por uma conexao a um banco de dados como PostgreSQL ou MySQL.
     */
    private final Map<String, Usuario> usuariosDb = new HashMap<>();

    /**
     * Ferramenta para converter objetos Java para o formato JSON e vice-versa.
     * Essencial para a comunicacao com o Front-End.
     */
    private final Gson gson = new Gson();

    /**
     * O construtor do Controller recebe a instância do servidor Javalin
     * e configura todas as rotas (endpoints) da nossa API.
     * @param app A instância do servidor web Javalin.
     */
    public CaixaEletronicoController(Javalin app) {
        cadastrarUsuariosIniciais();

        // Endpoint público para cadastro de novos usuarios.
        app.post("/usuarios", this::cadastrarNovoUsuario);

        // Endpoint público para autenticacao de usuarios.
        app.post("/login", this::efetuarLogin);

        // Endpoints de operacões que exigem autenticacao via Token.
        app.post("/operacoes/deposito", this::efetuarDeposito);
        app.post("/operacoes/saque", this::efetuarSaque);
        app.post("/operacoes/transferencia", this::efetuarTransferencia);
        app.get("/saldo", this::consultarSaldo);
        app.get("/extrato", this::consultarExtrato);
    }

    // =================================================================================
    // MehTODOS QUE TRATAM AS REQUISIcÕES (HANDLERS)
    // =================================================================================

    private void cadastrarNovoUsuario(Context ctx) {
        CadastroRequest req = gson.fromJson(ctx.body(), CadastroRequest.class);
        String nome = req.nome.toLowerCase();

        if (usuariosDb.containsKey(nome)) {
            ctx.status(409).json(Map.of("sucesso", false, "mensagem", "Erro: Nome de usuario ja existe."));
            return;
        }

        /**
         * PATTERN: Factory Method
         * O Controller recebe um "código" do tipo de conta (1, 2, ou 3) e usa
         * a Factory apropriada para criar o objeto Conta correto.
         * O Controller nao precisa saber os detalhes de como uma ContaCorrente
         * ou ContaPoupanca eh construída. Ele apenas pede à fabrica para criar.
         */
        ContaFactory factory;
        switch (req.tipoConta) {
            case 1: factory = new ContaCorrenteFactory(); break;
            case 2: factory = new ContaPoupancaFactory(); break;
            case 3: factory = new ContaEmpresarialFactory(); break;
            default:
                ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Tipo de conta invalido."));
                return;
        }

        Usuario novoUsuario = new Usuario(nome, req.senha, factory);
        usuariosDb.put(nome, novoUsuario);
        ctx.status(201).json(Map.of("sucesso", true, "mensagem", "Usuario '" + nome + "' cadastrado com sucesso!"));
    }

    private void efetuarLogin(Context ctx) {
        LoginRequest loginReq = gson.fromJson(ctx.body(), LoginRequest.class);
        Usuario user = usuariosDb.get(loginReq.nome.toLowerCase());

        if (user != null && user.autenticar(loginReq.senha)) {
            // Em um sistema real, usaríamos um Token JWT. Nosso token simples eh para fins didaticos.
            String token = user.getNome() + "-token-secreto";
            ctx.json(new LoginResponse(true, "Login bem-sucedido!", token));
        } else {
            ctx.status(401).json(new LoginResponse(false, "Usuario ou senha invalidos.", null));
        }
    }

    private void efetuarSaque(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return; // autenticarViaToken ja define o status de erro

        /**
         * PATTERN: Proxy
         * Antes de realizar qualquer operacao, criamos um Proxy para a conta real do usuario.
         * O Proxy adiciona uma camada de controle, como verificar a disponibilidade de notas,
         * sem que o Controller ou o Command precisem se preocupar com essa lógica.
         */
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        OperacaoRequest saqueReq = gson.fromJson(ctx.body(), OperacaoRequest.class);

        /**
         * PATTERN: Command
         * A requisicao de "saque" eh encapsulada em um objeto `SaqueCommand`.
         * O Controller nao chama `conta.sacar()` diretamente. Ele cria um comando
         * e o executa. Isso separa a "invocacao" da "execucao", permitindo
         * funcionalidades futuras como filas de comandos ou undo/redo.
         */
        Command saqueCommand = new SaqueCommand(contaProxy, saqueReq.valor);
        boolean sucesso = saqueCommand.execute();

        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxy.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha no saque. Verifique o valor ou saldo."));
        }
    }
    
    private void efetuarDeposito(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        OperacaoRequest depositoReq = gson.fromJson(ctx.body(), OperacaoRequest.class);
        
        Command depositoCommand = new DepositoCommand(contaProxy, depositoReq.valor);
        boolean sucesso = depositoCommand.execute();
        
        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxy.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha no depósito. Valor invalido."));
        }
    }

    private void efetuarTransferencia(Context ctx) {
        Usuario userOrigem = autenticarViaToken(ctx);
        if (userOrigem == null) return;

        TransferenciaRequest req = gson.fromJson(ctx.body(), TransferenciaRequest.class);
        Usuario userDestino = usuariosDb.get(req.usuarioDestino.toLowerCase());

        if (userDestino == null) {
            ctx.status(404).json(Map.of("sucesso", false, "mensagem", "Usuario de destino nao encontrado."));
            return;
        }
        if (Objects.equals(userOrigem.getNome(), userDestino.getNome())) {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Nao eh possível transferir para si mesmo."));
            return;
        }

        Conta contaProxyOrigem = new ContaProxy(userOrigem.getConta(), userOrigem);
        
        Command transferenciaCommand = new TransferenciaCommand(contaProxyOrigem, userDestino.getConta(), req.valor);
        boolean sucesso = transferenciaCommand.execute();

        if (sucesso) {
            ctx.json(Map.of("sucesso", true, "novoSaldo", contaProxyOrigem.getSaldo()));
        } else {
            ctx.status(400).json(Map.of("sucesso", false, "mensagem", "Falha na transferencia. Verifique o valor ou saldo."));
        }
    }

    private void consultarSaldo(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        ctx.json(Map.of("sucesso", true, "saldo", contaProxy.getSaldo()));
    }
    
    private void consultarExtrato(Context ctx) {
        Usuario user = autenticarViaToken(ctx);
        if (user == null) return;
        Conta contaProxy = new ContaProxy(user.getConta(), user);
        
        /**
         * PATTERN: Iterator (Uso Indireto)
         * O Controller obtem a List<Transacao> do Model. A biblioteca Gson, por baixo dos panos,
         * itera sobre essa lista para serializa-la em um array JSON. O Front-End, ao receber
         * este array, usara seu próprio Iterator (em JavaScript, por exemplo) para exibir os dados.
         */
        ctx.json(Map.of(
            "sucesso", true,
            "extrato", contaProxy.getHistoricoTransacoes(),
            "saldo", contaProxy.getSaldo()
        ));
    }

    // =================================================================================
    // METODOS AUXILIARES
    // =================================================================================

    private Usuario autenticarViaToken(Context ctx) {
        String token = ctx.header("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            ctx.status(401).json(Map.of("sucesso", false, "mensagem", "Token de autorizacao ausente ou mal formatado."));
            return null;
        }
        String nomeUsuario = token.replace("Bearer ", "").replace("-token-secreto", "");
        Usuario user = usuariosDb.get(nomeUsuario);
        if (user == null) {
            ctx.status(401).json(Map.of("sucesso", false, "mensagem", "Token invalido."));
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

    // =================================================================================
    // CLASSES DTO (Data Transfer Objects) - Para mapear o JSON
    // =================================================================================
    
    static class CadastroRequest { 
        String nome; 
        String senha; 
        int tipoConta; 
    }
    static class LoginRequest { 
        String nome; 
        String senha; 
    }
    static class LoginResponse {
        boolean sucesso; 
        String mensagem; 
        String token;

        LoginResponse(boolean s, String m, String t) { 
            sucesso = s; mensagem = m; token = t; 
        }
    }
    static class OperacaoRequest { 
        double valor; 
    }
    static class TransferenciaRequest { 
        double valor; 
        String usuarioDestino; 
    }
}