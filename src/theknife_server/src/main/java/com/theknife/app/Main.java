package com.theknife.app;

import java.util.Scanner;

/**
 * Entry-point dell'applicazione server.
 *
 * <p>
 * La classe {@code Main} rappresenta il punto di ingresso dell'applicazione
 * lato server e coordina l'intero processo di avvio controllato del sistema.
 * </p>
 *
 * <p>
 * In particolare, questa classe si occupa di:
 * </p>
 * <ul>
 *     <li>verificare la presenza del file di configurazione {@code connection.ini}</li>
 *     <li>richiedere interattivamente le credenziali di accesso al database al primo avvio</li>
 *     <li>inizializzare il {@link ConnectionManager}</li>
 *     <li>avviare l'applicazione server tramite {@link ServerApplication}</li>
 *     <li>gestire comandi amministrativi da console</li>
 * </ul>
 *
 * <p>
 * Il server viene avviato esclusivamente se la configurazione del database
 * è stata correttamente caricata e validata.
 * </p>
 *
 * <p>
 * Porta TCP di default: <b>12345</b>.
 * </p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */

public final class Main {

    /**
     * Costruttore privato.
     *
     * <p>
     * Impedisce l'istanziazione della classe {@code Main}, che è utilizzata
     * esclusivamente come entry-point dell'applicazione.
     * </p>
     */
    private Main() { }

    /**
     * Metodo principale di avvio dell'applicazione server.
     *
     * <p>
     * Il metodo esegue le seguenti operazioni, in ordine sequenziale:
     * </p>
     * <ol>
     *     <li>verifica e/o crea il file {@code connection.ini}</li>
     *     <li>inizializza il {@link ConnectionManager}</li>
     *     <li>avvia il server sulla porta TCP configurata</li>
     *     <li>rimane in ascolto di comandi amministrativi da console</li>
     * </ol>
     *
     * <p>
     * L'esecuzione del metodo è bloccante fino alla ricezione di un comando
     * di terminazione.
     * </p>
     *
     * @param args argomenti da linea di comando (non utilizzati)
     */

    public static void main(String[] args) {

        final int port = 12345;
        System.out.println("[MAIN] Avvio server...");

        try (Scanner scanner = new Scanner(System.in)) {

            if (!ConfigurationPersistenceManager.ensureConfigurationExists(scanner)) {
                System.err.println("[MAIN] ERRORE: configurazione database non valida.");
                return;
            }

            try {
                ConnectionManager.getInstance();
            } catch (RuntimeException e) {
                System.err.println("[MAIN] Errore inizializzazione DB: " + e.getMessage());
                return;
            }

            ServerApplication server = ServerApplication.getInstance();
            if (!server.start(port)) {
                System.err.println("[MAIN] ERRORE: impossibile avviare il server.");
                return;
            }

            System.out.println("[MAIN] Server avviato sulla porta " + port);
            System.out.println("[MAIN] Digita 'quit', 'exit' o 'stop' per arrestarlo.");

            while (true) {
                String cmd = scanner.nextLine();
                if (cmd.equalsIgnoreCase("quit")
                        || cmd.equalsIgnoreCase("exit")
                        || cmd.equalsIgnoreCase("stop")) {
                    break;
                }
                System.out.println("[MAIN] Comando sconosciuto: " + cmd);
            }

            System.out.println("[MAIN] Arresto del server...");
            server.stop();
            System.out.println("[MAIN] Server terminato correttamente.");
        }
    }
}
