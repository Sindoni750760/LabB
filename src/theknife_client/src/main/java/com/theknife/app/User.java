package com.theknife.app;

import java.io.IOException;

/**
 * Classe statica che gestisce lo stato dell'utente attualmente autenticato nel client.
 *
 * <p>Fornisce funzionalità per:</p>
 * <ul>
 *     <li>eseguire il login e memorizzare le informazioni utente</li>
 *     <li>effettuare logout locale e remoto</li>
 *     <li>accedere ai dati utente sinché è valido lo stato di sessione</li>
 *     <li>ripristinare lo stato in caso di errore di comunicazione</li>
 * </ul>
 *
 * <p>Lo stato mantenuto non includes un identificativo numerico univoco,
 * in quanto la sessione viene gestita dal server in base al socket corrente.</p>
 *
 * @see Communicator
 */

public class User{
    /** Nome dell'utente autenticato. */
    private static String name;
    /** Cognome dell'utente autenticato. */
    private static String surname;
    /** Flag di stato utente autenticato. */
    private static boolean loggedIn = false;
    /** Indica se l'utente autenticato è un ristoratore. */
    private static boolean isRestaurateur = false;

     /**
     * Effettua il login presso il server usando il protocollo testuale.
     *
     * <p>Flusso logico:</p>
     * <ol>
     *     <li>Invia username e password al server</li>
     *     <li>Attende conferma della validità delle credenziali</li>
     *     <li>Richiede le informazioni associate all’utente autenticato</li>
     *     <li>Aggiorna lo stato locale del client</li>
     * </ol>
     *
     * @param username nome utente richiesto
     * @param password password in chiaro da autenticare
     * @return stringa di esito dal server:
     *         <ul>
     *             <li>"ok" → autenticazione riuscita</li>
     *             <li>"username" → utente inesistente</li>
     *             <li>"password" → credenziali errate</li>
     *             <li>"error" → errore interno</li>
     *         </ul>
     *
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */
    public static String login(String username, String password) throws IOException {
        ClientLogger.getInstance().info("User.login() - Sending login command for user: " + username);

        Communicator.send("login");
        Communicator.send(username);
        Communicator.send(password);

        String response = Communicator.read();
        ClientLogger.getInstance().info("User.login() - Server response: " + response);

        if (response == null) {
            ClientLogger.getInstance().error("User.login() - Server returned null");
            panic();
            return "error";
        }

        if (response.equals("ok")) {
            ClientLogger.getInstance().info("User.login() - Login OK, requesting user info");
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
     * Effettua il logout lato client e lato server.
     *
     * <p>Dopo il logout:</p>
     * <ul>
     *     <li>le informazioni utente vengono azzerate</li>
     *     <li>lo stato di sessione diventa non autenticato</li>
     * </ul>
     *
     * @throws IOException se si verifica un errore di comunicazione
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
     * Reset di emergenza dello stato utente.
     *
     * <p>Viene invocato in caso di errore di comunicazione o server offline.</p>
     *
     * <p>Effetti:</p>
     * <ul>
     *     <li>logout forzato</li>
     *     <li>rimozione delle informazioni memorizzate</li>
     * </ul>
     */
    public static void panic() {
        loggedIn = false;
        name = null;
        surname = null;
        isRestaurateur = false;
    }

    /**
     * Restituisce le informazioni dell’utente attualmente loggato.
     *
     * @return array di lunghezza 3 contenente:
     *         <pre>
     *         [0] → nome
     *         [1] → cognome
     *         [2] → "y" se ristoratore, altrimenti "n"
     *         </pre>
     *
     *         oppure {@code null} se non autenticato
     */
    public static String[] getInfo() {
        if (!loggedIn) return null;

        return new String[]{
                name,
                surname,
                isRestaurateur ? "y" : "n"
        };
    }

    /**
     * Indica se esiste un utente attualmente autenticato.
     *
     * @return {@code true} se l’utente risulta loggato, altrimenti {@code false}
     */
    public static boolean isLoggedIn() {
        return loggedIn;
    }
    
    /**
     * Indica se l’utente attualmente loggato è registrato come ristoratore.
     *
     * @return {@code true} se il profilo utente è di tipo "ristoratore"
     */
    public static boolean isRestaurateur() {
        return isRestaurateur;
    }
}
