package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler specializzato: gestione recensioni (ristorante + utente).
 */
public class ReviewHandler implements CommandHandler {

    private static ReviewHandler instance = null;

    public static synchronized ReviewHandler getInstance() {
        if (instance == null)
            instance = new ReviewHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private ReviewHandler() {}

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

    private void handleGetMyReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int userId = ctx.getLoggedUserId();
        int pages = db.getUserReviewsPages(userId);

        ctx.write("ok");
        ctx.write(Integer.toString(pages));
    }

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
