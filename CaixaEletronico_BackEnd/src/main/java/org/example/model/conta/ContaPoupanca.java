package org.example.model.conta;

/**
 * MODEL: Implementação concreta de uma Conta Empresarial.
 * Herda a maior parte de sua funcionalidade da ContaAbstrata.
 */
public class ContaPoupanca extends ContaAbstrata {
    public ContaPoupanca(String numeroConta, double saldoInicial) {
        super(numeroConta, saldoInicial);
    }

    @Override
    public void exibirTipoConta() {
        System.out.println("Conta Poupanca criada.");
    }
}

