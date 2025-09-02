package com.theknife.app;

import java.io.IOException;

/**
 * Classe statica che gestisce lo stato dell'utente attualmente loggato.
 * Fornisce metodi per effettuare login, logout e recuperare le informazioni dell'utente.
 * Supporta anche la disconnessione forzata in caso di errore di comunicazione.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class User {
    /** Nome dell'utente loggato. */
    private static String name;

    /** Cognome dell'utente loggato. */
    private static String surname;

    /** Indica se l'utente è attualmente loggato. */
    private static boolean logged_in = false;

    /** Indica se l'utente loggato è un ristoratore. */
    private static boolean is_restaurateur;


    /**
     * Effettua il login dell'utente sul client e sul server.
     * Se le credenziali sono corrette, recupera le informazioni dell'utente.
     *
     * @param username nome utente
     * @param password password
     * @return "ok" se il login ha successo, altrimenti un messaggio di errore
     * @throws IOException se si verifica un errore nella comunicazione
     */
    public static String login(String username, String password) throws IOException {        
        Communicator.sendStream("login");
        Communicator.sendStream(username);
        Communicator.sendStream(password);

        String response = Communicator.readStream();

        if(response.equals("ok")) {
            Communicator.sendStream("getUserInfo");
            name = Communicator.readStream();
            surname = Communicator.readStream();
            is_restaurateur = Communicator.readStream().equals("y");
            logged_in = true;
        }

        return response;
    }

    /**
     * Effettua il logout dell'utente sia sul client che sul server.
     * Se il logout ha successo, aggiorna lo stato interno.
     *
     * @throws IOException se si verifica un errore nella comunicazione
     */
    public static void logout() throws IOException {
        Communicator.sendStream("logout");
        if(Communicator.readStream().equals("ok"))
            logged_in = false;
    }

    /**
     * Disconnette forzatamente l'utente in caso di errore di comunicazione.
     * Utilizzato per gestire situazioni critiche.
     */
    public static void panic() {
        logged_in = false;
    }

    /**
     * Restituisce le informazioni dell'utente attualmente loggato.
     *
     * @return array contenente nome, cognome e ruolo ("y" se ristoratore), oppure {@code null} se non loggato
     */
    public static String[] getInfo() {
        if(!logged_in)
            return null;
        return new String[]{name, surname, is_restaurateur ? "y" : "n"};
    }
}