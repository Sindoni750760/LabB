package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione delle recensioni dei ristoranti.
 *
 * <p>
 * Gestisce tutte le operazioni relative alle recensioni, incluse:
 * </p>
 * <ul>
 *     <li>visualizzazione recensioni di un ristorante</li>
 *     <li>visualizzazione recensione dell'utente</li>
 *     <li>inserimento, modifica e rimozione recensioni</li>
 *     <li>consultazione delle recensioni scritte dall'utente</li>
 * </ul>
 *
 * <p>
 * L'handler utilizza il {@link DBHandler} come facade
 * per l'accesso al database e il {@link ClientContext}
 * per la comunicazione con il client.
 * </p>
 *
 */
public class ReviewHandler implements CommandHandler {

    private static ReviewHandler instance = null;

    /**
     * Restituisce l'unica istanza del {@code ReviewHandler}.
     *
     * @return istanza singleton dell'handler
     */
    public static synchronized ReviewHandler getInstance() {
        if (instance == null)
            instance = new ReviewHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private ReviewHandler() {}

    /**
     * Gestisce i comandi relativi alle recensioni.
     *
     * <p>
     * In base al comando ricevuto, il metodo delega
     * l'elaborazione al metodo di gestione specifico.
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
            case "getReviewsPages"     -> { handleGetReviewsPages(ctx); return true; }
            case "getReviewsPageCount" -> { handleGetReviewsPages(ctx); return true; }

            case "getReviews"          -> { handleGetReviews(ctx); return true; }
            case "getMyReview"         -> { handleGetMyReview(ctx); return true; }

            case "addReview"           -> { handleAddReview(ctx); return true; }
            case "editReview"          -> { handleEditReview(ctx); return true; }
            case "removeReview"        -> { handleRemoveReview(ctx); return true; }

            case "getMyReviewsPages"   -> { handleGetMyReviewsPages(ctx); return true; }
            case "getMyReviews"        -> { handleGetMyReviews(ctx); return true; }

            default -> { return false; }
        }
    }

    /**
     * Gestisce il comando {@code getReviewsPages}.
     *
     * <p>
     * Restituisce il numero di pagine di recensioni
     * associate a un ristorante.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("0");
            return;
        }

        int pages = db.getReviewsPageCount(restId);
        ctx.write(Integer.toString(pages));
    }

    /**
     * Gestisce il comando {@code getReviews}.
     *
     * <p>
     * Restituisce le recensioni di un ristorante
     * in forma paginata, includendo eventuali risposte
     * del ristoratore.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetReviews(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId, page;
        try {
            restId = Integer.parseInt(ctx.read());
            page   = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("0");
            return;
        }

        String[][] reviews = db.getReviews(restId, page);

        ctx.write(Integer.toString(reviews.length));

        for (String[] r : reviews) {
            String id     = r[0];
            String stars  = r[1];
            String text   = r[2];
            String reply  = r[3];

            ctx.write(id);
            ctx.write(stars);
            ctx.write(text);

            if (reply == null || reply.isEmpty()) {
                ctx.write("n");
            } else {
                ctx.write("y");
                ctx.write(reply);
            }
        }
    }

    /**
     * Gestisce il comando {@code getMyReview}.
     *
     * <p>
     * Restituisce la recensione scritta dall'utente
     * per un determinato ristorante, se presente.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetMyReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("0");
            ctx.write("");
            return;
        }

        int userId = ctx.getLoggedUserId();

        String[] r = db.getMyReview(userId, restId);

        if (r == null) {
            ctx.write("0");
            ctx.write("");
        } else {
            ctx.write(r[0]); // stelle
            ctx.write(r[1]); // testo
        }
    }

    /**
     * Gestisce il comando {@code addReview}.
     *
     * <p>
     * Inserisce una nuova recensione per un ristorante
     * da parte dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleAddReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId, stars;
        try {
            restId = Integer.parseInt(ctx.read());
            stars  = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        boolean ok = db.addReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Gestisce il comando {@code editReview}.
     *
     * <p>
     * Modifica una recensione esistente scritta
     * dall'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleEditReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId, stars;
        try {
            restId = Integer.parseInt(ctx.read());
            stars  = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        String text = ctx.read();

        boolean ok = db.editReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Gestisce il comando {@code removeReview}.
     *
     * <p>
     * Rimuove una recensione precedentemente
     * inserita dall'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleRemoveReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        boolean ok = db.removeReview(ctx.getLoggedUserId(), restId);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Gestisce il comando {@code getMyReviewsPages}.
     *
     * <p>
     * Restituisce il numero di pagine di recensioni
     * scritte dall'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetMyReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int userId = ctx.getLoggedUserId();
        int pages = db.getUserReviewsPages(userId);

        ctx.write("ok");
        ctx.write(Integer.toString(pages));
    }

    /**
     * Gestisce il comando {@code getMyReviews}.
     *
     * <p>
     * Restituisce l'elenco delle recensioni
     * scritte dall'utente autenticato in forma paginata.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetMyReviews(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page;
        try {
            page = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("0");
            return;
        }

        int userId = ctx.getLoggedUserId();

        String[][] data = db.getUserReviews(userId, page);

        ctx.write(Integer.toString(data.length));

        for (String[] r : data) {
            ctx.write(r[0]); // nome ristorante
            ctx.write(r[1]); // stelle
            ctx.write(r[2]); // testo
        }
    }
}
