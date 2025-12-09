package com.theknife.app;

import java.util.Scanner;

/**
 * Entry-point dell'applicazione server.
 * <p>
 * Questa classe avvia il server principale e rimane in ascolto
 * sulla console per ricevere comandi amministrativi.
 * </p>
 *
 * <p>I comandi supportati sono:</p>
 * <ul>
 *     <li><b>quit</b> - arresta il server</li>
 *     <li><b>exit</b> - arresta il server</li>
 *     <li><b>stop</b> - arresta il server</li>
 * </ul>
 *
 * Il server viene avviato sulla porta predefinita 12345.
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public class Main {

    /**
     * Metodo principale di avvio del server.
     * <p>
     * Responsabilità del metodo:
     * </p>
     * <ul>
     *     <li>istanziare il server tramite il singleton {@link ServerApplication}</li>
     *     <li>avviarlo in ascolto sulla porta TCP predefinita</li>
     *     <li>accettare comandi da console per interrompere l’esecuzione</li>
     * </ul>
     *
     * <p>
     * Il metodo resta in ascolto sulla console finché l’utente
     * non digita uno dei comandi di terminazione.
     * </p>
     *
     * @param args argomenti da linea di comando (ignorati)
     */
    public static void main(String[] args) {

        int port = 12345; // Porta del server

        ServerApplication server = ServerApplication.getInstance();

        System.out.println("[MAIN] Avvio del server...");

        // --- Avvio Server ---
        if (!server.start(port)) {
            System.out.println("[MAIN] ERRORE: impossibile avviare il server.");
            return;
        }

        System.out.println("[MAIN] Server avviato sulla porta " + port);
        System.out.println("[MAIN] Digita 'quit', 'exit' o 'stop' per arrestarlo.");

        // --- Loop console amministrativo ---
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String cmd = scanner.nextLine();

                if (cmd.equalsIgnoreCase("quit")
                    || cmd.equalsIgnoreCase("exit")
                    || cmd.equalsIgnoreCase("stop")) {
                    break;
                }

                System.out.println("[MAIN] Comando sconosciuto: " + cmd);
            }
        }

        // --- Arresto Server ---
        System.out.println("[MAIN] Arresto del server...");
        server.stop();
        System.out.println("[MAIN] Server terminato correttamente.");
    }
}
