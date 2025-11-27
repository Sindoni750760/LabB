package com.theknife.app;

import java.util.Scanner;

/**
 * Classe di avvio dell'applicazione server.
 * Gestisce l'avvio del server, l'accettazione di connessioni client,
 * e l'ascolto dei comandi da console per l'arresto del server.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class Main {
    
    /**
     * Metodo principale dell'applicazione server.
     * Avvia il server sulla porta predefinita (12345) e attende i comandi da console.
     * Digita 'quit', 'exit' o 'stop' per arrestare il server.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
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
