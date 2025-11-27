package com.theknife.app;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Manager singleton per la gestione della sicurezza delle password.
 * Fornisce metodi per l'hashing delle password con BCrypt e la verifica della robustezza.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class SecurityManager {

    /** Istanza singleton del SecurityManager. */
    private static SecurityManager instance = null;
    
    /** Encoder BCrypt per l'hashing sicuro delle password. */
    private final BCryptPasswordEncoder encoder;

    /**
     * Costruttore privato che inizializza l'encoder BCrypt.
     */
    private SecurityManager() {
        encoder = new BCryptPasswordEncoder();
    }

    /**
     * Restituisce l'istanza singleton del SecurityManager.
     *
     * @return istanza singleton
     */
    public static synchronized SecurityManager getInstance() {
        if (instance == null)
            instance = new SecurityManager();
        return instance;
    }

    /**
     * Genera un hash bcrypt della password in chiaro.
     *
     * @param raw password in chiaro
     * @return hash bcrypt della password
     */
    public String hashPassword(String raw) {
        return encoder.encode(raw);
    }

    /**
     * Verifica che una password in chiaro corrisponda all'hash bcrypt memorizzato.
     *
     * @param raw password in chiaro da verificare
     * @param hash hash bcrypt da confrontare
     * @return true se la password corrisponde, false altrimenti
     */
    public boolean verifyPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }

    /**
     * Verifica la robustezza di una password secondo i seguenti criteri:
     * - Lunghezza: 8-32 caratteri
     * - Contiene almeno una lettera minuscola
     * - Contiene almeno una lettera maiuscola
     * - Contiene almeno una cifra
     * - Contiene almeno un carattere speciale
     *
     * @param pw password da verificare
     * @return true se la password soddisfa tutti i criteri, false altrimenti
     */
    public boolean checkPasswordStrength(String pw) {
        if (pw.length() < 8 || pw.length() > 32) return false;
        boolean lower = false, upper = false, digit = false, special = false;

        for (char c : pw.toCharArray()) {
            if (Character.isLowerCase(c)) lower = true;
            else if (Character.isUpperCase(c)) upper = true;
            else if (Character.isDigit(c)) digit = true;
            else if (!Character.isWhitespace(c)) special = true;
        }
        return lower && upper && digit && special;
    }
}
