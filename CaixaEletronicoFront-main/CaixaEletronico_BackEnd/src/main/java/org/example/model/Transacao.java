package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transacao {
    private final LocalDateTime data;
    private final String tipo;
    private final double valor;
    private final String descricao;

    public Transacao(String tipo, double valor, String descricao) {
        this.data = LocalDateTime.now(); 
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
    }

    // Método para formatar a transação para exibição no extrato
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = data.format(formatter);
        return String.format("[%s] %-25s | R$ %9.2f | %s", dataFormatada, tipo, valor, descricao);
    }
}