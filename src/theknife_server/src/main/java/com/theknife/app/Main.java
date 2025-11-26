package com.theknife.app;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        int port = 12345; // porta predefinita

        ServerApplication server = ServerApplication.getInstance();

        System.out.println("[MAIN] Avvio del server...");

        if (!server.start(port)) {
            System.out.println("[MAIN] ERRORE: impossibile avviare il server.");
            return;
        }

        System.out.println("[MAIN] Server avviato sulla porta " + port);
        System.out.println("[MAIN] Digita 'quit' per arrestarlo.");

        // Attesa comandi console
        try (Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String cmd = scanner.nextLine();

                if (cmd.equalsIgnoreCase("quit") ||
                    cmd.equalsIgnoreCase("exit") ||
                    cmd.equalsIgnoreCase("stop")) 
                {
                    break;
                }

                System.out.println("[MAIN] Comando sconosciuto: " + cmd);
            }
        }

        System.out.println("[MAIN] Arresto del server...");
        server.stop();
        System.out.println("[MAIN] Terminato.");
    }
}
