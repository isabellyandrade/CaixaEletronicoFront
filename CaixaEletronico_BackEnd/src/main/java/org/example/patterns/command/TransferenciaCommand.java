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

    /**
     * 2. O CONSTRUTOR agora aceita os TRÊS parâmetros que o Controller envia.
     * @param contaOrigem A conta que vai enviar o dinheiro (o Proxy).
     * @param contaDestino A conta que vai receber.
     * @param valor O montante a ser transferido.
     */
    public TransferenciaCommand(Conta contaOrigem, Conta contaDestino, double valor) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
    }

    /**
     * 3. O MÉTODO EXECUTE agora usa os campos guardados para chamar o método
     * 'transferir' com os DOIS parâmetros que ele exige.
     */
    @Override
    public boolean execute() {
        return this.contaOrigem.transferir(this.valor, this.contaDestino);
    }
}