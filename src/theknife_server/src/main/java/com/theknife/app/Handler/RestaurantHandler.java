package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Dispatcher unico per tutti i comandi relativi a ristoranti/recensioni/risposte/preferiti.
 * Delega la gestione a handler specializzati per responsabilità.
 */
public class RestaurantHandler implements CommandHandler {

    private static RestaurantHandler instance = null;

    public static synchronized RestaurantHandler getInstance() {
        if (instance == null)
            instance = new RestaurantHandler();
        return instance;
    }

    private final CommandHandler[] handlers = new CommandHandler[]{
            RestaurantCRUDHandler.getInstance(),
            RestaurantQueryHandler.getInstance(),
            ReviewHandler.getInstance(),
            ResponseHandler.getInstance(),
            FavouriteHandler.getInstance()
    };

    private RestaurantHandler() {}

    @Override
    public boolean handle(String cmd, ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        for (CommandHandler h : handlers) {
            if (h.handle(cmd, ctx))
                return true;
        }
        return false;
    }
}
