package com.theknife.app;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class ConnectionManager {

    private static ConnectionManager instance = null;

    private String jdbcUrl;
    private String user;
    private String pass;

    /** Percorso del file di configurazione */
    private final String CONFIG_PATH = "connection.ini";

    private ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            ServerLogger.getInstance().error("PostgreSQL JDBC driver not found: " + e.getMessage());
        }

        loadOrCreateConfig();
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    /**
     * Legge connection.ini se esiste, altrimenti ne crea uno con valori di default.
     */
    private void loadOrCreateConfig() {
        Properties props = new Properties();
        File f = new File(CONFIG_PATH);

        if (!f.exists()) {
            // crea file con valori predefiniti
            createDefaultConfig();
        }

        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);
        } catch (IOException e) {
            ServerLogger.getInstance().error("Impossibile leggere connection.ini: " + e.getMessage());
            createDefaultConfig();
        }

        jdbcUrl = props.getProperty("jdbc_url", "jdbc:postgresql://localhost:5432/theknife");
        user    = props.getProperty("username", "postgres");
        pass    = props.getProperty("password", "");

        ServerLogger.getInstance().info("Configurazione DB caricata correttamente.");
    }

    /**
     * Crea un file connection.ini con valori standard se non esiste.
     */
    private void createDefaultConfig() {
        Properties props = new Properties();

        props.setProperty("jdbc_url", "jdbc:postgresql://localhost:5432/theknife");
        props.setProperty("username", "postgres");
        props.setProperty("password", "");

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            props.store(fos, "Configurazione connessione database");
            ServerLogger.getInstance().info("Creato connection.ini predefinito");
        } catch (IOException e) {
            ServerLogger.getInstance().error("Impossibile creare connection.ini: " + e.getMessage());
        }
    }

    /** Ritorna una nuova connessione */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    public void flush() {
        // nessuna cache
    }
}
