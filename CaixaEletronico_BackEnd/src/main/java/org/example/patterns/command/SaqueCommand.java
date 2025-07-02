package org.example.patterns.command;
import org.example.model.conta.Conta;

/**
 * PATTERN: Command (Concreto)
 * Encapsula a ação de "sacar" como um objeto.
 */
public class SaqueCommand implements Command {
    private Conta conta;
    private double valor;

    public SaqueCommand(Conta conta, double valor) {
        this.conta = conta;
        this.valor = valor;
    }

    @Override
    public boolean execute() {
        return conta.sacar(valor);
    }
}