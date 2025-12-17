package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione CRUD dei ristoranti
 * e della visualizzazione dei ristoranti posseduti dal ristoratore.
 *
 * <p>
 * Gestisce i comandi del protocollo applicativo relativi
 * all'inserimento, modifica, cancellazione dei ristoranti
 * e al recupero della lista dei ristoranti associati all'utente autenticato.
 * </p>
 *
 * <p>
 * I comandi supportati sono:
 * </p>
 * <ul>
 *     <li>{@code addRestaurant}</li>
 *     <li>{@code editRestaurant}</li>
 *     <li>{@code deleteRestaurant}</li>
 *     <li>{@code getMyRestaurantsPages}</li>
 *     <li>{@code getMyRestaurants}</li>
 * </ul>
 *
 * <p>
 * L'handler delega la logica di persistenza al {@link DBHandler}
 * e utilizza il {@link ClientContext} per la comunicazione
 * con il client.
 * </p>
 *
 * <p>Pattern utilizzato: <b>Singleton</b></p>
 */
public class RestaurantCRUDHandler implements CommandHandler {

    private static RestaurantCRUDHandler instance = null;

    /**
     * Restituisce l'unica istanza del {@code RestaurantCRUDHandler}.
     *
     * @return istanza singleton dell'handler
     */
    public static synchronized RestaurantCRUDHandler getInstance() {
        if (instance == null)
            instance = new RestaurantCRUDHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private RestaurantCRUDHandler() {}

    /**
     * Gestisce i comandi relativi alla gestione dei ristoranti.
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
            case "addRestaurant"         -> { handleAddRestaurant(ctx); return true; }
            case "editRestaurant"        -> { handleEditRestaurant(ctx); return true; }
            case "deleteRestaurant"      -> { handleDeleteRestaurant(ctx); return true; }

            case "getMyRestaurantsPages" -> { handleGetMyRestaurantsPages(ctx); return true; }
            case "getMyRestaurants"      -> { handleGetMyRestaurants(ctx); return true; }

            default -> { return false; }
        }
    }

    /**
     * Gestisce il comando {@code addRestaurant}.
     *
     * <p>
     * Inserisce un nuovo ristorante associandolo
     * all'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Gestisce il comando {@code editRestaurant}.
     *
     * <p>
     * Modifica un ristorante esistente previa verifica
     * dei permessi di accesso dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Gestisce il comando {@code deleteRestaurant}.
     *
     * <p>
     * Elimina un ristorante esistente previa verifica
     * dei permessi di accesso dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Gestisce il comando {@code getMyRestaurantsPages}.
     *
     * <p>
     * Restituisce il numero di pagine di ristoranti
     * posseduti dall'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetMyRestaurantsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int pages = db.getUserRestaurantsPages(ctx.getLoggedUserId());
        ctx.write(Integer.toString(pages));
    }

    /**
     * Gestisce il comando {@code getMyRestaurants}.
     *
     * <p>
     * Restituisce la lista dei ristoranti posseduti
     * dall'utente autenticato per la pagina richiesta.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Verifica se una stringa è nulla o composta esclusivamente da spazi.
     *
     * @param s stringa da controllare
     * @return {@code true} se la stringa è nulla o vuota
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
