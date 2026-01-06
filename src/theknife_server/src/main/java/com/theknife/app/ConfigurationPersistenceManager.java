package com.theknife.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

/**
 * Gestore della persistenza del file di configurazione {@code connection.ini}.
 *
 * <p>
 * Questa classe fornisce funzionalità per la verifica, creazione e validazione
 * della configurazione di accesso al database PostgreSQL.
 * </p>
 *
 * <p>
 * La configurazione è esterna al codice ed è persistente rispetto
 * ai riavvii dell'applicazione.
 * </p>
 */

public abstract class ConfigurationPersistenceManager {

    /**
     * Costruttore privato.
     */
    private ConfigurationPersistenceManager() { }

    /**
     * Verifica l'esistenza e la validità del file {@code connection.ini}.
     *
     * <p>
     * Se il file non esiste o risulta incompleto, viene richiesto
     * all'amministratore di inserire interattivamente i parametri
     * di connessione al database.
     * </p>
     *
     * @param scanner scanner utilizzato per la lettura dell'input da console
     * @return {@code true} se la configurazione è valida o è stata creata correttamente,
     *         {@code false} in caso di errore
     */

    public static boolean ensureConfigurationExists(Scanner scanner) {

        File root = ConnectionManager.findConfigurationRoot();
        if (root == null) {
            System.err.println("[CONFIG] Directory di configurazione non trovata.");
            return false;
        }

        File iniFile = new File(root, "connection.ini");

        if (!iniFile.exists() || !isConfigurationValid(iniFile)) {
            return createConfigurationFile(iniFile, scanner);
        }

        return true;
    }

    /**
     * Crea il file {@code connection.ini} richiedendo all'utente
     * i parametri di connessione al database.
     *
     * @param iniFile file di configurazione da creare
     * @param scanner scanner per la lettura dell'input da console
     * @return {@code true} se il file è stato creato correttamente
     */

    private static boolean createConfigurationFile(File iniFile, Scanner scanner) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {

            System.out.println("[CONFIG] Inserire parametri di connessione DB");

            System.out.print("Host (default localhost): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("Database (default theknife): ");
            String db = scanner.nextLine().trim();
            if (db.isEmpty()) db = "theknife";

            System.out.print("Username (default postgres): ");
            String user = scanner.nextLine().trim();
            if (user.isEmpty()) user = "postgres";

            System.out.print("Password: ");
            String pass = scanner.nextLine();

            pw.println("jdbc_url=jdbc:postgresql://" + host + ":5432/" + db);
            pw.println("username=" + user);
            pw.println("password=" + pass);

            System.out.println("[CONFIG] connection.ini creato in:");
            System.out.println("         " + iniFile.getAbsolutePath());

            return true;

        } catch (Exception e) {
            System.err.println("[CONFIG] Errore creazione connection.ini");
            return false;
        }
    }

    /**
     * Verifica che il file {@code connection.ini} contenga tutti
     * i parametri necessari per la connessione al database.
     *
     * @param iniFile file di configurazione da verificare
     * @return {@code true} se il file è valido e completo
     */

    public static boolean isConfigurationValid(File iniFile) {
        try {
            Properties prop = new Properties();
            try (var fis = new java.io.FileInputStream(iniFile)) {
                prop.load(fis);
            }

            return prop.getProperty("jdbc_url") != null
                && prop.getProperty("username") != null
                && prop.getProperty("password") != null
                && !prop.getProperty("password").isBlank();

        } catch (Exception e) {
            return false;
        }
    }
}
