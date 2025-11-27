package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler singleton per il comando "quit" (disconnessione client).
 * Gestisce la disconnessione pulita di un client dal server,
 * chiudendo la sessione e il socket.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class DisconnectHandler implements CommandHandler {

    /** Istanza singleton del DisconnectHandler. */
    private static DisconnectHandler instance = null;

    /**
     * Restituisce l'istanza singleton del DisconnectHandler.
     *
     * @return istanza singleton
     */
    public static synchronized DisconnectHandler getInstance() {
        if (instance == null)
            instance = new DisconnectHandler();
        return instance;
    }

    /**
     * Costruttore privato per il pattern singleton.
     */
    private DisconnectHandler() {}

    /**
     * Gestisce il comando "quit" disconnettendo il client.
     *
     * @param cmd comando da gestire
     * @param ctx contesto della sessione client
     * @return true se il comando era "quit", false altrimenti
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore di database
     */
    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException {

        if (!cmd.equals("quit"))
            return false;

        ctx.write("bye");
        ctx.close();
        return true;
    }
}
