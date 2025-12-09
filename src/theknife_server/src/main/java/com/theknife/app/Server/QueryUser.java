package com.theknife.app.Server;

import java.sql.SQLException;

/**
 * Interfaccia che definisce le principali operazioni relative agli utenti
 * nel database lato server.
 * <p>
 * Espone operazioni CRUD essenziali:
 * <ul>
 *     <li>Registrazione di un nuovo utente</li>
 *     <li>Verifica esistenza account</li>
 *     <li>Recupero credenziali (per login)</li>
 *     <li>Recupero informazioni personali (per profilo/logged user)</li>
 * </ul>
 * Implementata dal repository {@link DBHandler}.
 *
 * @author Mattia Sindoni 750760
 * @author Erica Faccio
 * @author Giovanni Isgrò
 */
public interface QueryUser {

    /**
     * Inserisce un nuovo utente nel database.
     * <p>
     * Se l'inserimento va a buon fine, l'utente viene registrato e potrà
     * successivamente autenticarsi tramite username/password.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco non già presente nel DB
     * @param passwordHashed hash della password salvato nel DB
     * @param dataNascita data di nascita, eventualmente null
     * @param lat latitudine del domicilio
     * @param lon longitudine del domicilio
     * @param isRistoratore true se l'utente è registrato come ristoratore
     * @return {@code true} se l'inserimento è avvenuto correttamente,
     *         {@code false} in caso contrario
     * @throws SQLException errore di scrittura sul database
     * @throws InterruptedException se l'operazione viene interrotta
     */
    boolean addUser(
            String nome,
            String cognome,
            String username,
            String passwordHashed,
            java.sql.Date dataNascita,
            double lat,
            double lon,
            boolean isRistoratore
    ) throws SQLException, InterruptedException;

    /**
     * Verifica se l'username indicato è già presente nel database.
     *
     * @param username username da controllare
     * @return {@code true} se esiste già un utente registrato con quel nome,
     *         {@code false} altrimenti
     * @throws SQLException errore di lettura dal database
     * @throws InterruptedException se l'operazione viene interrotta
     */
    boolean userExists(String username) throws SQLException, InterruptedException;

    /**
     * Recupera le informazioni necessarie per la fase di login.
     * <p>
     * Le informazioni restituite sono tipicamente:
     * <ul>
     *     <li>ID utente</li>
     *     <li>password hash</li>
     *     <li>flag ristoratore (nel protocollo standard)</li>
     * </ul>
     *
     * @param username username dell'utente che sta effettuando login
     * @return array contenente almeno:
     *         <pre>[id, hashedPassword]</pre>
     *         oppure {@code null} se nessun utente corrisponde
     * @throws SQLException errore di lettura dal database
     * @throws InterruptedException operazione interrotta
     */
    String[] getUserLoginInfo(String username) throws SQLException, InterruptedException;

    /**
     * Recupera le informazioni di un utente tramite il suo ID.
     * <p>
     * Restituisce i dati necessari alla visualizzazione del profilo
     * e/o invio di dati al client.
     *
     * @param id identificativo utente
     * @return array strutturato come:
     *         <pre>[nome, cognome, "y"/"n" isRistoratore]</pre>
     *         oppure {@code null} se l'utente non è presente
     * @throws SQLException errore di database
     * @throws InterruptedException se l'operazione viene interrotta
     */
    String[] getUserInfoById(int id) throws SQLException, InterruptedException;
}
