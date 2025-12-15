package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler specializzato: gestione preferiti utente.
 */
public class FavouriteHandler implements CommandHandler {

    private static FavouriteHandler instance = null;

    public static synchronized FavouriteHandler getInstance() {
        if (instance == null)
            instance = new FavouriteHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private FavouriteHandler() {}

    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        switch (cmd) {
            case "isFavourite"     -> { handleIsFavourite(ctx); return true; }
            case "addFavourite"    -> { handleAddFavourite(ctx); return true; }
            case "removeFavourite" -> { handleRemoveFavourite(ctx); return true; }
            default -> { return false; }
        }
    }

    private void handleIsFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("n");
            return;
        }

        boolean fav = db.isFavourite(ctx.getLoggedUserId(), restId);
        ctx.write(fav ? "y" : "n");
    }

    private void handleAddFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        boolean ok = db.addFavourite(ctx.getLoggedUserId(), restId);
        ctx.write(ok ? "ok" : "error");
    }

    private void handleRemoveFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId;
        try {
            restId = Integer.parseInt(ctx.read());
        } catch (NumberFormatException e) {
            ctx.write("error");
            return;
        }

        boolean ok = db.removeFavourite(ctx.getLoggedUserId(), restId);
        ctx.write(ok ? "ok" : "error");
    }
}
