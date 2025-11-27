package com.theknife.app;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.theknife.app.Server.DBHandler;

/**
 * Service singleton per la gestione degli utenti lato server.
 * Fornisce metodi per registrazione, login e recupero delle informazioni utente.
 * Gestisce la validazione dei dati e l'interazione con il database.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class User {

    /** Istanza singleton del servizio User. */
    private static User instance = null;

    /** Manager per operazioni di sicurezza (hashing password). */
    private final SecurityManager security;
    
    /** Handler per le operazioni sul database. */
    private final DBHandler db;

    /**
     * Costruttore privato che inizializza i servizi dipendenti.
     */
    private User() {
        this.security = SecurityManager.getInstance();
        this.db = DBHandler.getInstance();
    }

    /**
     * Restituisce l'istanza singleton del servizio User.
     *
     * @return istanza singleton
     */
    public static synchronized User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    // ============================================================
    //                     REGISTRAZIONE
    // ============================================================

    /**
     * Registra un nuovo utente nel sistema.
     * Effettua validazioni lato server su campi obbligatori, robustezza password,
     * e formato delle coordinate.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco
     * @param pw password in chiaro
     * @param dataNascita data di nascita nel formato "yyyy-MM-dd" (o "-" se non specificata)
     * @param latStr latitudine del domicilio
     * @param lonStr longitudine del domicilio
     * @param isRistoratore true se l'utente è un ristoratore
     * @return "ok" se la registrazione ha successo,
     *         "missing" se campi obbligatori sono vuoti,
     *         "password" se la password non soddisfa i requisiti,
     *         "coordinates" se le coordinate sono invalide,
     *         "credentials" se l'username esiste già
     * @throws SQLException se si verifica un errore del database
     * @throws InterruptedException se il thread viene interrotto
     */
    public String registerUser(
            String nome,
            String cognome,
            String username,
            String pw,
            String dataNascita,
            String latStr,
            String lonStr,
            boolean isRistoratore
    ) throws SQLException, InterruptedException {

        // Campi obbligatori
        if (nome.trim().isEmpty() || cognome.trim().isEmpty() || username.trim().isEmpty())
            return "missing";

        // Password check
        if (!security.checkPasswordStrength(pw))
            return "password";

        // Data di nascita -> long
        long birth = -1L;
        if (!dataNascita.equals("-")) {
            try {
                birth = new SimpleDateFormat("yyyy-MM-dd")
                        .parse(dataNascita)
                        .getTime();
            } catch (ParseException e) {
                return "date";
            }
        }

        // Coordinate
        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            return "coordinates";
        }

        // Inserimento in DB
        boolean ok = db.addUser(
                nome, cognome, username,
                security.hashPassword(pw),
                birth, lat, lon, isRistoratore
        );

        // Il client si aspetta "credentials" quando l'username esiste già
        return ok ? "ok" : "credentials";
    }

    /**
     * Effettua il login di un utente verificando le credenziali.
     * Verifica che l'username esista e che la password sia corretta.
     *
     * @param username username dell'utente
     * @param pw password in chiaro
     * @return l'ID dell'utente se il login ha successo,
     *         -1 se l'username non esiste,
     *         -2 se la password è errata
     * @throws SQLException se si verifica un errore del database
     * @throws InterruptedException se il thread viene interrotto
     */
    public int loginUser(String username, String pw)
            throws SQLException, InterruptedException {

        String[] data = db.getUserLoginInfo(username);

        if (data == null)
            return -1; // username non trovato

        if (security.verifyPassword(pw, data[1]))
            return Integer.parseInt(data[0]); // login ok

        return -2; // password errata
    }

    /**
     * Recupera le informazioni di un utente dal database.
     *
     * @param id l'ID dell'utente
     * @return un array contenente [nome, cognome, "y"/"n"] dove l'ultimo elemento
     *         indica se l'utente è un ristoratore, oppure null se l'utente non esiste
     * @throws SQLException se si verifica un errore del database
     * @throws InterruptedException se il thread viene interrotto
     */
    public String[] getUserInfo(int id) throws SQLException, InterruptedException {
        return db.getUserInfo(id);  // <-- CORRETTO
    }
}
