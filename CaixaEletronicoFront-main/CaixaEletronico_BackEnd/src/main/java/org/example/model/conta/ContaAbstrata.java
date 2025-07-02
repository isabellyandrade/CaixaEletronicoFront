package org.example.model.conta;

import org.example.model.Transacao;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL: Implementação base de uma Conta. Contém a lógica de negócio principal.
 * Não sabe nada sobre a interface com o usuário.
 */
public abstract class ContaAbstrata implements Conta {
    protected String numeroConta;
    protected double saldo;
    protected List<Transacao> historicoTransacoes;

    public ContaAbstrata(String numeroConta, double saldoInicial) {
        this.numeroConta = numeroConta;
        this.saldo = saldoInicial;
        this.historicoTransacoes = new ArrayList<>();
        if (saldoInicial > 0) {
            this.adicionarTransacao("DEPOSITO INICIAL", saldoInicial, "Carga inicial");
        }
    }

    private void adicionarTransacao(String tipo, double valor, String descricao) {
        this.historicoTransacoes.add(new Transacao(tipo, valor, descricao));
    }

    @Override
    public boolean depositar(double valor) {
        if (valor <= 0) return false;
        this.saldo += valor;
        this.adicionarTransacao("DEPOSITO", valor, "Deposito em conta");
        return true;
    }

    @Override
    public boolean sacar(double valor) {
        if (valor <= 0 || valor > this.saldo) return false;
        this.saldo -= valor;
        this.adicionarTransacao("SAQUE", valor, "Saque em caixa eletronico");
        return true;
    }

    @Override
    public boolean transferir(double valor, Conta contaDestino) {
        if (valor <= 0 || valor > this.saldo) return false;
        if (this.numeroConta.equals(contaDestino.getNumeroConta())) return false;
        
        this.saldo -= valor;
        this.adicionarTransacao("TRANSFERENCIA ENVIADA", valor, "Para conta: " + contaDestino.getNumeroConta());
        
        return contaDestino.receberTransferencia(valor, this);
    }
    
    @Override
    public boolean receberTransferencia(double valor, Conta contaOrigem) {
        if (valor <= 0) return false;
        this.saldo += valor;
        this.adicionarTransacao("TRANSFERENCIA RECEBIDA", valor, "De conta: " + contaOrigem.getNumeroConta());
        return true;
    }

    @Override
    public double getSaldo() { 
        return this.saldo; 
    }

    @Override
    public String getNumeroConta() { 
        return this.numeroConta; 
    }

    @Override
    public List<Transacao> getHistoricoTransacoes() { 
        return new ArrayList<>(this.historicoTransacoes); 
    }
    
    public abstract void exibirTipoConta(); // Este método é mais da View, mas vamos mantê-lo por simplicidade
}