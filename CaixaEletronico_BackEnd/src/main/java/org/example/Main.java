package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import org.example.controller.CaixaEletronicoController;

/**
 * Ponto de entrada da aplicação BACK-END.
 * A única responsabilidade é configurar e iniciar o servidor da API.
 */
public class Main {
    public static void main(String[] args) {

        // 1. Criamos nossa instância do Gson.
        Gson gson = new GsonBuilder().create();

        // 2. Criamos o "adaptador" que ensina o Javalin a usar o Gson.
        JavalinGson adapter = new JavalinGson(gson);

        // 3. Entregamos o adaptador para o Javalin na sua criação.
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(adapter); // <== ESTA É A FORMA CORRETA
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7070);

        System.out.println("API do Caixa Eletronico iniciado em http://localhost:7070");

        // Agora o Controller não precisa mais receber o 'gson', pois o Javalin já o conhece.
        new CaixaEletronicoController(app);
    }
}