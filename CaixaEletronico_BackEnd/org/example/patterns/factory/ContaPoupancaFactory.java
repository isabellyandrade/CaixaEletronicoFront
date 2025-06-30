package org.example.patterns.factory;

import org.example.model.conta.Conta;
import org.example.model.conta.ContaPoupanca;

/**
 * PATTERN: Factory Method (Abstrato)
*/

public class ContaPoupancaFactory extends ContaFactory {
    public Conta criarConta() {
        String numeroConta = "CP -" + (10000 + new java.util.Random().nextInt(90000));
        
        // Começa com saldo 0.
        return new ContaPoupanca(numeroConta, 0.0);
    }
}
