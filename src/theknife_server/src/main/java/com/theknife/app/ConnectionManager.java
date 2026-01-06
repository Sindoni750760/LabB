package com.theknife.app;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore centralizzato delle connessioni JDBC verso PostgreSQL.
 *
 * <p>
 * Questa classe si occupa di caricare il driver JDBC, individuare dinamicamente
 * la directory di configurazione e leggere i parametri di connessione
 * dal file {@code connection.ini}.
 * </p>
 *
 * <p>
 * Il componente espone un'interfaccia semplificata per l'ottenimento e
 * il rilascio delle connessioni al database.
 * </p>
 *
 * <p>
 * Pattern architetturale adottato: <b>Singleton</b>.
 * </p>
 */

public final class ConnectionManager {

    private static ConnectionManager instance;

    private String jdbcUrl;
    private String username;
    private String password;

    private File iniFile;

    /**
     * Costruttore privato.
     *
     * <p>
     * Inizializza il gestore caricando il driver PostgreSQL e i parametri
     * di configurazione dal file {@code connection.ini}.
     * </p>
     *
     * @throws RuntimeException
     *         se il driver JDBC non è disponibile o se il file di configurazione
     *         è mancante o non valido
     */

    private ConnectionManager() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL mancante.", e);
        }

        File root = findConfigurationRoot();
        if (root == null) {
            throw new RuntimeException("Impossibile determinare la directory di configurazione.");
        }

        iniFile = new File(root, "connection.ini");
        if (!iniFile.exists()) {
            throw new RuntimeException("connection.ini mancante.");
        }

        loadIni();
    }

    /**
     * Restituisce l'istanza unica del {@link ConnectionManager}.
     *
     * <p>
     * L'istanza viene creata alla prima invocazione secondo il pattern Singleton.
     * </p>
     *
     * @return istanza singleton del gestore delle connessioni
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    /**
     * Individua la directory da utilizzare come radice per la configurazione.
     *
     * <p>
     * La ricerca avviene secondo una strategia multi-livello:
     * </p>
     * <ol>
     *     <li>cartella denominata {@code LabB}</li>
     *     <li>radice Maven identificata dal file {@code pom.xml}</li>
     *     <li>directory Desktop dell'utente</li>
     *     <li>working directory corrente come fallback</li>
     * </ol>
     *
     * @return directory di configurazione individuata
     */

    static File findConfigurationRoot() {

        File current = new File(System.getProperty("user.dir"));

        // Strategia 1: cartella LabB
        while (current != null) {
            if ("LabB".equalsIgnoreCase(current.getName())) {
                return current;
            }
            File child = new File(current, "LabB");
            if (child.exists() && child.isDirectory()) {
                return child;
            }
            current = current.getParentFile();
        }

        // Strategia 2: radice Maven (pom.xml)
        current = new File(System.getProperty("user.dir"));
        while (current != null) {
            File pom = new File(current, "pom.xml");
            if (pom.exists()) {
                System.out.println("[DB] Utilizzo radice Maven: " + current.getAbsolutePath());
                return current;
            }
            current = current.getParentFile();
        }

        // Strategia 3: Desktop
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists()) {
            System.out.println("[DB] Utilizzo Desktop come directory di configurazione.");
            return desktop;
        }

        // Fallback: working directory
        return new File(System.getProperty("user.dir"));
    }

    /**
     * Carica i parametri di connessione dal file {@code connection.ini}.
     *
     * @throws RuntimeException
     *         se il file non è leggibile o se i parametri richiesti
     *         sono mancanti o non validi
     */

    private void loadIni() {
        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(iniFile)) {
            prop.load(fis);
        } catch (Exception e) {
            throw new RuntimeException("Errore lettura connection.ini", e);
        }

        jdbcUrl  = prop.getProperty("jdbc_url");
        username = prop.getProperty("username");
        password = prop.getProperty("password");

        if (jdbcUrl == null || username == null || password == null || password.isBlank()) {
            throw new RuntimeException("connection.ini non valido.");
        }
    }

    /**
     * Fornisce una connessione JDBC attiva verso il database configurato.
     *
     * @return connessione JDBC aperta
     * @throws SQLException
     *         se il database non è raggiungibile o le credenziali non sono valide
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Rilascia una connessione JDBC precedentemente ottenuta.
     *
     * @param c connessione da chiudere; può essere {@code null}
     */
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }
}
