package org.example.patterns.factory;

import org.example.model.conta.Conta;
import org.example.model.conta.ContaCorrente;

/**
 * PATTERN: Factory Method (Abstrato)
 */

public class ContaCorrenteFactory extends ContaFactory {
    @Override
    public Conta criarConta() {
        String numeroConta = "CC -" + (10000 + new java.util.Random().nextInt(90000));
        
        // Começa com saldo 0.
        return new ContaCorrente(numeroConta, 0.0);
    }
}