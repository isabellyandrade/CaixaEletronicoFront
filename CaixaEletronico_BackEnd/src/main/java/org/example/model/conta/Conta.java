package org.example.model.conta;

import org.example.model.Transacao;
import java.util.List;

//MODEL DA CONTA

public interface Conta {
    boolean depositar(double valor);
    boolean sacar(double valor);
    boolean transferir(double valor, Conta contaDestino);
    boolean receberTransferencia(double valor, Conta contaOrigem);

    double getSaldo();
    String getNumeroConta();
    List<Transacao> getHistoricoTransacoes();
}