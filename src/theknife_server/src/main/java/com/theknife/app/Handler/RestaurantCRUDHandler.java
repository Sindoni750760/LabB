package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler specializzato: CRUD ristoranti + lista ristoranti del ristoratore.
 */
public class RestaurantCRUDHandler implements CommandHandler {

    private static RestaurantCRUDHandler instance = null;

    public static synchronized RestaurantCRUDHandler getInstance() {
        if (instance == null)
            instance = new RestaurantCRUDHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private RestaurantCRUDHandler() {}

    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "addRestaurant"         -> { handleAddRestaurant(ctx); return true; }
            case "editRestaurant"        -> { handleEditRestaurant(ctx); return true; }
            case "deleteRestaurant"      -> { handleDeleteRestaurant(ctx); return true; }

            case "getMyRestaurantsPages" -> { handleGetMyRestaurantsPages(ctx); return true; }
            case "getMyRestaurants"      -> { handleGetMyRestaurants(ctx); return true; }

            default -> { return false; }
        }
    }

    private void handleAddRestaurant(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int userId = ctx.getLoggedUserId();

        String name        = ctx.read();
        String nation      = ctx.read();
        String city        = ctx.read();
        String address     = ctx.read();
        String latStr      = ctx.read();
        String lonStr      = ctx.read();
        String priceStr    = ctx.read();
        String categories  = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();

        if (isBlank(name) || isBlank(nation) || isBlank(city) || isBlank(address) || isBlank(priceStr)) {
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

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("invalid");
            return;
        }

        int userId = ctx.getLoggedUserId();

        if (!db.hasAccess(userId, restId)) {
            ctx.write("denied");
            return;
        }

        String name        = ctx.read();
        String nation      = ctx.read();
        String city        = ctx.read();
        String address     = ctx.read();
        String latStr      = ctx.read();
        String lonStr      = ctx.read();
        String priceStr    = ctx.read();
        String categories  = ctx.read();
        String deliveryStr = ctx.read();
        String onlineStr   = ctx.read();

        if (isBlank(name) || isBlank(nation) || isBlank(city) || isBlank(address) || isBlank(priceStr)) {
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

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("invalid");
            return;
        }

        int userId = ctx.getLoggedUserId();

        if (!db.hasAccess(userId, restId)) {
            ctx.write("denied");
            return;
        }

        boolean ok = db.deleteRestaurant(restId);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleGetMyRestaurantsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int pages = db.getUserRestaurantsPages(ctx.getLoggedUserId());
        ctx.write(Integer.toString(pages));
    }

    private void handleGetMyRestaurants(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page;
        try {
            page = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("0");
            return;
        }

        int userId = ctx.getLoggedUserId();

        String[][] list = db.getUserRestaurants(userId, page);

        ctx.write(Integer.toString(list.length));
        for (String[] r : list) {
            ctx.write(r[0]); // id
            ctx.write(r[1]); // nome
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
