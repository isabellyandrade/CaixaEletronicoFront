package org.example.model.conta;

/**
 * MODEL: Implementação concreta de uma Conta Empresarial.
 * Herda a maior parte de sua funcionalidade da ContaAbstrata.
 */
public class ContaEmpresarial extends ContaAbstrata {
    public ContaEmpresarial(String numeroConta, double saldoInicial) {
        super(numeroConta, saldoInicial);
    }

    @Override
    public void exibirTipoConta() {
        System.out.println("Conta Empresarial criada.");
    }
}

