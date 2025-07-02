package org.example.patterns.command;
import org.example.model.conta.Conta;

/**
 * PATTERN: Command (Concreto)
 * Encapsula a ação de "depositar" como um objeto.
 */
public class DepositoCommand implements Command {
    private Conta conta;
    private double valor;

    public DepositoCommand(Conta conta, double valor) {
        this.conta = conta;
        this.valor = valor;
    }

    @Override
    public boolean execute() {
        return conta.depositar(valor);
    }
}
