package org.example.view;

import org.example.model.Transacao;
import java.util.Iterator;
import java.util.Scanner;
import java.util.List;

/**
 * VIEW: Responsavel por TODA a interacao com o console.
 * É a "tela" do nosso sistema. Nao contém nenhuma regra de negócio.
 */
public class CaixaEletronicoView {
    private final Scanner scanner = new Scanner(System.in);

    public int exibirMenuPrincipal() {
        System.out.println("\n===== MENU PRINCIPAL =====");
        System.out.println("[1] Cadastrar Novo Usuario");
        System.out.println("[2] Efetuar Login");
        System.out.println("[0] Sair do Programa");
        System.out.print("Escolha uma opcao: ");
        int escolha = scanner.nextInt();
        scanner.nextLine(); // Limpeza de buffer
        return escolha;
    }
    
    public int exibirMenuOperacoes() {
        System.out.println("\n--- MENU DE OPERACOES ---");
        System.out.println("1. Depositar\n2. Sacar\n3. Transferir\n4. Emitir Extrato\n5. Ver Saldo\n0. Deslogar");
        System.out.print("Escolha uma opcao: ");
        int escolha = scanner.nextInt();
        scanner.nextLine(); 
        return escolha;
    }

    public String[] pedirCredenciaisLogin() {
        String[] credenciais = new String[2];
        System.out.println("\n##### Login de Usuario #####");
        System.out.print("Nome de usuario: ");
        credenciais[0] = scanner.nextLine();
        System.out.print("Senha: ");
        credenciais[1] = scanner.nextLine();
        return credenciais;
    }
    
    public String[] pedirDadosCadastro() {
        String[] dados = new String[3];
        System.out.println("\n##### Cadastro de Novo Usuario #####");
        System.out.print("Digite o nome do novo usuario: ");
        dados[0] = scanner.nextLine();
        System.out.print("Digite a senha: ");
        dados[1] = scanner.nextLine();
        System.out.println("Escolha o tipo de conta (1-Corrente, 2-Poupanca, 3-Empresarial):");
        dados[2] = scanner.nextLine();
        return dados;
    }
    
    public double pedirValor(String mensagem) {
        System.out.print(mensagem);
        double valor = scanner.nextDouble();
        scanner.nextLine(); // Limpeza de buffer
        return valor;
    }
    
    public String pedirUsername(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }

    public void exibirMensagem(String mensagem) {
        System.out.println(mensagem);
    }
    
    public void exibirExtrato(List<Transacao> historico, double saldo) {
        System.out.println("\n==================================================================");
        System.out.println("                       EXTRATO DA CONTA");
        System.out.println("------------------------------------------------------------------");
        if (historico == null || historico.isEmpty()) {
            System.out.println("Nenhuma transacao encontrada.");
        } else {
            /**
             * PATTERN: Iterator
             * Usamos o Iterator explicitamente para percorrer a lista de transacões
             * sem precisar saber como a lista foi implementada no Model.
             * O Controller nos da a lista, e a View a consome com o Iterator.
             */
            Iterator<Transacao> it = historico.iterator();
            while(it.hasNext()){
                Transacao t = it.next();
                System.out.println(t); // Usando o método toString() da Transacao
            }
        }
        System.out.println("------------------------------------------------------------------");
        System.out.printf("SALDO ATUAL: R$ %.2f\n", saldo);
        System.out.println("==================================================================");
    }
}