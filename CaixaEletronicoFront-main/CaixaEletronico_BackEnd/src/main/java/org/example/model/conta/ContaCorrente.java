package org.example.model.conta;

/**
 * MODEL: Implementação concreta de uma Conta Corrente.
 * Herda a maior parte de sua funcionalidade da ContaAbstrata.
 */
public class ContaCorrente extends ContaAbstrata {

    /**
     * Construtor que repassa os dados para a classe pai (ContaAbstrata).
     * @param numeroConta O número de identificação da conta.
     * @param saldoInicial O saldo inicial da conta.
     */
    public ContaCorrente(String numeroConta, double saldoInicial) {
        super(numeroConta, saldoInicial);
    }

    /**
     * Implementa o método abstrato para exibir o tipo específico desta conta.
     * Embora seja um 'println', ele está mais relacionado à representação
     * do objeto do que à interface do usuário em si.
     */
    @Override
    public void exibirTipoConta() {
        System.out.println("Conta Corrente criada.");
    }
}

