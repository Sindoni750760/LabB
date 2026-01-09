package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione dei ristoranti preferiti dell'utente.
 *
 * <p>
 * Gestisce i comandi del protocollo applicativo relativi
 * all'aggiunta, rimozione e verifica dei ristoranti preferiti.
 * </p>
 *
 * <p>
 * I comandi supportati sono:
 * </p>
 * <ul>
 *     <li>{@code isFavourite}</li>
 *     <li>{@code addFavourite}</li>
 *     <li>{@code removeFavourite}</li>
 * </ul>
 *
 * <p>
 * L'handler delega le operazioni di persistenza al
 * {@link DBHandler} e utilizza il {@link ClientContext}
 * per la comunicazione con il client.
 * </p>
 *
 * <p>Pattern utilizzato: <b>Singleton</b></p>
 */
public class FavouriteHandler implements CommandHandler {

    
    private static FavouriteHandler instance = null;

    /**
     * Restituisce l'unica istanza del {@code FavouriteHandler}.
     *
     * @return istanza singleton dell'handler
     */
    public static synchronized FavouriteHandler getInstance() {
        if (instance == null)
            instance = new FavouriteHandler();
        return instance;
    }

    private final DBHandler db = DBHandler.getInstance();

    private FavouriteHandler() {}

    /**
     * Gestisce i comandi relativi ai ristoranti preferiti.
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
            case "isFavourite"     -> { handleIsFavourite(ctx); return true; }
            case "addFavourite"    -> { handleAddFavourite(ctx); return true; }
            case "removeFavourite" -> { handleRemoveFavourite(ctx); return true; }
            case "getFavourites" -> {handleGetFavourites(ctx); return true;}
            default -> { return false; }
        }
    }

    /**
     * Gestisce il comando {@code isFavourite}.
     *
     * <p>
     * Verifica se il ristorante indicato è presente
     * tra i preferiti dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Gestisce il comando {@code addFavourite}.
     *
     * <p>
     * Aggiunge il ristorante indicato alla lista
     * dei preferiti dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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

    /**
     * Gestisce il comando {@code removeFavourite}.
     *
     * <p>
     * Rimuove il ristorante indicato dalla lista
     * dei preferiti dell'utente autenticato.
     * </p>
     *
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
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
    /**
     * Gestisce il comando {@code getFavourites}
     * <p>
     * Restituisce tutti i ristoranti preferiti dell'utente autenticato
     * Il formato di risposta è simile a {@code getRestaurants}
     * </p>
     * @param ctx contesto di sessione del client
     * @throws IOException errori di comunicazione
     * @throws SQLException errori di accesso ai dati
     * @throws InterruptedException gestione concorrenza
     */
    private void handleGetFavourites(ClientContext ctx)
    throws IOException, SQLException, InterruptedException{
        int page;
        try{
            page = Integer.parseInt(ctx.read());
        }catch(NumberFormatException e){
            ctx.write("error");
            return;
        }
        int userId = ctx.getLoggedUserId();
        
        int pages = db.getFavouritesPages(userId);
        String[][] favourites = db.getFavourites(userId, page);

        ctx.write("ok");
        ctx.write(String.valueOf(pages));
        ctx.write(String.valueOf(favourites.length));

        for(String[] fav: favourites){
            ctx.write(fav[0]);
            ctx.write(fav[1]);
        }
    }
}
