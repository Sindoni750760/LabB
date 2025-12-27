package com.theknife.app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

/**
 * Gestore della persistenza del file di configurazione {@code connection.ini}.
 *
 * <p>Questa classe fornisce metodi per:</p>
 * <ul>
 *     <li>Verificare l'esistenza del file di configurazione</li>
 *     <li>Creare o ricrearne il file se assente</li>
 *     <li>Verificare l'integrità della configurazione</li>
 * </ul>
 *
 * <p>Se il file viene cancellato dopo l'avvio iniziale, il server lo ricrea
 * automaticamente al prossimo accesso, mantenendo la persistenza della configurazione.</p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public abstract class ConfigurationPersistenceManager {

    /**
     * Costruttore privato
     */
    private ConfigurationPersistenceManager(){

    }


    /**
     * Verifica l'esistenza e l'integrità del file {@code connection.ini}.
     * Se il file è mancante, lo ricrea interattivamente.
     *
     * <p>La directory radice è identificata con una strategia multi-livello:
     * ricerca della cartella "LabB", quindi del file {@code pom.xml},
     * quindi fallback alla working directory.</p>
     *
     * @param labbRoot directory radice del progetto (può essere qualsiasi cartella)
     * @param scanner  Scanner per la lettura dell'input da console
     * @return {@code true} se il file esiste e è valido, {@code false} altrimenti
     */
    public static boolean ensureConfigurationExists(File labbRoot, Scanner scanner) {
        if (labbRoot == null) {
            System.err.println("[CONFIG] ERRORE: directory radice del progetto non trovata.");
            return false;
        }

        File iniFile = new File(labbRoot, "connection.ini");

        // Se il file non esiste, crearlo
        if (!iniFile.exists()) {
            return createConfigurationFile(iniFile, scanner);
        }

        // Verificare l'integrità del file
        if (!isConfigurationValid(iniFile)) {
            System.err.println("[CONFIG] AVVERTENZA: connection.ini corrotto o incompleto.");
            System.out.println("[CONFIG] Ricreazione della configurazione in corso...");
            return createConfigurationFile(iniFile, scanner);
        }

        return true;
    }

    /**
     * Crea il file {@code connection.ini} chiedendo all'utente
     * i parametri di connessione al database.
     *
     * @param iniFile file da creare
     * @param scanner Scanner per la lettura dell'input da console
     * @return {@code true} se il file è stato creato correttamente
     */
    private static boolean createConfigurationFile(File iniFile, Scanner scanner) {
        try {
            try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {

                System.out.println("[CONFIG] Configurazione database richiesta.");
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

                System.out.println("[CONFIG] File connection.ini creato/ricreato correttamente in:");
                System.out.println("       " + iniFile.getAbsolutePath());

                return true;

            }
        } catch (Exception e) {
            System.err.println("[CONFIG] ERRORE durante la creazione di connection.ini.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica se il file {@code connection.ini} contiene
     * tutti i parametri necessari per la connessione al database.
     *
     * @param iniFile file di configurazione da verificare
     * @return {@code true} se tutti i parametri sono presenti
     */
    public static boolean isConfigurationValid(File iniFile) {
        if (!iniFile.exists()) {
            return false;
        }

        try {
            Properties prop = new Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(iniFile)) {
                prop.load(fis);
            }

            String jdbcUrl = prop.getProperty("jdbc_url");
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");

            // Tutti i parametri devono essere presenti e non vuoti
            return (jdbcUrl != null && !jdbcUrl.isBlank()) &&
                   (username != null && !username.isBlank()) &&
                   (password != null && !password.isBlank());

        } catch (Exception e) {
            System.err.println("[CONFIG] Errore nella verifica di connection.ini: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ricrea il file di configurazione se è stato cancellato
     * o è invalido, utilizzando i parametri passati.
     *
     * <p>Questo metodo è utile per il ripristino automatico
     * della configurazione se il file viene eliminato durante l'esecuzione.</p>
     *
     * @param labbRoot directory radice del progetto (qualsiasi directory valida)
     * @param jdbcUrl URL JDBC per la connessione
     * @param username nome utente per il database
     * @param password password per il database
     * @return {@code true} se il file è stato ricreato correttamente
     */
    public static boolean recreateConfigurationWithValues(File labbRoot, String jdbcUrl, String username, String password) {
        if (labbRoot == null || !labbRoot.exists()) {
            System.err.println("[CONFIG] ERRORE: directory radice del progetto non valida.");
            return false;
        }

        File iniFile = new File(labbRoot, "connection.ini");

        try {
            try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {
                pw.println("jdbc_url=" + jdbcUrl);
                pw.println("username=" + username);
                pw.println("password=" + password);

                System.out.println("[CONFIG] File connection.ini ricreato automaticamente.");
                return true;
            }
        } catch (Exception e) {
            System.err.println("[CONFIG] ERRORE durante la ricreazione di connection.ini: " + e.getMessage());
            return false;
        }
    }
}
