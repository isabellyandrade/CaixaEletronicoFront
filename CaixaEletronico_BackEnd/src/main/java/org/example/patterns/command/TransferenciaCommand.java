package org.example.patterns.command;

import org.example.model.conta.Conta;

/**
 * PATTERN: Command (Concreto)
 * Encapsula a ação de "transferir" como um objeto.
 * Contém todos os dados necessários para realizar a transferência.
 */
public class TransferenciaCommand implements Command {

    // 1. OS CAMPOS para guardar TODA a informação necessária
    private final Conta contaOrigem;
    private final Conta contaDestino;
    private final double valor;

    public TransferenciaCommand(Conta contaOrigem, Conta contaDestino, double valor) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
    }


    @Override
    public boolean execute() {
        return this.contaOrigem.transferir(this.valor, this.contaDestino);
    }
}