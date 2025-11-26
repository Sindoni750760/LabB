package com.theknife.app.Handler;

import java.util.List;

import com.theknife.app.Server.DBHandler;
import com.theknife.app.Server.QueryRestaurant;

public class RestaurantHandler implements CommandHandler {

    private static RestaurantHandler instance = null;
    private final QueryRestaurant db;

    private RestaurantHandler() {
        this.db = (QueryRestaurant) DBHandler.getInstance();
    }

    public static synchronized RestaurantHandler getInstance() {
        if (instance == null) instance = new RestaurantHandler();
        return instance;
    }

    @Override
    public boolean handle(String cmd, ClientContext ctx) {
        try{
            switch (cmd) {

            // RISTORANTI
            case "getRestaurants"       -> handleGetRestaurants(ctx);
            case "getRestaurantInfo"    -> handleGetRestaurantInfo(ctx);

            // CRUD RISTORANTI (solo ristoratore)
            case "addRestaurant"        -> handleAddRestaurant(ctx);
            case "editRestaurant"       -> handleEditRestaurant(ctx);
            case "deleteRestaurant"     -> handleDeleteRestaurant(ctx);

            // MIO RISTORANTI
            case "getMyRestaurants"     -> handleGetMyRestaurants(ctx);
            case "getMyRestaurantsPages" -> handleGetMyRestaurantsPages(ctx);

            // RECENSIONI
            case "getReviewsPages"      -> handleGetReviewsPages(ctx);
            case "getReviews"           -> handleGetReviews(ctx);
            case "addReview"            -> handleAddReview(ctx);
            case "editReview"           -> handleEditReview(ctx);
            case "removeReview"         -> handleRemoveReview(ctx);
            case "getMyReview"          -> handleGetMyReview(ctx);
            case "getMyReviewsPages"    -> handleGetMyReviewsPages(ctx);
            case "getMyReviews"         -> handleGetMyReviews(ctx);

            // RISPOSTE RISTORATORE
            case "getResponse"          -> handleGetResponse(ctx);
            case "addResponse"          -> handleAddResponse(ctx);
            case "editResponse"         -> handleEditResponse(ctx);
            case "removeResponse"       -> handleRemoveResponse(ctx);

            // PREFERITI
            case "isFavourite"          -> handleIsFavourite(ctx);
            case "addFavourite"         -> handleAddFavourite(ctx);
            case "removeFavourite"      -> handleRemoveFavourite(ctx);

            default -> { return false; }
        }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // =====================================================================
    //                               GET RESTAURANTS
    // =====================================================================

    private void handleGetRestaurants(ClientContext ctx) throws Exception {

        int page = Integer.parseInt(ctx.read());

        String lat        = ctx.read();
        String lon        = ctx.read();
        String rangeKm    = ctx.read();
        String priceMin   = ctx.read();
        String priceMax   = ctx.read();
        String hasDelivery = ctx.read();
        String hasOnline   = ctx.read();
        String starsMin    = ctx.read();
        String starsMax    = ctx.read();
        String onlyFavStr  = ctx.read();
        String hasCuisine  = ctx.read();

        boolean onlyFavourites = "y".equals(onlyFavStr);

        String category = null;
        if ("y".equals(hasCuisine)) {
            category = ctx.read();
        }

        Integer userId = ctx.getLoggedUserId();
        if (userId <= 0) userId = null;

        List<String[]> data = db.listRestaurants(
                page,
                lat, lon, rangeKm,
                priceMin, priceMax,
                hasDelivery, hasOnline,
                starsMin, starsMax,
                category,
                "-",             // nearMe (non usato)
                userId,
                onlyFavourites
        );

        // data.get(0) = ["pages", "count"]
        ctx.write("ok");
        ctx.write(data.get(0)[0]);   // numero pagine
        ctx.write(data.get(0)[1]);   // numero risultati in questa pagina

        for (int i = 1; i < data.size(); i++) {
            ctx.write(data.get(i)[0]);    // id
            ctx.write(data.get(i)[1]);    // nome
        }
    }

    // =====================================================================
    //                           INFO RISTORANTE
    // =====================================================================

    private void handleGetRestaurantInfo(ClientContext ctx) throws Exception {

        int id = Integer.parseInt(ctx.read());

        String[] info = db.getRestaurantInfo(id);

        if (info == null) {
            ctx.write("err");
            return;
        }

        ctx.write("ok");

        for (String s : info) {
            ctx.write(s);
        }
    }

    // =====================================================================
    //                       CRUD RISTORANTE (RISTORATORE)
    // =====================================================================

    private void handleAddRestaurant(ClientContext ctx) throws Exception {
        int owner = ctx.getLoggedUserId();

        String nome   = ctx.read();
        String naz    = ctx.read();
        String citta  = ctx.read();
        String ind    = ctx.read();
        double lat    = Double.parseDouble(ctx.read());
        double lon    = Double.parseDouble(ctx.read());
        int prezzo    = Integer.parseInt(ctx.read());
        boolean deliv = "y".equals(ctx.read());
        boolean onl   = "y".equals(ctx.read());
        String cucina = ctx.read();

        boolean ok = db.addRestaurant(owner, nome, naz, citta, ind, lat, lon, prezzo, deliv, onl, cucina);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleEditRestaurant(ClientContext ctx) throws Exception {

        int id      = Integer.parseInt(ctx.read());
        String nome = ctx.read();
        String naz  = ctx.read();
        String citta = ctx.read();
        String ind   = ctx.read();
        double lat   = Double.parseDouble(ctx.read());
        double lon   = Double.parseDouble(ctx.read());
        int prezzo   = Integer.parseInt(ctx.read());
        boolean deliv = "y".equals(ctx.read());
        boolean onl   = "y".equals(ctx.read());
        String cucina = ctx.read();

        boolean ok = db.editRestaurant(id, nome, naz, citta, ind, lat, lon, prezzo, deliv, onl, cucina);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleDeleteRestaurant(ClientContext ctx) throws Exception {

        int id = Integer.parseInt(ctx.read());

        boolean ok = db.deleteRestaurant(id);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleGetMyRestaurantsPages(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();

        // Verifica che l'utente sia loggato e sia un ristoratore
        if (userId <= 0) {
            ctx.write("unauthorized");
            return;
        }

        int pages = db.getMyRestaurantsPageCount(userId);

        ctx.write(Integer.toString(pages));
    }

    private void handleGetMyRestaurants(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();

        // Verifica che l'utente sia loggato
        if (userId <= 0) {
            ctx.write("0");
            return;
        }

        int page = Integer.parseInt(ctx.read());

        List<String[]> list = db.getMyRestaurants(userId, page);

        ctx.write(Integer.toString(list.size()));

        for (String[] r : list) {
            ctx.write(r[0]); // id
            ctx.write(r[1]); // nome
        }
    }

    // =====================================================================
    //                           RECENSIONI
    // =====================================================================

    private void handleGetReviewsPages(ClientContext ctx) throws Exception {

        int restId = Integer.parseInt(ctx.read());

        int pages = db.getReviewsPageCount(restId);

        ctx.write(Integer.toString(pages));
    }

    private void handleGetReviews(ClientContext ctx) throws Exception {

        int restId = Integer.parseInt(ctx.read());
        int page   = Integer.parseInt(ctx.read());

        List<String[]> list = db.getReviews(restId, page);

        ctx.write(Integer.toString(list.size()));

        for (String[] r : list) {
            ctx.write(r[0]); // id
            ctx.write(r[1]); // stelle
            ctx.write(r[2]); // testo
            ctx.write(r[3] == null ? "-" : r[3]); // risposta
        }
    }

    private void handleAddReview(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.addReview(userId, restId, stars, text);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleEditReview(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.editReview(userId, restId, stars, text);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleRemoveReview(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());

        boolean ok = db.removeReview(userId, restId);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleGetMyReview(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());

        String[] data = db.getMyReview(userId, restId);

        if (data == null) {
            ctx.write("none");
            return;
        }

        ctx.write("ok");
        ctx.write(data[0]); // stelle
        ctx.write(data[1]); // testo
    }

    private void handleGetMyReviewsPages(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();

        int pages = db.getMyReviewsPageCount(userId);

        ctx.write("ok");
        ctx.write(Integer.toString(pages));
    }

    private void handleGetMyReviews(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int page   = Integer.parseInt(ctx.read());

        List<String[]> list = db.getMyReviews(userId, page);

        ctx.write(Integer.toString(list.size()));

        for (String[] r : list) {
            ctx.write(r[1]); // nome ristorante
            ctx.write(r[2]); // stelle
            ctx.write(r[3]); // testo
        }
    }

    // =====================================================================
    //                              RISPOSTE
    // =====================================================================

    private void handleGetResponse(ClientContext ctx) throws Exception {

        int reviewId = Integer.parseInt(ctx.read());

        String text = db.getResponse(reviewId);

        ctx.write(text == null ? "-" : text);
    }

    private void handleAddResponse(ClientContext ctx) throws Exception {

        int reviewId = Integer.parseInt(ctx.read());
        String text  = ctx.read();

        boolean ok = db.addResponse(reviewId, text);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleEditResponse(ClientContext ctx) throws Exception {

        int reviewId = Integer.parseInt(ctx.read());
        String text  = ctx.read();

        boolean ok = db.editResponse(reviewId, text);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleRemoveResponse(ClientContext ctx) throws Exception {

        int reviewId = Integer.parseInt(ctx.read());

        boolean ok = db.removeResponse(reviewId);

        ctx.write(ok ? "ok" : "err");
    }

    // =====================================================================
    //                              PREFERITI
    // =====================================================================

    private void handleIsFavourite(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());

        boolean fav = db.isFavourite(userId, restId);

        ctx.write(fav ? "y" : "n");
    }

    private void handleAddFavourite(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());

        boolean ok = db.addFavourite(userId, restId);

        ctx.write(ok ? "ok" : "err");
    }

    private void handleRemoveFavourite(ClientContext ctx) throws Exception {

        int userId = ctx.getLoggedUserId();
        int restId = Integer.parseInt(ctx.read());

        boolean ok = db.removeFavourite(userId, restId);

        ctx.write(ok ? "ok" : "err");
    }
}
