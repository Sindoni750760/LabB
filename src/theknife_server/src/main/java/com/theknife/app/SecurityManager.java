package com.theknife.app;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Manager singleton responsabile della gestione della sicurezza delle password.
 * <p>
 * Fornisce funzionalità di:
 * </p>
 * <ul>
 *     <li>Hashing sicuro delle password tramite BCrypt</li>
 *     <li>Verifica tra password in chiaro e hash</li>
 *     <li>Validazione della robustezza di una password</li>
 * </ul>
 *
 *
 * <p>Pattern utilizzato: Singleton</p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public class SecurityManager {

    /** Istanza singleton interna del SecurityManager. */
    private static SecurityManager instance = null;
    
    /** Encoder BCrypt utilizzato per hashing e matching. */
    private final BCryptPasswordEncoder encoder;

    /**
     * Costruttore privato che inizializza il BCryptPasswordEncoder.
     * <p>
     * L'istanza di encoder creata è thread-safe e riutilizzabile.
     * </p>
     */
    private SecurityManager() {
        encoder = new BCryptPasswordEncoder();
    }

    /**
     * Restituisce l'istanza singleton del manager.
     * <p>
     * L'accesso è sincronizzato per evitare race condition durante l'inizializzazione.
     * </p>
     *
     * @return unica istanza condivisa di {@code SecurityManager}
     */
    public static synchronized SecurityManager getInstance() {
        if (instance == null)
            instance = new SecurityManager();
        return instance;
    }

    /**
     * Genera l'hash BCrypt di una password in chiaro.
     *
     * <p>L'hash restituito ha il seguente formato:</p>
     * <pre>$2a$&lt;workfactor&gt;$&lt;salt+hash&gt;</pre>
     *
     * @param raw password in chiaro
     * @return stringa hash BCrypt che include salt e work factor
     */
    public String hashPassword(String raw) {
        return encoder.encode(raw);
    }

    /**
     * Verifica se una password in chiaro corrisponde all'hash BCrypt memorizzato.

     * <p>
     * BCrypt estrae automaticamente i dati e applica la stessa funzione crittografica.
     * </p>
     *
     * @param raw password inserita dall'utente
     * @param hash hash precedentemente salvato nel database
     * @return {@code true} se {@code raw} corrisponde all'hash, {@code false} altrimenti
     */
    public boolean verifyPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }

    /**
     * Verifica la robustezza sintattica di una password secondo criteri di sicurezza consigliati.
     * <p>
     * Requisiti verificati:
     * </p>
     * <ul>
     *     <li>lunghezza minima: 8 caratteri</li>
     *     <li>lunghezza massima: 32 caratteri</li>
     *     <li>almeno una minuscola</li>
     *     <li>almeno una maiuscola</li>
     *     <li>almeno una cifra numerica</li>
     *     <li>almeno un carattere speciale non alfanumerico</li>
     * </ul>
     *
     * <p>
     * Sono ignorati spazi, tabulazioni e whitespace come caratteri validi.
     * </p>
     *
     * @param pw password in chiaro da controllare
     * @return {@code true} se la password soddisfa tutti i vincoli, {@code false} altrimenti
     */
    public boolean checkPasswordStrength(String pw) {
        if (pw.length() < 8 || pw.length() > 32) return false;

        boolean lower = false;
        boolean upper = false;
        boolean digit = false;
        boolean special = false;

        for (char c : pw.toCharArray()) {
            if (Character.isLowerCase(c)) lower = true;
            else if (Character.isUpperCase(c)) upper = true;
            else if (Character.isDigit(c)) digit = true;
            else if (!Character.isWhitespace(c)) special = true;
        }

        return lower && upper && digit && special;
    }
}
