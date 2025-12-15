package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler specializzato: ricerca ristoranti + info ristorante.
 */
public class RestaurantQueryHandler implements CommandHandler {

    private static RestaurantQueryHandler instance = null;

    public static synchronized RestaurantQueryHandler getInstance() {
        if (instance == null)
            instance = new RestaurantQueryHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private RestaurantQueryHandler() {}

    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "getRestaurants"    -> { handleGetRestaurants(ctx); return true; }
            case "getRestaurantInfo" -> { handleGetRestaurantInfo(ctx); return true; }
            default -> { return false; }
        }
    }

    private void handleGetRestaurants(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page;
        try {
            page = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("invalid");
            return;
        }

        // ---- PROTOCOLLO DAL CLIENT ----
        String mode        = ctx.read();  // all | location | coordinates | invalid
        String first       = ctx.read();
        String second      = ctx.read();
        String rangeStr    = ctx.read();
        String priceMinStr = ctx.read();
        String priceMaxStr = ctx.read();
        String categoryStr = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();
        String starsMinStr = ctx.read();
        String starsMaxStr = ctx.read();
        String onlyFavStr  = ctx.read();

        boolean onlyFav = "y".equalsIgnoreCase(onlyFavStr);
        int favUserId   = onlyFav ? ctx.getLoggedUserId() : -1;

        // ---- VARIABILI PER IL DB ----
        String nation = null;
        String city   = null;
        Double lat    = null;
        Double lon    = null;
        Double rangeKm = null;

        switch (mode) {

            case "all" -> { /* no filters */ }

            case "invalid" -> {
                ctx.write("ok");
                ctx.write("1");
                ctx.write("0");
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

                if (nation == null || nation.isBlank() || city == null || city.isBlank()) {
                    ctx.write("location");
                    return;
                }
            }

            default -> {
                ctx.write("invalid");
                return;
            }
        }

        if (!"-".equals(rangeStr)) {
            try {
                rangeKm = Double.parseDouble(rangeStr);
                if (rangeKm <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                ctx.write("coordinates");
                return;
            }

            if (!"coordinates".equals(mode)) {
                ctx.write("coordinates");
                return;
            }
        }

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

        boolean delivery = "y".equals(deliveryStr);
        boolean online   = "y".equals(onlineStr);

        String category = null;
        if (categoryStr != null && !categoryStr.isBlank() && !"-".equals(categoryStr)) {
            category = categoryStr.trim();
        }

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

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            // risposta coerente: 12 vuoti
            for (int i = 0; i < 12; i++) ctx.write("");
            return;
        }

        String[] info = db.getRestaurantInfo(restId);

        if (info == null) {
            for (int i = 0; i < 12; i++) ctx.write("");
            return;
        }

        for (String s : info)
            ctx.write(s);
    }
}
