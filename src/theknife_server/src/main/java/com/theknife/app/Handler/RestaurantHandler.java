package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler singleton per i comandi relativi ai ristoranti.
 * Gestisce le operazioni CRUD per ristoranti, recensioni, risposte ai commenti e preferiti.
 * Responsabile di delegare al DBHandler per l'accesso ai dati.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class RestaurantHandler implements CommandHandler {

    /** Istanza singleton del RestaurantHandler. */
    private static RestaurantHandler instance = null;

    /**
     * Restituisce l'istanza singleton del RestaurantHandler.
     *
     * @return istanza singleton
     */
    public static synchronized RestaurantHandler getInstance() {
        if (instance == null)
            instance = new RestaurantHandler();
        return instance;
    }

    /** Handler per le operazioni sul database. */
    private final DBHandler db = DBHandler.getInstance();

    /**
     * Costruttore privato per il pattern singleton.
     */
    private RestaurantHandler() {}

    /**
     * Gestisce i comandi relativi ai ristoranti: add, edit, delete, get,
     * e le operazioni su recensioni, risposte e preferiti.
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

            case "addRestaurant"          -> handleAddRestaurant(ctx);
            case "editRestaurant"         -> handleEditRestaurant(ctx);
            case "deleteRestaurant"       -> handleDeleteRestaurant(ctx);

            case "getRestaurants"         -> handleGetRestaurants(ctx);
            case "getRestaurantInfo"      -> handleGetRestaurantInfo(ctx);

            case "getMyRestaurantsPages"  -> handleGetMyRestaurantsPages(ctx);
            case "getMyRestaurants"       -> handleGetMyRestaurants(ctx);

            // pagine recensioni ristorante (il client usa "getReviewsPages")
            case "getReviewsPages"        -> handleGetReviewsPages(ctx);
            case "getReviewsPageCount"    -> handleGetReviewsPages(ctx); // alias compatibilità

            case "getReviews"             -> handleGetReviews(ctx);
            case "getMyReview"            -> handleGetMyReview(ctx);
            case "addReview"              -> handleAddReview(ctx);
            case "editReview"             -> handleEditReview(ctx);
            case "removeReview"           -> handleRemoveReview(ctx);

            case "getResponse"            -> handleGetResponse(ctx);
            case "addResponse"            -> handleAddResponse(ctx);
            case "editResponse"           -> handleEditResponse(ctx);
            case "removeResponse"         -> handleRemoveResponse(ctx);

            case "isFavourite"            -> handleIsFavourite(ctx);
            case "addFavourite"           -> handleAddFavourite(ctx);
            case "removeFavourite"        -> handleRemoveFavourite(ctx);

            // le mie recensioni (MyReviews controller)
            case "getMyReviewsPages"      -> handleGetMyReviewsPages(ctx);
            case "getMyReviews"           -> handleGetMyReviews(ctx);

            default -> { return false; }
        }

        return true;
    }

    
    private void handleAddRestaurant(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int userId = ctx.getLoggedUserId();

        String name    = ctx.read();
        String nation  = ctx.read();
        String city    = ctx.read();
        String address = ctx.read();
        String latStr  = ctx.read();
        String lonStr  = ctx.read();
        String priceStr = ctx.read();
        String categories = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();

        // campi obbligatori lato server
        if (isBlank(name) || isBlank(nation) || isBlank(city) ||
            isBlank(address) || isBlank(priceStr)) {
            ctx.write("missing");
            return;
        }

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            ctx.write("coordinates");
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            ctx.write("price_format");
            return;
        }

        if (price < 0) {
            ctx.write("price_negative");
            return;
        }

        boolean delivery = "y".equals(deliveryStr);
        boolean online   = "y".equals(onlineStr);

        boolean ok = db.addRestaurant(
                userId,
                name, nation, city, address,
                lat, lon,
                price,
                categories,
                delivery, online
        );

        ctx.write(ok ? "ok" : "error");
    }

    private void handleEditRestaurant(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int userId = ctx.getLoggedUserId();

        if (!db.hasAccess(userId, restId)) {
            ctx.write("denied");
            return;
        }

        String name    = ctx.read();
        String nation  = ctx.read();
        String city    = ctx.read();
        String address = ctx.read();
        String latStr  = ctx.read();
        String lonStr  = ctx.read();
        String priceStr = ctx.read();
        String categories = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();

        if (isBlank(name) || isBlank(nation) || isBlank(city) ||
            isBlank(address) || isBlank(priceStr)) {
            ctx.write("missing");
            return;
        }

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            ctx.write("coordinates");
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            ctx.write("price_format");
            return;
        }

        if (price < 0) {
            ctx.write("price_negative");
            return;
        }

        boolean delivery = "y".equals(deliveryStr);
        boolean online   = "y".equals(onlineStr);

        boolean ok = db.editRestaurant(
                restId,
                name, nation, city, address,
                lat, lon,
                price,
                categories,
                delivery, online
        );

        ctx.write(ok ? "ok" : "error");
    }

    private void handleDeleteRestaurant(ClientContext ctx)
                throws IOException, SQLException, InterruptedException {

            int restId = Integer.parseInt(ctx.read());
            int userId = ctx.getLoggedUserId();

            if (!db.hasAccess(userId, restId)) {
                ctx.write("denied");
                return;
            }

            boolean ok = db.deleteRestaurant(restId);
            ctx.write(ok ? "ok" : "error");
        }

        private void handleGetRestaurants(ClientContext ctx)
        throws IOException, SQLException, InterruptedException {

        int page = Integer.parseInt(ctx.read());

        // ---- PROTOCOLLO DAL CLIENT ----
        // mode: all | location | coordinates | invalid
        String mode      = ctx.read();
        String first     = ctx.read();
        String second    = ctx.read();
        String rangeStr  = ctx.read();
        String priceMinStr = ctx.read();
        String priceMaxStr = ctx.read();
        String categoryStr = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();
        String starsMinStr = ctx.read();
        String starsMaxStr = ctx.read();
        // (per ora non gestiamo "only favourites")
        boolean onlyFav = false;
        int favUserId   = -1;

        // ---- VARIABILI PER IL DB ----
        String nation = null;
        String city   = null;
        Double lat    = null;
        Double lon    = null;
        Double rangeKm = null;

        // ==============================
        // 1) INTERPRETAZIONE searchMode
        // ==============================
        switch (mode) {

            case "all" -> {
                // Nessun filtro geografico: prendi tutto
                nation = null;
                city   = null;
                lat    = null;
                lon    = null;
                rangeKm = null;
            }

            case "invalid" -> {
                // Input misto (es: Italia / 55) → nessun risultato, ma niente crash
                ctx.write("ok");
                ctx.write("1"); // 1 pagina
                ctx.write("0"); // 0 risultati
                return;
            }

            case "coordinates" -> {
                try {
                    lat = Double.parseDouble(first);
                    lon = Double.parseDouble(second);
                } catch (NumberFormatException e) {
                    ctx.write("coordinates");
                    return;
                }
            }

            case "location" -> {
                nation = first;
                city   = second;

                if (nation == null || nation.isBlank() ||
                    city   == null || city.isBlank()) {
                    ctx.write("location");
                    return;
                }
            }

            default -> {
                ctx.write("invalid");
                return;
            }
        }

        // ============================
        // 2) RANGE (solo con coordinate)
        // ============================
        if (!"-".equals(rangeStr)) {
            try {
                rangeKm = Double.parseDouble(rangeStr);
                if (rangeKm <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                ctx.write("coordinates");
                return;
            }

            if (!"coordinates".equals(mode)) {
                // Range ha senso solo con lat/lon
                ctx.write("coordinates");
                return;
            }
        }

        // ============================
        // 3) PREZZO
        // ============================
        Integer priceMin = null, priceMax = null;

        try {
            if (!"-".equals(priceMinStr)) priceMin = Integer.parseInt(priceMinStr);
            if (!"-".equals(priceMaxStr)) priceMax = Integer.parseInt(priceMaxStr);
        } catch (NumberFormatException e) {
            ctx.write("price");
            return;
        }

        if ((priceMin != null && priceMin < 0) ||
            (priceMax != null && priceMax < 0) ||
            (priceMin != null && priceMax != null && priceMin > priceMax)) {
            ctx.write("price");
            return;
        }

        // ============================
        // 4) STELLE
        // ============================
        Double starsMin = null, starsMax = null;

        try {
            if (!"-".equals(starsMinStr)) starsMin = Double.parseDouble(starsMinStr);
            if (!"-".equals(starsMaxStr)) starsMax = Double.parseDouble(starsMaxStr);
        } catch (NumberFormatException e) {
            ctx.write("stars");
            return;
        }

        if ((starsMin != null && (starsMin < 0 || starsMin > 5)) ||
            (starsMax != null && (starsMax < 0 || starsMax > 5)) ||
            (starsMin != null && starsMax != null && starsMin > starsMax)) {
            ctx.write("stars");
            return;
        }

        // ============================
        // 5) DELIVERY / ONLINE / CATEGORIA
        // ============================
        boolean delivery = "y".equals(deliveryStr);
        boolean online   = "y".equals(onlineStr);

        String category = null;
        if (categoryStr != null && !categoryStr.isBlank() && !"-".equals(categoryStr)) {
            category = categoryStr.trim();
        }

        // ============================
        // 6) QUERY DB
        // ============================
        String[][] data = db.getRestaurantsWithFilter(
                page,
                nation, city,
                lat, lon, rangeKm,
                priceMin, priceMax,
                delivery, online,
                starsMin, starsMax,
                favUserId,
                category
        );

        // ============================
        // 7) RISPOSTA AL CLIENT
        // ============================
        ctx.write("ok");
        ctx.write(data[0][0]); // pages
        ctx.write(data[0][1]); // size

        for (int i = 1; i < data.length; i++) {
            ctx.write(data[i][0]); // id
            ctx.write(data[i][1]); // nome
        }
    }




    private void handleGetRestaurantInfo(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());

        String[] info = db.getRestaurantInfo(restId);

        if (info == null) {
            for (int i = 0; i < 12; i++) ctx.write("");
            return;
        }

        for (String s : info)
            ctx.write(s);
    }

    private void handleGetMyRestaurantsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int pages = db.getUserRestaurantsPages(ctx.getLoggedUserId());
        ctx.write(Integer.toString(pages));
    }

    private void handleGetMyRestaurants(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page = Integer.parseInt(ctx.read());
        int userId = ctx.getLoggedUserId();

        String[][] list = db.getUserRestaurants(userId, page);

        ctx.write(Integer.toString(list.length));
        for (String[] r : list) {
            ctx.write(r[0]); // id
            ctx.write(r[1]); // nome
        }
    }

    private void handleGetReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int pages = db.getReviewsPageCount(restId);
        ctx.write(Integer.toString(pages));
    }

    private void handleGetReviews(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int page   = Integer.parseInt(ctx.read());

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

        int restId = Integer.parseInt(ctx.read());
        int userId = ctx.getLoggedUserId();

        String[] r = db.getMyReview(userId, restId);

        // Il client si aspetta SEMPRE 2 righe:
        // stelle (int) e testo (anche vuoto).
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

        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.addReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleEditReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.editReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleRemoveReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.removeReview(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    private void handleGetResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId = Integer.parseInt(ctx.read());
        String resp = db.getResponse(reviewId);

        // Il client si aspetta:
        //   "ok" + testo      se esiste
        //   qualsiasi altra cosa se non esiste
        if (resp == null) {
            ctx.write("none");
        } else {
            ctx.write("ok");
            ctx.write(resp);
        }
    }

    private void handleAddResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId = Integer.parseInt(ctx.read());
        String text  = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.addResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleEditResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId = Integer.parseInt(ctx.read());
        String text  = ctx.read();

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.editResponse(reviewId, text);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleRemoveResponse(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int reviewId = Integer.parseInt(ctx.read());

        if (!db.canRespond(ctx.getLoggedUserId(), reviewId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.removeResponse(reviewId);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleIsFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean fav = db.isFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(fav ? "y" : "n");
    }

    private void handleAddFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.addFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    private void handleRemoveFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.removeFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    private void handleGetMyReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int userId = ctx.getLoggedUserId();
        int pages = db.getUserReviewsPages(userId);

        // Il client si aspetta:
        //   "ok"
        //   pages
        ctx.write("ok");
        ctx.write(Integer.toString(pages));
    }

    private void handleGetMyReviews(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page   = Integer.parseInt(ctx.read());
        int userId = ctx.getLoggedUserId();

        String[][] data = db.getUserReviews(userId, page);

        // Il client si aspetta:
        //   size
        //   per ogni review: nome_ristorante, stelle, testo
        ctx.write(Integer.toString(data.length));

        for (String[] r : data) {
            ctx.write(r[0]); // nome ristorante
            ctx.write(r[1]); // stelle
            ctx.write(r[2]); // testo
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
