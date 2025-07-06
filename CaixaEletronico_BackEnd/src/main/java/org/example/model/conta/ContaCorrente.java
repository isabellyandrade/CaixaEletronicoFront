package org.example.model.conta;

/**
 * MODEL: Implementação concreta de uma Conta Corrente.
 * Herda a maior parte de sua funcionalidade da ContaAbstrata.
 */
public class ContaCorrente extends ContaAbstrata {
    public ContaCorrente(String numeroConta, double saldoInicial) {
        super(numeroConta, saldoInicial);
    }

    @Override
    public void exibirTipoConta() {
        System.out.println("Conta Corrente criada.");
    }
}

