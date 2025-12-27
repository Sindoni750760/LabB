package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.theknife.app.User;

/**
 * Gestore dei comandi relativi all'autenticazione lato server.
 * <p>
 * Implementa i comandi del protocollo di autenticazione testuale tra client e server:
 * </p>
 *
 * <ul>
 *     <li>{@code login} — validazione credenziali e inizializzazione sessione</li>
 *     <li>{@code register} — registrazione nuovo utente</li>
 *     <li>{@code logout} — invalidazione sessione corrente</li>
 *     <li>{@code getUserInfo} — restituzione delle informazioni dell'utente autenticato</li>
 * </ul>
 *
 * <p>
 * Questa classe funge da bridge tra:
 * </p>
 * <ul>
 *     <li>il protocollo di comunicazione lato socket fornito da {@link ClientContext}</li>
 *     <li>le logiche di business utente, incapsulate in {@link User}</li>
 * </ul>
 * 
 * <p>
 * La classe è un singleton e deve essere recuperata tramite {@link #getInstance()}.
 * </p>
 */

public class AuthHandler implements CommandHandler {

    /** Istanza singleton dell'AuthHandler. */
    private static AuthHandler instance = null;

    private static final Set<Integer> loggedInUserIds = Collections.synchronizedSet(new HashSet<>());

    /**
     * Restituisce l'unica istanza dell'handler.
     *
     * @return istanza singleton di {@link AuthHandler}
     */
    public static synchronized AuthHandler getInstance() {
        if (instance == null)
            instance = new AuthHandler();
        return instance;
    }

     /**
     * Servizio applicativo centralizzato per la gestione utenti.
     * Si occupa di:
     * <ul>
     *     <li>validazione credenziali</li>
     *     <li>creazione account</li>
     *     <li>recupero informazioni utente</li>
     * </ul>
     */
    private final User userService = User.getInstance();

    /**
     * Costruttore privato, utilizzato dal pattern Singleton.
     */
    private AuthHandler() {}
    
    /**
     * Gestisce un comando testuale fornito dal client.
     *
     * <p>Mappa i comandi supportati ai relativi metodi privati di gestione.
     * In particolare:</p>
     *
     * <ul>
     *     <li><code>login</code> → {@link #handleLogin(ClientContext)}</li>
     *     <li><code>register</code> → {@link #handleRegister(ClientContext)}</li>
     *     <li><code>logout</code> → {@link #handleLogout(ClientContext)}</li>
     *     <li><code>getUserInfo</code> → {@link #handleGetUserInfo(ClientContext)}</li>
     * </ul>
     *
     * @param cmd comando ricevuto dal client
     * @param ctx contesto di sessione, incapsula lettura/scrittura su socket
     * @return {@code true} se il comando è stato gestito, {@code false} altrimenti
     *
     * @throws IOException se si verificano problemi di I/O sulla socket
     * @throws SQLException se si verificano errori DB lato service
     * @throws InterruptedException se si verifica un'interruzione del thread handler
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
     * Gestisce il comando {@code login}.
     * <p>
     * Flusso del protocollo lato server:
     * </p>
     *
     * <ol>
     *     <li>legge username e password dal client</li>
     *     <li>interroga il servizio utenti</li>
     *     <li>risponde con uno dei valori:</li>
     * </ol>
     *
     * <ul>
     *     <li>{@code ok} → credenziali accettate</li>
     *     <li>{@code username} → utente non trovato</li>
     *     <li>{@code password} → password errata</li>
     * </ul>
     *
     * Se login riuscito → il contesto viene marcato come autenticato.
     *
     * @param ctx contesto di sessione
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

        synchronized(loggedInUserIds){
            if(loggedInUserIds.contains(id)){
                ctx.write("already_logged_in");
                return;
            }

            loggedInUserIds.add(id);
        }

        ctx.setLoggedUserId(id);
        ctx.write("ok");
    }

    /**
     * Gestisce il comando {@code register}.
     *
     * <p>Flusso:</p>
     * <ol>
     *     <li>legge i dati di registrazione dal client</li>
     *     <li>invoca {@link User#registerUser(...)}</li>
     *     <li>risponde al client con esito testuale</li>
     * </ol>
     *
     * <p>Possibili risposte:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code missing}</li>
     *     <li>{@code credentials} (username già esistente)</li>
     *     <li>{@code password} (non rispetta policy)</li>
     * </ul>
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
     * Gestisce il comando {@code logout}.
     * Invalida la sessione associata alla socket, azzerando {@code userId}.
    */
    private void handleLogout(ClientContext ctx) throws IOException {
        int userId = ctx.getLoggedUserId();

        if(userId > 0){
            synchronized (loggedInUserIds){
                loggedInUserIds.remove(userId);
            }
        }
        
        ctx.setLoggedUserId(-1);
        ctx.write("ok");
    }

    
    /**
     * Gestisce il comando {@code getUserInfo}.
     *
     * <p>Il server risponde inviando tre righe:</p>
     *
     * <pre>
     * nome
     * cognome
     * y|n   (ristoratore o utente normale)
     * </pre>
     *
     * <p>Se non è autenticato:</p>
     *
     * <pre>
     * ""
     * ""
     * n
     * </pre>
     */
    private void handleGetUserInfo(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

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

        ctx.write(info[0]); 
        ctx.write(info[1]); 
        ctx.write(info[2]); 
    }
    /**
     * Metodo statico per gestire disconnessioni improvvise,
     * richiamato quando un client si disconnette senza fare logout
     * @param userId Id dell'utente attualmente loggato
     */
    public static void handleClientDisconnect(int userId){
        if(userId > 0){
            synchronized(loggedInUserIds){
                loggedInUserIds.remove(userId);
            }
        }
    }

    /**
     * Metodo di debug che restituisce il numero di utenti attualmente loggati
     * @return il numero di utenti che hanno effettuato il login
     */
    public static int getLoggedInUsersCount(){
        synchronized(loggedInUserIds){
            return loggedInUserIds.size();
        }
    }
}

