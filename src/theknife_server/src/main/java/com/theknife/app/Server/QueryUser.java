package com.theknife.app.Server;

import java.sql.SQLException;

/**
 * Interfaccia che definisce i metodi per le query utente.
 * Specifica le operazioni CRUD per gli utenti nel database.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public interface QueryUser {

    /**
     * Aggiunge un nuovo utente al database.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco
     * @param passwordHashed hash della password
     * @param dataNascita data di nascita
     * @param lat latitudine del domicilio
     * @param lon longitudine del domicilio
     * @param isRistoratore true se l'utente è un ristoratore
     * @return true se l'inserimento ha successo, false altrimenti
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
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
     * Verifica se un username esiste nel database.
     *
     * @param username username da verificare
     * @return true se l'username esiste, false altrimenti
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    boolean userExists(String username) throws SQLException, InterruptedException;

    /**
     * Recupera le credenziali di login per un utente.
     *
     * @param username username dell'utente
     * @return array contenente [id, hashPassword], oppure null se non trovato
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    String[] getUserLoginInfo(String username) throws SQLException, InterruptedException;

    /**
     * Recupera le informazioni di un utente.
     *
     * @param id ID dell'utente
     * @return array contenente [nome, cognome, "y"/"n" isRistoratore], oppure null se non trovato
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    String[] getUserInfoById(int id) throws SQLException, InterruptedException;

}
