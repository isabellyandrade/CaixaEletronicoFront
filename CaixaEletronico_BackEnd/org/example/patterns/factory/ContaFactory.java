package org.example.patterns.factory;
import org.example.model.conta.Conta;

/**
 * PATTERN: Factory Method (Abstrato)
 * Define o contrato para criar contas, permitindo que subclasses decidam qual tipo de conta instanciar.
 */
public abstract class ContaFactory {
    public abstract Conta criarConta();
}
