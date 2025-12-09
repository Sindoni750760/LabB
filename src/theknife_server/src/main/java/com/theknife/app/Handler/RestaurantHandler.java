package com.theknife.app.Handler;

import com.theknife.app.Server.DBHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Handler responsabile della gestione di tutti i comandi relativi a ristoranti,
 * recensioni, risposte dei ristoratori e preferiti utente.
 *
 * <p>La classe effettua il dispatch dei comandi testuali ricevuti dal client
 * inoltrandoli ai corrispondenti metodi del livello dati ({@link DBHandler}).</p>
 *
 * <p>Macro-categorie di operazioni gestite:</p>
 *
 * <ul>
 *     <li><b>CRUD ristoranti</b>:
 *         <ul>
 *             <li>{@code addRestaurant}</li>
 *             <li>{@code editRestaurant}</li>
 *             <li>{@code deleteRestaurant}</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Ricerca e visualizzazione</b>:
 *         <ul>
 *             <li>{@code getRestaurants} con filtri multipli</li>
 *             <li>{@code getRestaurantInfo}</li>
 *             <li>{@code getMyRestaurants}, {@code getMyRestaurantsPages}</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Recensioni utente</b>:
 *         <ul>
 *             <li>{@code getReviewsPages}, {@code getReviews}</li>
 *             <li>{@code getMyReview}</li>
 *             <li>{@code addReview}, {@code editReview}, {@code removeReview}</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Risposte del ristoratore</b>:
 *         <ul>
 *             <li>{@code getResponse}</li>
 *             <li>{@code addResponse}, {@code editResponse}, {@code removeResponse}</li>
 *         </ul>
 *     </li>
 *
 *     <li><b>Preferiti</b>:
 *         <ul>
 *             <li>{@code isFavourite}, {@code addFavourite}, {@code removeFavourite}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>Protocollo di comunicazione:</p>
 * <pre>
 * [comando]
 * [parametro1]
 * [parametro2]
 * ...
 * </pre>
 *
 * <p>L’ordine di lettura dei parametri è vincolante e deve rispettare quello
 * inviato dal client.</p>
 *
 * <p>La classe non interagisce direttamente con la rete ma utilizza
 * {@link ClientContext} per consumare i parametri e restituire le risposte.</p>
 *
 * <p>Pattern applicati:</p>
 * <ul>
 *     <li><b>Singleton</b> → accesso tramite {@link #getInstance()}</li>
 *     <li><b>Command Pattern</b> → implementazione di {@link CommandHandler}</li>
 * </ul>
 *
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */

public class RestaurantHandler implements CommandHandler {

    /** Istanza singleton del RestaurantHandler. */
    private static RestaurantHandler instance = null;

    /**
     * Restituisce l'unica istanza dell'handler.
     *
     * @return istanza unica mantenuta in memoria
     */
    public static synchronized RestaurantHandler getInstance() {
        if (instance == null)
            instance = new RestaurantHandler();
        return instance;
    }

    /** Servizio interno che esegue tutte le operazioni SQL. */
    private final DBHandler db = DBHandler.getInstance();

    /**
     * Costruttore privato per impedire istanziazione esterna.
     */
    private RestaurantHandler() {}

    /**
     * Gestisce tutte le richieste correlate ai ristoranti, alle recensioni
     * e ai preferiti, inoltrando ai rispettivi metodi.
     *
     * <p>Il protocollo avviene sempre nella forma:</p>
     *
     * <pre>
     * [comando]
     * [parametro1]
     * [parametro2]
     * ...
     * </pre>
     *
     * <p>L’ordine di lettura dei parametri è strettamente definito
     * dal client, quindi il server deve consumarli nell’esatto ordine corretto.</p>
     *
     * <p>Se il comando non è di pertinenza dell’handler,
     * ritorna {@code false} per delegare altri handler.</p>
     *
     * @param cmd comando testuale ricevuto dal client
     * @param ctx oggetto che incapsula socket, input e output
     * @return {@code true} se il comando è stato gestito, {@code false} altrimenti
     *
     * @throws IOException se avvengono errori nella comunicazione col client
     * @throws SQLException propagata dal DB in caso di fallimenti SQL
     * @throws InterruptedException se un thread è stato interrotto durante DB o rete
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

            case "getReviewsPages"        -> handleGetReviewsPages(ctx);
            case "getReviewsPageCount"    -> handleGetReviewsPages(ctx);

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

            case "getMyReviewsPages"      -> handleGetMyReviewsPages(ctx);
            case "getMyReviews"           -> handleGetMyReviews(ctx);

            default -> { return false; }
        }

        return true;
    }

    /**
     * Gestisce il comando {@code addRestaurant}.
     *
     * <p>Legge dal client i seguenti parametri in ordine:</p>
     * <ol>
     *     <li>nome</li>
     *     <li>nazione</li>
     *     <li>città</li>
     *     <li>indirizzo</li>
     *     <li>latitudine</li>
     *     <li>longitudine</li>
     *     <li>fascia di prezzo</li>
     *     <li>categorie</li>
     *     <li>flag delivery (y/n)</li>
     *     <li>flag prenotazione online (y/n)</li>
     * </ol>
     *
     * <p>Effettua controlli di validità sui campi numerici e obbligatori;
     * in caso di errori restituisce codici specifici quali:</p>
     *
     * <ul>
     *     <li>{@code missing} → campi obbligatori mancanti</li>
     *     <li>{@code coordinates} → coordinate non numeriche</li>
     *     <li>{@code price_format} → prezzo non numerico</li>
     *     <li>{@code price_negative} → prezzo < 0</li>
     * </ul>
     *
     * <p>In caso di successo restituisce {@code ok}.</p>
     *
     * @param ctx contesto della sessione associata al client
     */

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

    /**
     * Gestisce il comando {@code editRestaurant}.
     *
     * <p>Il client fornisce ID ristorante e nuovi valori dei campi.
     * Prima della modifica viene verificato che l'utente corrente
     * sia proprietario del ristorante tramite {@code db.hasAccess()}.</p>
     *
     * <p>Può restituire:</p>
     * <ul>
     *     <li>{@code denied} → se l'utente non è proprietario</li>
     *     <li>{@code missing} → se alcuni campi obbligatori sono mancanti</li>
     *     <li>{@code coordinates} → lat/lon non valide</li>
     *     <li>{@code price_format} → prezzo non numerico</li>
     *     <li>{@code price_negative} → prezzo < 0</li>
     *     <li>{@code ok} → aggiornamento riuscito</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */

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

    /**
     * Gestisce il comando {@code deleteRestaurant}.
     *
     * <p>Legge ID ristorante e verifica che l'utente loggato
     * ne sia proprietario; in caso contrario restituisce {@code denied}.</p>
     *
     * <p>Altrimenti invoca {@link DBHandler#deleteRestaurant(int)}
     * e restituisce:</p>
     *
     * <ul>
     *     <li>{@code ok} eliminazione riuscita</li>
     *     <li>{@code error} problema lato DB</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */

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

    /**
     * Gestisce il comando {@code getRestaurants}.
     *
     * <p>Metodo complesso che effettua una ricerca filtrata sui ristoranti.
     * Legge numerosi parametri (modalità ricerca, intervallo prezzo, stelle,
     * filtraggio preferiti, range geografico, categorie, ecc.).
     * </p>
     *
     * <p>Possibili risposte errore:</p>
     * <ul>
     *     <li>{@code coordinates} → range o coordinate non numeric</li>
     *     <li>{@code price} → range di prezzo non valido</li>
     *     <li>{@code stars} → range stelle non valido</li>
     *     <li>{@code invalid} → input incoerente</li>
     *     <li>{@code location} → ricerca per luogo errata</li>
     * </ul>
     *
     * <p>In caso di successo restituisce:</p>
     *
     * <pre>
     * ok
     * [numero_pagine]
     * [numero_risultati_pagina]
     * per ogni ristorante:
     *     id
     *     nome
     * </pre>
     *
     * @param ctx client context di comunicazione
     */

    private void handleGetRestaurants(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int page = Integer.parseInt(ctx.read());

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
        String onlyFavStr  = ctx.read();  // <-- AGGIUNTO (ora letto correttamente!)

        boolean onlyFav = "y".equalsIgnoreCase(onlyFavStr);
        int favUserId   = onlyFav ? ctx.getLoggedUserId() : -1;

        // ---- VARIABILI PER IL DB ----
        String nation = null;
        String city   = null;
        Double lat    = null;
        Double lon    = null;
        Double rangeKm = null;

        switch (mode) {

            case "all" -> { }

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
                favUserId,          // <-- ORA FUNZIONA
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

    /**
     * Gestisce il comando {@code getRestaurantInfo}.
     *
     * <p>Legge l'ID ristorante e restituisce sempre 12 valori in ordine:
     * nome, nazione, città, indirizzo, lat, lon,
     * prezzo medio, categorie, delivery, online,
     * media stelle, numero recensioni.</p>
     *
     * <p>Se il ristorante non esiste, restituisce 12 stringhe vuote.</p>
     *
     * @param ctx gestione sessione client
     */

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

    /**
     * Restituisce il numero di pagine di ristoranti
     * inseriti dall'utente loggato.
     *
     * <p>Risposta: un intero in formato stringa.</p>
     *
     * @param ctx sessione client
     */

    private void handleGetMyRestaurantsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int pages = db.getUserRestaurantsPages(ctx.getLoggedUserId());
        ctx.write(Integer.toString(pages));
    }

    /**
     * Restituisce un elenco paginato dei ristoranti inseriti
     * dal ristoratore attualmente loggato.
     *
     * <p>Formato risposta:</p>
     * <pre>
     * [numero]
     * (ripetuto numero volte):
     *     id
     *     nome
     * </pre>
     *
     * @param ctx contesto comunicazione
     */

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

    /**
     * Restituisce il numero di pagine disponibili di recensioni
     * per un certo ristorante.
     *
     * <p>Risposta: intero positivo (anche 0)</p>
     *
     * @param ctx sessione client
     */

    private void handleGetReviewsPages(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int pages = db.getReviewsPageCount(restId);
        ctx.write(Integer.toString(pages));
    }

    /**
     * Restituisce le recensioni associate ad un ristorante
     * in forma paginata.
     *
     * <p>Formato risposta:</p>
     *
     * <pre>
     * [size]
     * per ogni recensione:
     *     id_recensione
     *     stelle
     *     testo
     *     n/y  (se esiste risposta del ristoratore)
     *     [testo risposta] se presente
     * </pre>
     *
     * @param ctx sessione client
     */

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

    /**
     * Restituisce la recensione dell'utente loggato
     * per un ristorante specifico.
     *
     * <p>Il formato è sempre:</p>
     *
     * <pre>
     * [stelle] (0 se mai recensito)
     * [testo]
     * </pre>
     *
     * @param ctx contesto sessione client
     */

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

    /**
     * Inserisce una nuova recensione lasciata dall’utente loggato
     * su un ristorante selezionato.
     *
     * <p>Restituisce:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code error} se fallisce DB</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */

    private void handleAddReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.addReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Modifica una recensione esistente dell’utente sul ristorante corrente.
     *
     * <p>Restituisce:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code error} errore DB</li>
     * </ul>
     *
     * @param ctx contesto comunicazione client
     */
    private void handleEditReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        int stars  = Integer.parseInt(ctx.read());
        String text = ctx.read();

        boolean ok = db.editReview(ctx.getLoggedUserId(), restId, stars, text);
        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Elimina una recensione dell'utente.
     *
     * <p>Risposta:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code error}</li>
     * </ul>
     *
     * @param ctx sessione client
     */

    private void handleRemoveReview(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.removeReview(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Restituisce la risposta del ristoratore ad una recensione.
     *
     * <p>Formato risposta:</p>
     * <ul>
     *     <li>{@code ok} + testo risposta</li>
     *     <li>{@code none} se non esiste</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */
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

    /**
     * Aggiunge una risposta ufficiale del ristoratore
     * ad una recensione.
     *
     * <p>Controlla tramite DB se l'utente loggato possiede quel ristorante;
     * se non è autorizzato restituisce {@code denied}.</p>
     *
     * @param ctx sessione client corrente
     */
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

    /**
     * Modifica la risposta precedentemente inserita
     * dal ristoratore alla recensione.
     *
     * <p>Risposte possibili:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code denied}</li>
     *     <li>{@code error}</li>
     * </ul>
     *
     * @param ctx sessione active client
     */
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

    /**
     * Rimuove la risposta del ristoratore ad una recensione.
     *
     * <p>Verifica la proprietà sulla recensione
     * tramite DB; altrimenti ritorna {@code denied}.</p>
     *
     * @param ctx sessione client
     */
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

    /**
     * Verifica se il ristorante indicato
     * è presente tra i preferiti dell'utente loggato.
     *
     * <p>Risposta:</p>
     * <ul>
     *     <li>{@code y}</li>
     *     <li>{@code n}</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */
    private void handleIsFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean fav = db.isFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(fav ? "y" : "n");
    }

    /**
     * Aggiunge un ristorante ai preferiti dell’utente loggato.
     *
     * <p>Risposta:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code error}</li>
     * </ul>
     *
     * @param ctx sessione client
     */

    private void handleAddFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.addFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Rimuove un ristorante dai preferiti dell’utente loggato.
     *
     * <p>Risposta:</p>
     * <ul>
     *     <li>{@code ok}</li>
     *     <li>{@code error}</li>
     * </ul>
     *
     * @param ctx contesto sessione client
     */

    private void handleRemoveFavourite(ClientContext ctx)
            throws IOException, SQLException, InterruptedException {

        int restId = Integer.parseInt(ctx.read());
        boolean ok = db.removeFavourite(ctx.getLoggedUserId(), restId);

        ctx.write(ok ? "ok" : "error");
    }

    /**
     * Restituisce il numero di pagine di recensioni create dall’utente.
     *
     * <p>Formato risposta:</p>
     * <pre>
     * ok
     * [n_pagine]
     * </pre>
     *
     * @param ctx sessione client
     */
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

    /**
     * Restituisce l'elenco paginato delle recensioni dell’utente loggato.
     *
     * <p>Formato risposta:</p>
     *
     * <pre>
     * [size]
     * per ogni recensione:
     *     nome_ristorante
     *     stelle
     *     testo
     * </pre>
     *
     * @param ctx sessione client
     */
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
    /**
     * Verifica se una stringa è nulla o contiene solo spazi bianchi.
     *
     * <p>È un piccolo helper interno usato per validare i campi obbligatori
     * ricevuti dal client prima di procedere con le operazioni sul database.</p>
     *
     * @param s stringa da controllare
     * @return {@code true} se {@code s} è {@code null} oppure vuota dopo trim,
     *         {@code false} altrimenti
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
