package com.theknife.app;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Classe utility per la gestione lato server degli utenti.
 * Fornisce metodi per la registrazione e il login, con validazione dei dati
 * e protezione delle password tramite hashing BCrypt.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class User {
    /**
     * Applica l'hashing alla password usando l'algoritmo BCrypt.
     *
     * @param password la password in chiaro da proteggere
     * @return la password hashata
     */
    //https://dzone.com/articles/secure-password-hashing-in-java
    private static String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * Verifica se una password in chiaro corrisponde all'hash memorizzato.
     *
     * @param inputPassword la password inserita dall'utente
     * @param storedHash l'hash memorizzato nel database
     * @return {@code true} se la password è corretta, {@code false} altrimenti
     */
    private static boolean verifyPassword(String inputPassword, String storedHash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(inputPassword, storedHash);
    }

    /**
     * Verifica che la password rispetti i requisiti di sicurezza:
     * - lunghezza tra 8 e 32 caratteri
     * - almeno una lettera minuscola
     * - almeno una lettera maiuscola
     * - almeno un numero
     * - almeno un carattere speciale
     *
     * @param password la password da validare
     * @return {@code true} se la password è valida, {@code false} altrimenti
     */
    private static boolean checkPassword(String pw) {
    if (pw.length() < 8 || pw.length() > 32) return false;
    boolean lower=false, upper=false, digit=false, special=false;
    for (char c: pw.toCharArray()) {
        if (Character.isLowerCase(c)) lower = true;
        else if (Character.isUpperCase(c)) upper = true;
        else if (Character.isDigit(c)) digit = true;
        else if (!Character.isWhitespace(c)) special = true;
    }
    return lower && upper && digit && special;
    }


    /**
     * Registra un nuovo utente nel sistema.
     * Valida i parametri, verifica la password, controlla la data di nascita e le coordinate,
     * e invia i dati al database.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username nome utente scelto
     * @param password password in chiaro
     * @param data_nascita data di nascita in formato "yyyy-MM-dd" oppure "-"
     * @param latitude latitudine come stringa
     * @param longitude longitudine come stringa
     * @param is_ristoratore {@code true} se l'utente è un ristoratore
     * @return "ok" se la registrazione ha successo, altrimenti un codice di errore:
     *         "missing", "password", "date", "coordinates", "username"
     * @throws SQLException se si verifica un errore nella comunicazione col database
     */
    public static String registerUser(String nome, String cognome, String username, String password, String data_nascita, String latitude, String longitude, boolean is_ristoratore) throws SQLException {
        //checks missing parameters
        if(nome.trim().isEmpty() || cognome.trim().isEmpty() || username.trim().isEmpty())
            return "missing";

        if(!checkPassword(password))
            return "password";

        //handles birth date
        long time = -1;
        try {
            if(!data_nascita.equals("-"))
                time = new SimpleDateFormat("yyyy-MM-dd").parse(data_nascita).getTime();
        } catch(ParseException e) {
            return "date";
        }

        //is the birth date is after today, returns the "date" error
        if(time > new java.util.Date().getTime())
            return "date";

         double latitude_double, longitude_double;
        try {
            latitude_double = Double.parseDouble(latitude);
            longitude_double = Double.parseDouble(longitude);
        } catch(NumberFormatException e) {
            return "coordinates";
        }

        return DBHandler.addUser(nome, cognome, username, hashPassword(password), time, latitude_double, longitude_double, is_ristoratore) ? "ok" : "username";

    }

    /**
     * Esegue il login dell'utente verificando username e password.
     *
     * @param username nome utente
     * @param password password in chiaro
     * @return ID utente se il login ha successo,
     *         -1 se l'utente non esiste,
     *         -2 se la password è errata
     * @throws SQLException se si verifica un errore nella comunicazione col database
     */
    public static int loginUser(String username, String password) throws SQLException {
        String[] user_info = DBHandler.getUserLoginInfo(username);

        //if no user was found with given username
        if(user_info == null)
            return -1;

        if(verifyPassword(password, user_info[1]))
            return Integer.parseInt(user_info[0]);

        //password missmatch
        return -2;
    }
}
