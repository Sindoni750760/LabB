package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione del comando {@code quit},
 * inviato dal client per richiedere la chiusura della sessione.
 *
 * <p>
 * Effettua una disconnessione pulita seguendo l’ordine:
 * </p>
 *
 * <ol>
 *     <li>invio del messaggio di conferma {@code "bye"}</li>
 *     <li>chiusura della sessione applicativa</li>
 *     <li>chiusura del socket associato al client</li>
 * </ol>
 *
 * <p>
 * L’handler non esegue altre operazioni applicative e non effettua
 * persistenza lato database poiché la disconnessione non modifica lo stato
 * logico dell'utente, ma esclusivamente la sessione attuale.
 * </p>
 *
 * <p>Implementa il pattern singleton.</p>
 *
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class DisconnectHandler implements CommandHandler {

    /** Istanza singleton del DisconnectHandler. */
    private static DisconnectHandler instance = null;

    /**
     * Restituisce l'unica istanza dell'handler.
     *
     * @return istanza singleton
     */
    public static synchronized DisconnectHandler getInstance() {
        if (instance == null)
            instance = new DisconnectHandler();
        return instance;
    }

    /**
     * Costruttore privato per impedire istanziazione esterna.
     */
    private DisconnectHandler() {}

     /**
     * Gestisce il comando {@code "quit"} terminando la sessione del client.
     *
     * <p>
     * Se il comando non corrisponde, l'handler non effettua alcuna operazione
     * e ritorna {@code false}, delegando la gestione ad altri handler.
     * </p>
     *
     * @param cmd comando ricevuto dal client
     * @param ctx contesto di sessione del client
     *
     * @return {@code true} se il comando è stato gestito, {@code false} altrimenti
     *
     * @throws IOException se si verificano errori durante la comunicazione
     * @throws SQLException non utilizzata in questo handler ma dichiarata per coerenza con la firma
     */
    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException {

        if (!cmd.equals("quit"))
            return false;

        ctx.write("bye");
        ctx.deactivate();
        return true;
    }
}
