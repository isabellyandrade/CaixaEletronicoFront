package org.example.patterns.proxy;

import org.example.model.Transacao;
import org.example.model.Usuario;
import org.example.model.conta.Conta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PATTERN: Proxy
 * Atua como um intermediário de segurança para o objeto Conta real.
 * É a camada que aplica regras de negócio antes de executar uma operação no Model.
 * Pertence à camada Model, pois contém regras de negócio (disponibilidade de notas).
 */
public class ContaProxy implements Conta {
    private final Conta contaReal;
    private final Usuario usuario;

    // A "memória" do nosso caixa eletrônico. É 'static' porque o caixa é um só.
    private static final Map<Integer, Integer> slotsDeNotas = new LinkedHashMap<>();

    static {
        slotsDeNotas.put(200, 20);
        slotsDeNotas.put(100, 30);
        slotsDeNotas.put(50, 50);
        slotsDeNotas.put(20, 50);
        slotsDeNotas.put(10, 100);
        slotsDeNotas.put(5, 100);
        slotsDeNotas.put(2, 200);
    }

    public ContaProxy(Conta contaReal, Usuario usuario) {
        this.contaReal = contaReal;
        this.usuario = usuario;
    }

    private boolean verificarAutenticacao() {
        if (usuario != null && usuario.isAutenticado()) {
            return true;
        }
        // No MVC, o Proxy não deve imprimir erros para a View.
        // A falha em retornar 'true' será tratada pelo Controller.
        System.out.println("[Proxy LOG] Tentativa de operação falhou: Usuario nao autenticado.");
        return false;
    }

    @Override
    public boolean depositar(double valor) {
        if (!verificarAutenticacao()) return false;
        return contaReal.depositar(valor);
    }

    @Override
    public boolean sacar(double valor) {
        if (!verificarAutenticacao()) return false;

        if (valor % 1 != 0 || valor <= 0) {
            System.out.println("[Proxy LOG] Saque falhou: Valor nao eh um inteiro positivo.");
            return false;
        }
        if (contaReal.getSaldo() < valor) {
            System.out.println("[Proxy LOG] Saque falhou: Saldo insuficiente.");
            return false;
        }

        Map<Integer, Integer> planoDeSaque = verificarDisponibilidadeNotas((int) valor);
        if (planoDeSaque == null) {
            System.out.println("[Proxy LOG] Saque falhou: Nao foi possível compor o valor com as notas disponiveis.");
            return false;
        }

        // Se tudo passou, executa as operações
        boolean sucesso = contaReal.sacar(valor);
        if (sucesso) {
            atualizarSlots(planoDeSaque);
            // O Proxy não informa mais o sucesso ao usuário. O Controller fará isso.
            System.out.println("[Proxy LOG] Saque de R$" + valor + " efetuado. Notas dispensadas: " + planoDeSaque);
            return true;
        }
        return false;
    }

    @Override
    public boolean transferir(double valor, Conta contaDestino) {
        if (!verificarAutenticacao()) return false;
        if (contaReal.getSaldo() < valor) {
            System.out.println("[Proxy LOG] Transferencia falhou: Saldo insuficiente.");
            return false;
        }
        return contaReal.transferir(valor, contaDestino);
    }
    
    @Override
    public boolean receberTransferencia(double valor, Conta contaOrigem) {
        // A validação de autenticação não se aplica ao receber,
        // pois a conta pode estar "offline". A ação é iniciada pela conta de origem.
        return contaReal.receberTransferencia(valor, contaOrigem);
    }


    // Métodos de consulta que precisam de autenticação
    @Override
    public double getSaldo() {
        return verificarAutenticacao() ? contaReal.getSaldo() : -1; // Retorna -1 para indicar erro
    }

    @Override
    public List<Transacao> getHistoricoTransacoes() {
        return verificarAutenticacao() ? contaReal.getHistoricoTransacoes() : null;
    }

    @Override
    public String getNumeroConta() {
        // Não requer autenticação para ver o número da conta
        return contaReal.getNumeroConta();
    }
    
    // Métodos auxiliares para a lógica de saque
    private Map<Integer, Integer> verificarDisponibilidadeNotas(int valor) {
        Map<Integer, Integer> planoDeSaque = new LinkedHashMap<>();
        int valorRestante = valor;
        for (Map.Entry<Integer, Integer> entry : slotsDeNotas.entrySet()) {
            int nota = entry.getKey();
            int qtdDisponivel = entry.getValue();
            if (valorRestante >= nota && qtdDisponivel > 0) {
                int qtdAUsar = Math.min(valorRestante / nota, qtdDisponivel);
                if (qtdAUsar > 0) {
                    planoDeSaque.put(nota, qtdAUsar);
                    valorRestante -= qtdAUsar * nota;
                }
            }
        }
        return (valorRestante == 0) ? planoDeSaque : null;
    }

    private void atualizarSlots(Map<Integer, Integer> planoDeSaque) {
        for (Map.Entry<Integer, Integer> entry : planoDeSaque.entrySet()) {
            int nota = entry.getKey();
            int qtdUsada = entry.getValue();
            slotsDeNotas.put(nota, slotsDeNotas.get(nota) - qtdUsada);
        }
    }
}