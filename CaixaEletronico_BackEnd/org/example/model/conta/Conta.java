package org.example.model.conta;

import org.example.model.Transacao;
import java.util.List;

/**
 * MODEL: A interface que define o contrato de uma Conta.
 * Não tem ideia de como os dados são exibidos (View)
 * ou de como as ações são iniciadas (Controller).
 */
public interface Conta {
    boolean depositar(double valor);
    boolean sacar(double valor);
    boolean transferir(double valor, Conta contaDestino);
    boolean receberTransferencia(double valor, Conta contaOrigem);

    double getSaldo();
    String getNumeroConta();
    List<Transacao> getHistoricoTransacoes();
}