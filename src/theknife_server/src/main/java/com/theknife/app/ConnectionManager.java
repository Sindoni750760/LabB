package com.theknife.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manager singleton per la gestione delle connessioni al database PostgreSQL.
 * Responsabile della creazione di nuove connessioni e della configurazione del driver JDBC.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ConnectionManager {

    /** Istanza singleton del ConnectionManager. */
    private static ConnectionManager instance = null;

    /** URL JDBC per la connessione al database PostgreSQL. */
    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/theknife";
    
    /** Nome utente per l'autenticazione al database. */
    private final String user    = "postgres";
    
    /** Password per l'autenticazione al database. */
    private final String pass    = "Federico.2006";

    /**
     * Costruttore privato che carica il driver PostgreSQL JDBC.
     */
    private ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            ServerLogger.getInstance().error("PostgreSQL JDBC driver not found: " + e.getMessage());
        }
    }

    /**
     * Restituisce l'istanza singleton del ConnectionManager.
     *
     * @return istanza singleton
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

    /**
     * Ritorna una nuova connessione al database.
     * Le try-with-resources in DBHandler la chiuderanno automaticamente.
     *
     * @return una nuova connessione al database
     * @throws SQLException se si verifica un errore durante la connessione
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    /**
     * Compatibilità con eventuali chiamate esistenti: non fa nulla,
     * ma evita errori di compilazione se venisse usata.
     *
     * @param c connessione da chiudere
     */
    public void releaseConnection(Connection c) {
        if (c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Chiamata alla chiusura del server. Non teniamo cache,
     * quindi non serve fare nulla di particolare.
     */
    public void flush() {
        // Nessuna cache da svuotare nella versione semplificata.
    }
}
