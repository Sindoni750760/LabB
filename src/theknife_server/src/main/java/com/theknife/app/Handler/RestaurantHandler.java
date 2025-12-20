package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Dispatcher centrale per tutti i comandi relativi a ristoranti,
 * recensioni, risposte e preferiti.
 *
 * <p>
 * Questa classe 
 * delega la gestione dei comandi a una sequenza ordinata di
 * handler specializzati, ciascuno responsabile di un sottoinsieme
 * del protocollo applicativo.
 * </p>
 *
 * <p>
 * Gli handler delegati includono:
 * </p>
 * <ul>
 *     <li>{@link RestaurantCRUDHandler}</li>
 *     <li>{@link RestaurantQueryHandler}</li>
 *     <li>{@link ReviewHandler}</li>
 *     <li>{@link ResponseHandler}</li>
 *     <li>{@link FavouriteHandler}</li>
 * </ul>
 *
 * <p>
 * Il primo handler che riconosce il comando ne gestisce
 * completamente l'elaborazione.
 * </p>
 *
 * Pattern utilizzato:
 *   <p>Singleton</p>
 * 
 */
public class RestaurantHandler implements CommandHandler {

    private static RestaurantHandler instance = null;
    
    /**
     * Restituisce l'unica istanza del {@code RestaurantHandler}.
     *
     * @return istanza singleton del dispatcher
     */
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

    /**
     * Instrada il comando ricevuto verso l'handler specializzato appropriato.
     *
     * <p>
     * Il metodo itera sugli handler registrati e delega
     * l'elaborazione al primo che riconosce il comando.
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

        for (CommandHandler h : handlers) {
            if (h.handle(cmd, ctx))
                return true;
        }
        return false;
    }
}
