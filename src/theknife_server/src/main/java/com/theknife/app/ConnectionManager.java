package com.theknife.app;

import java.io.*;
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
 * <p>Se il file di configurazione non è presente, viene generato automaticamente
 * con valori predefiniti.</p>
 *
 * <p>Il componente si occupa di:</p>
 * <ul>
 *     <li>Caricare il driver JDBC PostgreSQL</li>
 *     <li>Individuare (o creare) il file di configurazione</li>
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
     *
     * <ul>
     *     <li>Caricare il driver PostgreSQL</li>
     *     <li>Ricercare la cartella {@code LabB}</li>
     *     <li>Creare {@code connection.ini} se mancante</li>
     *     <li>Leggere i parametri dal file</li>
     * </ul>
     *
     * @throws RuntimeException se non è possibile individuare {@code LabB}
     *                          o se il file di configurazione risulta illeggibile.
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
     * Ricerca la cartella {@code LabB} risalendo progressivamente
     * dai percorsi parent della working directory corrente.
     *
     * <p>Ricerca effettuata con priorità:</p>
     * <ol>
     *     <li>Verifica presenza sottocartella denominata {@code LabB}</li>
     *     <li>Verifica se la directory corrente è {@code LabB}</li>
     *     <li>Risale nella gerarchia dei parent</li>
     * </ol>
     *
     * @return la directory radice del progetto oppure {@code null} se non individuata
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
     * Genera un file {@code connection.ini} con valori predefiniti.
     *
     * <p>Contenuto standard:</p>
     *
     * <pre>
     * jdbc_url=jdbc:postgresql://localhost:5432/theknife
     * username=postgres
     * password=
     * </pre>
     *
     * @throws RuntimeException se si verificano errori di scrittura
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
     * Carica i parametri di connessione dal file {@code connection.ini}.
     *
     * <p>Valori attesi:</p>
     * <ul>
     *     <li>{@code jdbc_url}</li>
     *     <li>{@code username}</li>
     *     <li>{@code password}</li>
     * </ul>
     *
     * @throws RuntimeException se il file è mancante,
     *                          danneggiato o con chiavi mancanti
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
     * Restituisce una connessione JDBC attiva verso il database configurato.
     *
     * <p>Il metodo ritorna ogni volta una nuova connessione, che deve essere chiusa
     * tramite try-with-resources:</p>
     *
     * <pre>
     * try (Connection conn = ConnectionManager.getInstance().getConnection()) {
     *     ...
     * }
     * </pre>
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
     * <p>Metodo tollerante verso argomenti {@code null};
     * equivale a {@link Connection#close()}.</p>
     *
     * @param c connessione da chiudere; può essere {@code null}
     */
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }
}
