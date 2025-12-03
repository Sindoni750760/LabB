package com.theknife.app;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestisce la connessione al database PostgreSQL tramite file di configurazione connection.ini.
 * Il file viene cercato automaticamente nella cartella "LabB", indipendentemente da dove
 * si trovi il JAR del server.
 *
 * Struttura attesa:
 *   LabB/
 *      connection.ini
 *
 * Se il file non esiste, viene creato automaticamente.
 */
public class ConnectionManager {

    private static ConnectionManager instance = null;

    private String jdbcUrl;
    private String username;
    private String password;

    private File iniFile;

    /** Caricamento del driver e setup */
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
            createIniFile();   // crea il template
        }

        loadIni();  // carica i valori
    }

    /** Singleton */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    // ============================================================
    //   1) RICERCA INTELLIGENTE DELLA CARTELLA "LabB"
    // ============================================================

    private File findLabBRoot() {
        File start = new File(System.getProperty("user.dir"));

        // 1) Se qualcuno esegue il jar DA una cartella che contiene LabB
        File sub = new File(start, "LabB");
        if (sub.exists() && sub.isDirectory()) return sub;

        // 2) Se siamo già dentro LabB
        if ("LabB".equalsIgnoreCase(start.getName())) return start;

        // 3) Risali verso la root
        File current = start;
        while (current != null) {

            // questa è LabB?
            if ("LabB".equalsIgnoreCase(current.getName()))
                return current;

            // contiene LabB come sottocartella?
            File child = new File(current, "LabB");
            if (child.exists() && child.isDirectory())
                return child;

            current = current.getParentFile();
        }

        return null;
    }

    // ============================================================
    //   2) CREAZIONE AUTOMATICA DEL FILE connection.ini
    // ============================================================

    private void createIniFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(iniFile))) {

            pw.println("jdbc_url=jdbc:postgresql://localhost:5432/theknife");
            pw.println("username=postgres");
            pw.println("password=");

        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare il file connection.ini", e);
        }
    }

    // ============================================================
    //   3) LETTURA DEL FILE connection.ini
    // ============================================================

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

    // ============================================================
    //   4) RESTITUISCE UNA NUOVA CONNESSIONE
    // ============================================================

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    // Compatibilità con versioni vecchie
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    public void flush() {
        // nessuna operazione necessaria
    }
}
