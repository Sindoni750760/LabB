package com.theknife.app;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore centralizzato per l’accesso al database PostgreSQL.
 *
 * <p>La configurazione è contenuta nel file {@code connection.ini}, ricercato
 * automaticamente nella cartella radice del progetto denominata {@code LabB}.</p>
 *
 * <p>Struttura attesa del progetto:</p>
 *
 * <pre>
 * LabB/
 * ├─ connection.ini
 * └─ server.jar
 * </pre>
 *
 * <p>Il file {@code connection.ini} deve essere creato al primo avvio
 * del server dall'amministratore. Il client non gestisce in alcun modo
 * la configurazione del database.</p>
 *
 * <p>Il componente si occupa di:</p>
 * <ul>
 *     <li>Caricare il driver JDBC PostgreSQL</li>
 *     <li>Leggere i parametri di connessione</li>
 *     <li>Fornire connessioni JDBC attive</li>
 * </ul>
 *
 * <p>Pattern applicato: <b>Singleton</b>.</p>
 */
public class ConnectionManager {

    private static ConnectionManager instance = null;

    private String jdbcUrl;
    private String username;
    private String password;

    /** Percorso del file {@code connection.ini} individuato. */
    private File iniFile;

    /**
     * Costruttore privato.
     *
     * <p>Responsabilità:</p>
     * <ul>
     *     <li>Caricare il driver PostgreSQL</li>
     *     <li>Individuare la cartella {@code LabB}</li>
     *     <li>Leggere i parametri dal file {@code connection.ini}</li>
     * </ul>
     *
     * @throws RuntimeException se il file di configurazione è mancante
     *                          o contiene parametri non validi.
     */
    private ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL mancante.", e);
        }

        File labbRoot = findLabBRoot();
        if (labbRoot == null)
            throw new RuntimeException("Impossibile trovare la cartella 'LabB'.");

        iniFile = new File(labbRoot, "connection.ini");

        if (!iniFile.exists()) {
            throw new RuntimeException(
                "connection.ini mancante. Avviare il server per configurare il database."
            );
        }

        loadIni();

        if (password.isBlank()) {
            throw new RuntimeException(
                "Password PostgreSQL mancante in connection.ini"
            );
        }
    }

    /**
     * Restituisce l'istanza unica di {@link ConnectionManager}.
     *
     * @return l'istanza singleton
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    /**
     * Ricerca la cartella radice del progetto, la quale avviene nel seguente modo:
     * <ol>
     *     <li>Cerca una cartella denominata {@code LabB}</li>
     *     <li>Se la ricerca primaria fallisce, ricerca un file {@code pom.xml} (indicatore di radice Maven)</li>
     *     <li>Utilizza la working directory corrente come fallback</li>
     * </ol>
     *
     * @return la directory radice del progetto oppure {@code null}
     */
    static File findLabBRoot() {
        File current = new File(System.getProperty("user.dir"));

        // Strategia 1: Ricerca della cartella denominata "LabB"
        while (current != null) {
            if ("LabB".equalsIgnoreCase(current.getName()))
                return current;

            File child = new File(current, "LabB");
            if (child.exists() && child.isDirectory())
                return child;

            current = current.getParentFile();
        }

        // Strategia 2: Ricerca della radice Maven (file pom.xml)
        current = new File(System.getProperty("user.dir"));
        while (current != null) {
            File pomFile = new File(current, "pom.xml");
            if (pomFile.exists() && pomFile.isFile()) {
                System.out.println("[DB] Cartella 'LabB' non trovata. Utilizzo della radice Maven: " + current.getAbsolutePath());
                return current;
            }
            current = current.getParentFile();
        }

        // Strategia 3: Fallback alla working directory corrente
        File workingDir = new File(System.getProperty("user.dir"));
        System.out.println("[DB] AVVERTENZA: Utilizzo della working directory come radice: " + workingDir.getAbsolutePath());
        return workingDir;
    }

    /**
     * Carica i parametri di connessione dal file {@code connection.ini}.
     * Se il file è stato cancellato, viene ricreato automaticamente.
     *
     * @throws RuntimeException se il file è danneggiato o incompleto
     */
    private void loadIni() {
        // Verifica se il file è stato cancellato durante l'esecuzione
        if (!iniFile.exists()) {
            System.out.println("[DB] AVVERTENZA: connection.ini non trovato. Tentativo di ricreazione...");
            
            // Tenta di ricrearlo con i valori precedenti se disponibili
            if (!ConfigurationPersistenceManager.isConfigurationValid(iniFile)) {
                // Se non è possibile ricrearlo, il metodo lancerà un'eccezione
                throw new RuntimeException(
                    "connection.ini è stato cancellato e non può essere ricreato automaticamente. " +
                    "Avviare nuovamente il server per riconfigurare il database."
                );
            }
        }
        
        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(iniFile)) {
            prop.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella lettura del connection.ini", e);
        }

        jdbcUrl  = prop.getProperty("jdbc_url");
        username = prop.getProperty("username");
        password = prop.getProperty("password");

        if (jdbcUrl == null || username == null || password == null) {
            throw new RuntimeException("connection.ini non valido: parametri mancanti.");
        }
    }

    /**
     * Restituisce una connessione JDBC attiva verso il database configurato.
     *
     * @return una connessione aperta verso PostgreSQL
     * @throws SQLException se il database non è raggiungibile
     *                      o le credenziali non sono valide
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Rilascia una connessione precedentemente ottenuta.
     *
     * @param c connessione da chiudere; può essere {@code null}
     */
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Ricrea il file {@code connection.ini} se è stato cancellato,
     * utilizzando i parametri di configurazione attualmente caricati.
     *
     * <p>Questo metodo assicura la persistenza della configurazione
     * anche se il file viene eliminato durante l'esecuzione del server.</p>
     *
     * @return {@code true} se il file è stato ricreato correttamente,
     *         {@code false} altrimenti
     */
    public synchronized boolean restorePersistentConfiguration() {
        File labbRoot = findLabBRoot();
        
        if (labbRoot == null) {
            System.err.println("[DB] ERRORE: cartella 'LabB' non trovata per ripristino configurazione.");
            return false;
        }

        return ConfigurationPersistenceManager.recreateConfigurationWithValues(
            labbRoot, 
            this.jdbcUrl, 
            this.username, 
            this.password
        );
    }
}
