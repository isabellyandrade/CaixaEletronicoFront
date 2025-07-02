package org.example.model;

import org.example.model.conta.Conta;
import org.example.patterns.factory.ContaFactory;

public class Usuario {
    private String nome;
    private String senha;
    private Conta contaReal; 
    private boolean autenticado = false;

    public Usuario(String nome, String senha, ContaFactory factory) {
        this.nome = nome;
        this.senha = senha;
        this.contaReal = factory.criarConta();
        this.autenticado = false;
    }

    // O método de autenticação só precisa validar a senha.
    public boolean autenticar(String senha) {
        if (this.senha.equals(senha)) {
            this.autenticado = true;
            return true;
        }
        return false;
    }

    public void deslogar() {
        this.autenticado = false;
    }

    public String getNome() {
        return nome;
    }

    public Conta getConta() {
        return contaReal;
    }
    
    public boolean isAutenticado() {
        return autenticado;
    }
}