package com.theknife.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
 * <p>
 * Al primo avvio del server viene richiesta all'amministratore
 * la configurazione di accesso al database PostgreSQL, che viene
 * salvata nel file {@code connection.ini} nella directory {@code LabB}.
 * </p>
 *
 * <p>
 * Il server viene avviato solo dopo che la configurazione del database
 * è stata correttamente caricata e validata.
 * </p>
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
     * Costruttore privato per impedire l'istanziazione della classe.
     * 
     * <p>
     * La classe {@code Main} è utilizzata esclusivamente come entry-point dell'applicazione server e non deve essere istanziata
     * </p>
     */
    private Main(){

    }

    /**
     * Metodo principale di avvio del server.
     * <p>
     * Responsabilità del metodo:
     * </p>
     * <ul>
     *     <li>individuare la directory {@code LabB}</li>
     *     <li>verificare l'esistenza del file {@code connection.ini}</li>
     *     <li>richiedere le credenziali DB al primo avvio</li>
     *     <li>inizializzare il {@link ConnectionManager}</li>
     *     <li>avviare il server tramite {@link ServerApplication}</li>
     *     <li>gestire comandi amministrativi da console</li>
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

        int port = 12345; // Porta TCP del server

        System.out.println("[MAIN] Avvio bootstrap server...");

        // Scanner unico per tutta l'applicazione
        try (Scanner scanner = new Scanner(System.in)) {

            // --- Individuazione directory LabB ---
            File labbRoot = ConnectionManager.findLabBRoot();
            if (labbRoot == null) {
                System.err.println("[MAIN] ERRORE: impossibile individuare la cartella 'LabB'.");
                return;
            }

            File iniFile = new File(labbRoot, "connection.ini");

            // --- Prima esecuzione: creazione connection.ini ---
            if (!iniFile.exists()) {
                System.out.println("[MAIN] Prima esecuzione: configurazione database richiesta.");

                try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {

                    System.out.print("Host DB (default: localhost): ");
                    String host = scanner.nextLine().trim();
                    if (host.isEmpty()) host = "localhost";

                    System.out.print("Nome database (default: theknife): ");
                    String dbName = scanner.nextLine().trim();
                    if (dbName.isEmpty()) dbName = "theknife";

                    System.out.print("Username DB (default: postgres): ");
                    String user = scanner.nextLine().trim();
                    if (user.isEmpty()) user = "postgres";

                    System.out.print("Password DB: ");
                    String pass = scanner.nextLine();

                    pw.println("jdbc_url=jdbc:postgresql://" + host + ":5432/" + dbName);
                    pw.println("username=" + user);
                    pw.println("password=" + pass);

                    System.out.println("[MAIN] File connection.ini creato correttamente in:");
                    System.out.println("       " + iniFile.getAbsolutePath());

                } catch (Exception e) {
                    System.err.println("[MAIN] ERRORE durante la configurazione del database.");
                    e.printStackTrace();
                    return;
                }
            }

            // --- Inizializzazione DB (BLOCCANTE) ---
            try {
                ConnectionManager.getInstance();
            } catch (RuntimeException e) {
                System.err.println("[MAIN] Errore inizializzazione DB: " + e.getMessage());
                return;
            }

            // --- Avvio Server ---
            ServerApplication server = ServerApplication.getInstance();

            System.out.println("[MAIN] Avvio del server...");

            if (!server.start(port)) {
                System.err.println("[MAIN] ERRORE: impossibile avviare il server.");
                return;
            }

            System.out.println("[MAIN] Server avviato sulla porta " + port);
            System.out.println("[MAIN] Digita 'quit', 'exit' o 'stop' per arrestarlo.");

            // --- Loop comandi amministrativi ---
            while (true) {
                String cmd = scanner.nextLine();

                if (cmd.equalsIgnoreCase("quit")
                        || cmd.equalsIgnoreCase("exit")
                        || cmd.equalsIgnoreCase("stop")) {
                    break;
                }

                System.out.println("[MAIN] Comando sconosciuto: " + cmd);
            }

            // --- Arresto Server ---
            System.out.println("[MAIN] Arresto del server...");
            server.stop();
            System.out.println("[MAIN] Server terminato correttamente.");

        }
    }
}
