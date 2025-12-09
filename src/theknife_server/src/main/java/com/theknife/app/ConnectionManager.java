package com.theknife.app;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore centralizzato per l’accesso al database PostgreSQL.
 *
 * <p>La configurazione è contenuta nel file <b>connection.ini</b>, cercato
 * automaticamente nella cartella "LabB", indipendentemente dal percorso del JAR.</p>
 *
 * Struttura attesa del progetto:
 *
 * <pre>
 *   LabB/
 *      connection.ini
 *      server.jar
 *      ...
 * </pre>
 *
 * <p>Se il file non esiste, viene generato automaticamente con valori predefiniti.</p>
 *
 * Supporta:
 * <ul>
 *     <li>lettura parametri DB al bootstrap</li>
 *     <li>creazione del file di configurazione se assente</li>
 *     <li>fornitura delle connessioni SQL</li>
 * </ul>
 *
 * Pattern utilizzato: Singleton.
 */
public class ConnectionManager {

    private static ConnectionManager instance = null;

    private String jdbcUrl;
    private String username;
    private String password;

    /** Percorso completo del file di configurazione. */
    private File iniFile;

    /**
     * Costruttore privato.
     *
     * <p>Responsabilità:</p>
     * <ul>
     *     <li>caricamento del driver PostgreSQL</li>
     *     <li>ricerca della cartella LabB</li>
     *     <li>creazione del file di configurazione se assente</li>
     *     <li>lettura dei parametri dal file</li>
     * </ul>
     *
     * @throws RuntimeException se non è possibile localizzare "LabB" o leggere connection.ini
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
            createIniFile();
        }

        loadIni();
    }

    /**
     * Restituisce la singola istanza di ConnectionManager.
     *
     * @return istanza singleton
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    /**
     * Cerca la cartella "LabB" risalendo dal working directory corrente.
     *
     * <p>È prevista una ricerca a ritroso:</p>
     * <ol>
     *     <li>cerca una sottocartella chiamata LabB</li>
     *     <li>controlla se la working directory corrente è LabB</li>
     *     <li>risale successivamente la gerarchia dei parent</li>
     * </ol>
     *
     * @return File rappresentante la root della cartella LabB, oppure null se non trovata
     */
    private File findLabBRoot() {
        File start = new File(System.getProperty("user.dir"));

        File sub = new File(start, "LabB");
        if (sub.exists() && sub.isDirectory()) return sub;

        if ("LabB".equalsIgnoreCase(start.getName())) return start;

        File current = start;
        while (current != null) {
            if ("LabB".equalsIgnoreCase(current.getName()))
                return current;

            File child = new File(current, "LabB");
            if (child.exists() && child.isDirectory())
                return child;

            current = current.getParentFile();
        }
        return null;
    }

    /**
     * Genera un file connection.ini con valori di default.
     *
     * Struttura generata:
     * <pre>
     * jdbc_url=jdbc:postgresql://localhost:5432/theknife
     * username=postgres
     * password=
     * </pre>
     *
     * @throws RuntimeException se il file non può essere creato
     */
    private void createIniFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {
            pw.println("jdbc_url=jdbc:postgresql://localhost:5432/theknife");
            pw.println("username=postgres");
            pw.println("password=");
        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare il file connection.ini", e);
        }
    }

    /**
     * Carica i parametri dal file connection.ini.
     * Valori attesi:
     * <ul>
     *     <li>jdbc_url</li>
     *     <li>username</li>
     *     <li>password</li>
     * </ul>
     *
     * @throws RuntimeException se il file è mancante o incompleto
     */
    private void loadIni() {
        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(iniFile)) {
            prop.load(fis);
        } catch (IOException e) {
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
     * Restituisce una nuova connessione JDBC aperta verso PostgreSQL.
     *
     * <p>Ogni chiamata genera una nuova connessione che deve essere chiusa</p>
     * tramite:
     *
     * <pre>
     * @code {try (Connection conn = connMgr.getConnection()) { ... }}
     * </pre>
     *
     * @return una connessione attiva verso il DB configurato
     * @throws SQLException se i parametri sono errati o il DB non è raggiungibile
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Metodo di compatibilità con versioni precedenti del design.
     *
     * <p>Equivalente a conn.close(), ma tollera valori null.</p>
     *
     * @param c connessione da rilasciare (chiudere)
     */
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }
}
