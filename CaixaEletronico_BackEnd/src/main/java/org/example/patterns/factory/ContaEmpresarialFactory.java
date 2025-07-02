package org.example.patterns.factory;

import org.example.model.conta.Conta;
import org.example.model.conta.ContaEmpresarial;

/**
 * PATTERN: Factory Method (Abstrato)
 */

public class ContaEmpresarialFactory extends ContaFactory {
    @Override
    public Conta criarConta() {
        String numeroConta = "CE -" + (10000 + new java.util.Random().nextInt(90000));
        
        // Começa com saldo 0.
        return new ContaEmpresarial(numeroConta, 0.0);
    }
}
