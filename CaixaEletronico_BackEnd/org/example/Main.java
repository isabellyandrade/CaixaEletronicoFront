package org.example;

import org.example.controller.BancoController;
import io.javalin.Javalin;

/**
 * Ponto de entrada da aplicação BACK-END.
 * Inicia o servidor da API e delega a configuração das rotas para o Controller.
 */
public class Main {
    public static void main(String[] args) {
        // Cria o servidor Javalin que vai rodar na porta 7070
        Javalin app = Javalin.create().start(7070);
        
        System.out.println("API do Caixa Eletronico iniciada em http://localhost:7070");

        // Cria o controller, que agora é responsável por definir as rotas da API
        new BancoController(app);
    }
}