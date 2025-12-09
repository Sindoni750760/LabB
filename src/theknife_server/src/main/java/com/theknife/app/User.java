package com.theknife.app;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.theknife.app.Server.DBHandler;

/**
 * Servizio singleton utilizzato lato server per la gestione degli utenti.
 * <p>
 * Si occupa di:
 * </p>
 * <ul>
 *     <li>registrazione utente</li>
 *     <li>validazione credenziali per il login</li>
 *     <li>recupero informazioni utente</li>
 * </ul>
 *
 * <p>
 * Questa classe rappresenta un livello logico superiore rispetto a {@link DBHandler},
 * che delega tutte le operazioni di accesso al database.
 * </p>
 *
 * <p>
 * Pattern applicato: Singleton.
 * </p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public class User {

    /** Istanza singleton del servizio. */
    private static User instance = null;

    /** Gestore della sicurezza (hashing/validazione password). */
    private final SecurityManager security;

    /** Access layer verso il database. */
    private final DBHandler db;

    /**
     * Costruttore privato.
     * Inizializza dipendenze interne.
     */
    private User() {
        this.security = SecurityManager.getInstance();
        this.db = DBHandler.getInstance();
    }

    /**
     * Restituisce l'istanza singleton del servizio {@link User}.
     *
     * @return unica istanza del servizio
     */
    public static synchronized User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    /**
     * Effettua la registrazione di un nuovo utente nel database.
     * <p>
     * Esegue le seguenti validazioni lato server:
     * </p>
     * <ul>
     *     <li>campi obbligatori non vuoti</li>
     *     <li>password conforme ai requisiti minimi</li>
     *     <li>formato delle coordinate numerico</li>
     *     <li>data di nascita nel formato yyyy-MM-dd</li>
     * </ul>
     *
     * <p>
     * Codici ritornati:
     * </p>
     * <ul>
     *     <li>{@code ok} → registrazione completata</li>
     *     <li>{@code missing} → campi mancanti</li>
     *     <li>{@code password} → password non valida</li>
     *     <li>{@code date} → data non conforme</li>
     *     <li>{@code coordinates} → coordinate non numeriche</li>
     *     <li>{@code credentials} → username già esistente</li>
     * </ul>
     *
     * @param nome nome dell’utente
     * @param cognome cognome dell’utente
     * @param username username desiderato
     * @param pw password in chiaro
     * @param dataNascita data di nascita formattata yyyy-MM-dd, oppure "-"
     * @param latStr latitudine come stringa
     * @param lonStr longitudine come stringa
     * @param isRistoratore flag booleano per tipologia utente
     *
     * @return esito dell'operazione come stringa simbolica
     *
     * @throws SQLException errore database
     * @throws InterruptedException thread interrotto
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

        if (nome.trim().isEmpty() || cognome.trim().isEmpty() || username.trim().isEmpty())
            return "missing";

        if (!security.checkPasswordStrength(pw))
            return "password";

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

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            return "coordinates";
        }

        boolean ok = db.addUser(
                nome,
                cognome,
                username,
                security.hashPassword(pw),
                birth,
                lat,
                lon,
                isRistoratore
        );

        return ok ? "ok" : "credentials";
    }

    /**
     * Effettua la verifica delle credenziali utente.
     * <p>
     * Ritorno previsto:
     * </p>
     * <ul>
     *     <li>ID utente → login valido</li>
     *     <li>-1 → username inesistente</li>
     *     <li>-2 → password non corretta</li>
     * </ul>
     *
     * @param username username fornito
     * @param pw password fornita in chiaro
     *
     * @return codice risultato numerico
     *
     * @throws SQLException errore database
     * @throws InterruptedException thread interrotto
     */
    public int loginUser(String username, String pw)
            throws SQLException, InterruptedException {

        String[] data = db.getUserLoginInfo(username);

        if (data == null)
            return -1; // username inesistente

        if (security.verifyPassword(pw, data[1]))
            return Integer.parseInt(data[0]); // autenticato

        return -2; // password errata
    }

    /**
     * Recupera le informazioni essenziali associate a un utente.
     *
     * <p>
     * Array di ritorno:
     * </p>
     *
     * <pre>
     * [0] nome
     * [1] cognome
     * [2] "y" / "n" → is_ristoratore
     * </pre>
     *
     * @param id id utente
     * @return array informazioni utente o null se non presente
     *
     * @throws SQLException errore SQL
     * @throws InterruptedException thread interrotto
     */
    public String[] getUserInfo(int id) throws SQLException, InterruptedException {
        return db.getUserInfo(id);
    }
}
