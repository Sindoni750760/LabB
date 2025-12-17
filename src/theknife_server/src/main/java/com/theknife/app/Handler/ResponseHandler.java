package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione delle risposte del ristoratore
 * alle recensioni degli utenti.
 *
 * <p>
 * Gestisce i comandi del protocollo applicativo relativi
 * al recupero, inserimento, modifica e rimozione delle risposte
 * associate alle recensioni.
 * </p>
 *
 * <p>
 * I comandi supportati sono:
 * </p>
 * <ul>
 *     <li>{@code getResponse}</li>
 *     <li>{@code addResponse}</li>
 *     <li>{@code editResponse}</li>
 *     <li>{@code removeResponse}</li>
 * </ul>
 *
 * <p>
 * L'handler delega le operazioni 
 * al {@link DBHandler} e utilizza il {@link ClientContext}
 * per la comunicazione con il client.
 * </p>
 *
 * <p>Pattern utilizzato: <b>Singleton</b></p>
 */
public class ResponseHandler implements CommandHandler {

    private static ResponseHandler instance = null;

    /**
     * Restituisce l'unica istanza del {@code ResponseHandler}.
     *
     * @return istanza singleton dell'handler
     */
    public static synchronized ResponseHandler getInstance() {
        if (instance == null)
            instance = new ResponseHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private ResponseHandler() {}

    /**
     * Gestisce i comandi relativi alle risposte alle recensioni.
     *
     * <p>
     * In base al comando ricevuto, il metodo delega
     * l'elaborazione al metodo specifico corrispondente.
     * </p>
     *
     * @param cmd comando ricevuto dal client
     * @param ctx contesto di sessione del client
     * @return {@code true} se il comando è stato gestito,
     *         {@code false} altrimenti
     *
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "getResponse"    -> { handleGetResponse(ctx); return true; }
            case "addResponse"    -> { handleAddResponse(ctx); return true; }
            case "editResponse"   -> { handleEditResponse(ctx); return true; }
            case "removeResponse" -> { handleRemoveResponse(ctx); return true; }
            default -> { return false; }
        }
    }

    /**
     * Gestisce il comando {@code getResponse}.
     *
     * <p>
     * Recupera la risposta associata a una recensione,
     * se presente, e la invia al client.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("none");
            return;
        }

        String resp = db.getResponse(reviewId);

        if (resp == null) {
            ctx.write("none");
        } else {
            ctx.write("ok");
            ctx.write(resp);
        }
    }

    /**
     * Gestisce il comando {@code addResponse}.
     *
     * <p>
     * Inserisce una nuova risposta a una recensione,
     * previa verifica dei permessi del ristoratore.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleAddResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.addResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Gestisce il comando {@code editResponse}.
     *
     * <p>
     * Modifica una risposta esistente associata a una recensione,
     * previa verifica dei permessi del ristoratore.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleEditResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.editResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Gestisce il comando {@code removeResponse}.
     *
     * <p>
     * Rimuove la risposta associata a una recensione,
     * previa verifica dei permessi del ristoratore.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleRemoveResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId;
        try {
            reviewId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.removeResponse(reviewId);
        ctx.write(ok ? "ok" : "error");
    }
}
