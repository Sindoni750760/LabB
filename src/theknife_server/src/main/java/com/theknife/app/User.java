package com.theknife.app;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.theknife.app.Server.DBHandler;

public class User {

    private static User instance = null;

    private final SecurityManager security;
    private final DBHandler db;

    private User() {
        this.security = SecurityManager.getInstance();
        this.db = DBHandler.getInstance();
    }

    public static synchronized User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    // ============================================================
    //                     REGISTRAZIONE
    // ============================================================

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

        return ok ? "ok" : "username";
    }

    // ============================================================
    //                        LOGIN
    // ============================================================

    public int loginUser(String username, String pw)
            throws SQLException, InterruptedException {

        String[] data = db.getUserLoginInfo(username);

        if (data == null)
            return -1; // username non trovato

        if (security.verifyPassword(pw, data[1]))
            return Integer.parseInt(data[0]); // login ok

        return -2; // password errata
    }

    // ============================================================
    //                     USER INFO
    // ============================================================

    /**
     * Ritorna [nome, cognome, "y" / "n"]
     */
    public String[] getUserInfo(int id) throws SQLException, InterruptedException {
        return db.getUserInfo(id);  // <-- CORRETTO
    }
}
