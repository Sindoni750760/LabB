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

public class User{

    private static String name;
    private static String surname;
    private static boolean loggedIn = false;
    private static boolean isRestaurateur = false;

    /**
     * Login utente tramite protocollo testuale
     */
    public static String login(String username, String password) throws IOException {
        ClientLogger.getInstance().info("User.login() - Sending login command for user: " + username);

        // invio comando login
        Communicator.send("login");
        Communicator.send(username);
        Communicator.send(password);

        // risposta del server
        String response = Communicator.read();
        ClientLogger.getInstance().info("User.login() - Server response: " + response);

        if (response == null) {
            ClientLogger.getInstance().error("User.login() - Server returned null");
            panic();
            return "error";
        }

        if (response.equals("ok")) {
            ClientLogger.getInstance().info("User.login() - Login OK, requesting user info");

            // login riuscito: salva ID interno
            // NON viene inviato l'ID qui → server lo salva nella sessione del socket, non lo manda
            // per semplicità, NON memorizziamo userId lato client (non lo usa)

            // ora servono le info utente
            Communicator.send("getUserInfo");

            ClientLogger.getInstance().info("User.login() - Waiting for user info");

            name = Communicator.read();
            ClientLogger.getInstance().info("User.login() - Received name: " + name);
            
            surname = Communicator.read();
            ClientLogger.getInstance().info("User.login() - Received surname: " + surname);
            
            String isRest = Communicator.read();
            ClientLogger.getInstance().info("User.login() - Received isRestaurateur: " + isRest);
            isRestaurateur = isRest.equals("y");

            loggedIn = true;
            ClientLogger.getInstance().info("User.login() - Login completed successfully");
        } else {
            ClientLogger.getInstance().warning("User.login() - Login failed with response: " + response);
        }

        return response;
    }

    /**
     * Logout locale + server
     */
    public static void logout() throws IOException {
        ClientLogger.getInstance().info("User.logout() - Sending logout command");
        Communicator.send("logout");
        String res = Communicator.read();
        
        ClientLogger.getInstance().info("User.logout() - Server response: " + res);

        if (res != null && res.equals("ok")) {
            loggedIn = false;
            name = null;
            surname = null;
            isRestaurateur = false;
            ClientLogger.getInstance().info("User.logout() - Logout completed successfully");
        } else {
            ClientLogger.getInstance().warning("User.logout() - Logout failed with response: " + res);
        }
    }

    /**
     * Reset di sicurezza
     */
    public static void panic() {
        loggedIn = false;
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
