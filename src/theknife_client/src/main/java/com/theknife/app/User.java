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

    private static String name;
    private static String surname;
    private static boolean loggedIn = false;
    private static boolean isRestaurateur = false;

    private static int userId = -1;

    /**
     * Login utente tramite protocollo testuale
     */
    public static String login(String username, String password) throws IOException {

        // invio comando login
        Communicator.send("login");
        Communicator.send(username);
        Communicator.send(password);

        // risposta del server
        String response = Communicator.read();

        if (response == null) {
            panic();
            return "error";
        }

        if (response.equals("ok")) {

            // login riuscito: salva ID interno
            // NON viene inviato l’ID qui → server lo salva nella sessione del socket, non lo manda
            // per semplicità, NON memorizziamo userId lato client (non lo usa)
            // se vuoi recuperarlo, aggiungo un comando al server

            // ora servono le info utente
            Communicator.send("getUserInfo");

            name = Communicator.read();
            surname = Communicator.read();
            isRestaurateur = Communicator.read().equals("y");

            loggedIn = true;
        }

        return response;
    }

    /**
     * Logout locale + server
     */
    public static void logout() throws IOException {
        Communicator.send("logout");
        String res = Communicator.read();

        if (res != null && res.equals("ok")) {
            loggedIn = false;
            userId = -1;
            name = null;
            surname = null;
            isRestaurateur = false;
        }
    }

    /**
     * Reset di sicurezza
     */
    public static void panic() {
        loggedIn = false;
        userId = -1;
        name = null;
        surname = null;
        isRestaurateur = false;
    }

    /**
     * Informazioni utente attualmente loggato
     */
    public static String[] getInfo() {
        if (!loggedIn) return null;

        return new String[]{
                name,
                surname,
                isRestaurateur ? "y" : "n"
        };
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static boolean isRestaurateur() {
        return isRestaurateur;
    }
}
