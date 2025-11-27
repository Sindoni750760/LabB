package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;
import com.theknife.app.User;

/**
 * Handler singleton per i comandi di autenticazione.
 * Gestisce login, registrazione, logout e recupero delle informazioni utente.
 * Responsabile di delegare al servizio User per la validazione e memorizzazione.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class AuthHandler implements CommandHandler {

    /** Istanza singleton dell'AuthHandler. */
    private static AuthHandler instance = null;

    /**
     * Restituisce l'istanza singleton dell'AuthHandler.
     *
     * @return istanza singleton
     */
    public static synchronized AuthHandler getInstance() {
        if (instance == null)
            instance = new AuthHandler();
        return instance;
    }

    /** Servizio per la gestione degli utenti. */
    private final User userService = User.getInstance();

    /**
     * Costruttore privato per il pattern singleton.
     */
    private AuthHandler() {}

    /**
     * Gestisce i comandi di autenticazione: login, register, logout, getUserInfo.
     *
     * @param cmd comando da gestire
     * @param ctx contesto della sessione client
     * @return true se il comando era riconosciuto, false altrimenti
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "login"       -> handleLogin(ctx);
            case "register"    -> handleRegister(ctx);
            case "logout"      -> handleLogout(ctx);
            case "getUserInfo" -> handleGetUserInfo(ctx);
            default -> { return false; }
        }

        return true;
    }

    /**
     * Gestisce il comando "login".
     * Legge username e password dal client e verifica le credenziali.
     *
     * @param ctx contesto della sessione client
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    private void handleLogin(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        String username = ctx.read();
        String password = ctx.read();

        int id = userService.loginUser(username, password);

        if (id == -1) {
            ctx.write("username");
            return;
        }

        if (id <= 0) {
            ctx.write("password");
            return;
        }

        ctx.setLoggedUserId(id);
        ctx.write("ok");
    }

    /**
     * Gestisce il comando "register".
     * Legge i dati di registrazione dal client e crea un nuovo account utente.
     *
     * @param ctx contesto della sessione client
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    private void handleRegister(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        String nome     = ctx.read();
        String cognome  = ctx.read();
        String username = ctx.read();
        String password = ctx.read();
        String nascita  = ctx.read();
        String latStr   = ctx.read();
        String lonStr   = ctx.read();
        boolean rist    = "y".equals(ctx.read());

        String esito = userService.registerUser(
                nome, cognome, username, password,
                nascita, latStr, lonStr, rist
        );

        ctx.write(esito);
    }

    /**
     * Gestisce il comando "logout".
     * Disconnette l'utente dalla sessione.
     *
     * @param ctx contesto della sessione client
     * @throws IOException se si verifica un errore di I/O
     */
    private void handleLogout(ClientContext ctx) throws IOException {
        ctx.setLoggedUserId(-1);
        ctx.write("ok");
    }

    private void handleGetUserInfo(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        /**
         * Gestisce il comando "getUserInfo".
         * Legge le informazioni dell'utente attualmente loggato dalla sessione
         * e le invia al client.
         *
         * @param ctx contesto della sessione client
         * @throws IOException se si verifica un errore di I/O
         * @throws SQLException se si verifica un errore di database
         * @throws InterruptedException se il thread viene interrotto
         */

        int id = ctx.getLoggedUserId();

        if (id <= 0) {
            ctx.write("");
            ctx.write("");
            ctx.write("n");
            return;
        }

        String[] info = userService.getUserInfo(id);

        if (info == null) {
            ctx.write("");
            ctx.write("");
            ctx.write("n");
            return;
        }

        ctx.write(info[0]); // nome
        ctx.write(info[1]); // cognome
        ctx.write(info[2]); // y/n is_ristoratore
    }
}

