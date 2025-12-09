package com.theknife.app.Server;

import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository principale del server.
 * <p>
 * Espone un'unica istanza (singleton) e implementa le interfacce
 * {@link QueryUser} e {@link QueryRestaurant}.  
 * Delegando al livello superiore (GenericCRUD → UserCRUD → RestaurateurCRUD
 * → RestaurantCRUD), fornisce un'unica entrypoint per tutte le operazioni
 * legate a utenti, ristoranti, recensioni e preferiti.
 * </p>
 */
public class DBHandler extends RestaurantCRUD implements QueryUser, QueryRestaurant {

    /** Istanza singleton. */
    private static DBHandler instance = null;

    /** Costruttore privato (pattern Singleton). */
    private DBHandler() {
        super();
    }

    /**
     * Restituisce l'unica istanza di {@code DBHandler}.
     *
     * @return istanza singleton
     */
    public static synchronized DBHandler getInstance() {
        if (instance == null) {
            instance = new DBHandler();
        }
        return instance;
    }

    /**
     * Aggiunge un nuovo utente nel database.
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username univoco
     * @param passwordHashed password già hashata
     * @param dataNascita data di nascita (può essere null)
     * @param lat latitudine del domicilio
     * @param lon longitudine del domicilio
     * @param isRistoratore true se l’utente è un ristoratore
     * @return true se l'inserimento è riuscito
     */
    @Override
    public boolean addUser(String nome, String cognome, String username,
                           String passwordHashed, Date dataNascita,
                           double lat, double lon, boolean isRistoratore)
            throws SQLException, InterruptedException {

        long birth = (dataNascita == null ? -1L : dataNascita.getTime());
        return super.addUser(nome, cognome, username, passwordHashed,
                             birth, lat, lon, isRistoratore);
    }

    /**
     * Verifica se esiste un utente con lo username specificato.
     *
     * @param username username da controllare
     * @return true se l'utente esiste
     */
    @Override
    public boolean userExists(String username)
            throws SQLException, InterruptedException {

        return super.getUserLoginInfo(username) != null;
    }

       /**
     * Restituisce le informazioni necessarie al login:
     * password hash + id utente.
     *
     * @param username username da cercare
     * @return array contenente le info login, o null se non esiste
     */
    @Override
    public String[] getUserLoginInfo(String username)
            throws SQLException, InterruptedException {

        return super.getUserLoginInfo(username);
    }

    /**
     * Restituisce informazioni complete di un utente dato il suo ID.
     *
     * @param id id dell’utente
     * @return array con nome, cognome e flag ristoratore
     */
    @Override
    public String[] getUserInfoById(int id)
            throws SQLException, InterruptedException {

        return super.getUserInfo(id);
    }


    /**
     * Aggiunge un nuovo ristorante.
     *
     * @return true se l'inserimento è riuscito
     */
    @Override
    public boolean addRestaurant(int ownerId,
                                 String nome,
                                 String nazione,
                                 String citta,
                                 String indirizzo,
                                 double lat,
                                 double lon,
                                 int fasciaPrezzo,
                                 boolean delivery,
                                 boolean online,
                                 String tipoCucina)
            throws SQLException {

        try {
            return super.addRestaurant(
                    ownerId,
                    nome,
                    nazione,
                    citta,
                    indirizzo,
                    lat,
                    lon,
                    fasciaPrezzo,
                    tipoCucina,
                    delivery,
                    online
            );
        } catch (SQLException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return online;
    }
    /**
     * Restituisce una lista di ristoranti filtrati e paginati.
     *
     * @return lista di array [id, nome]
     */
    @Override
    public List<String[]> listRestaurants(
            int page,
            String latStr, String lonStr, String rangeKmStr,
            String priceMinStr, String priceMaxStr,
            String hasDelivery, String hasOnline,
            String starsMinStr, String starsMaxStr,
            String category,
            String nearMe,
            Integer userId,
            boolean onlyFavourites
    ) throws SQLException, InterruptedException {

        Double lat = null, lon = null, rangeKm = null;
        Integer priceMin = null, priceMax = null;
        Double starsMin = null, starsMax = null;

        try {
            if (latStr != null && !latStr.isBlank())
                lat = Double.parseDouble(latStr);
            if (lonStr != null && !lonStr.isBlank())
                lon = Double.parseDouble(lonStr);
            if (rangeKmStr != null && !rangeKmStr.isBlank())
                rangeKm = Double.parseDouble(rangeKmStr);
        } catch (NumberFormatException ignored) {}

        try {
            if (priceMinStr != null && !priceMinStr.isBlank())
                priceMin = Integer.parseInt(priceMinStr);
            if (priceMaxStr != null && !priceMaxStr.isBlank())
                priceMax = Integer.parseInt(priceMaxStr);
        } catch (NumberFormatException ignored) {}

        try {
            if (starsMinStr != null && !starsMinStr.isBlank())
                starsMin = Double.parseDouble(starsMinStr);
            if (starsMaxStr != null && !starsMaxStr.isBlank())
                starsMax = Double.parseDouble(starsMaxStr);
        } catch (NumberFormatException ignored) {}

        boolean delivery = "y".equalsIgnoreCase(hasDelivery);
        boolean online   = "y".equalsIgnoreCase(hasOnline);
        int favouriteUserId = (onlyFavourites && userId != null) ? userId : -1;

        // logica "vicino a me" sta nel livello Handler
        String[][] raw = super.getRestaurantsWithFilter(
                page,
                null, // gestita nel Handler
                null, // city
                lat, lon, rangeKm,
                priceMin, priceMax,
                delivery, online,
                starsMin, starsMax,
                favouriteUserId,
                category
        );

        List<String[]> result = new ArrayList<>();
        if (raw.length <= 1) return result;

        for (int i = 1; i < raw.length; i++) {
            result.add(new String[]{ raw[i][0], raw[i][1] });
        }
        return result;
    }
    /**
     * Restituisce tutte le informazioni dettagliate su un ristorante.
     *
     * @param id id del ristorante
     * @return array di stringhe con tutte le informazioni
     */
    @Override
    public String[] getRestaurantInfo(int id)
            throws SQLException, InterruptedException {

        return super.getRestaurantInfo(id);
    }
    /**
     * Modifica un ristorante esistente.
     *
     * @return true se l'update va a buon fine
     */
    @Override
    public boolean editRestaurant(int id, String nome,
                                  String nazione,
                                  String citta,
                                  String indirizzo,
                                  double lat,
                                  double lon,
                                  int fasciaPrezzo,
                                  boolean delivery,
                                  boolean online,
                                  String tipoCucina)
            throws SQLException {

        try {
            return super.editRestaurant(
                    id,
                    nome,
                    nazione,
                    citta,
                    indirizzo,
                    lat,
                    lon,
                    fasciaPrezzo,
                    tipoCucina,
                    delivery,
                    online
            );
        } catch (SQLException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return online;
    }
    
    /**
     * Elimina un ristorante dato il suo ID.
     *
     * @return true se l'eliminazione è avvenuta
     */
    @Override
    public boolean deleteRestaurant(int id)
            throws SQLException, InterruptedException {

        return super.deleteRestaurant(id);
    }
    
    /**
     * Restituisce il numero totale di pagine di ristoranti dell'utente.
     */
    @Override
    public int getMyRestaurantsPageCount(int userId)
            throws SQLException, InterruptedException {

        return super.getUserRestaurantsPages(userId);
    }
    
    /**
     * Restituisce la pagina di ristoranti dell’utente.
     */
    @Override
    public List<String[]> getMyRestaurants(int userId, int page)
            throws SQLException, InterruptedException {

        String[][] arr = super.getUserRestaurants(userId, page);
        List<String[]> list = new ArrayList<>();
        for (String[] r : arr) list.add(r);
        return list;
    }

    /**
     * Restituisce quante pagine di recensioni possiede un ristorante.
     */
    @Override
    public int getReviewsPageCount(int restaurantId)
            throws SQLException, InterruptedException {

        return super.getReviewsPageCount(restaurantId);
    }
    
    /**
     * Aggiunge una nuova recensione utente.
     */
    @Override
    public boolean addReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException {

        return super.addReview(userId, restaurantId, stars, text);
    }
    
    /**
     * Modifica una recensione utente esistente.
     */
    @Override
    public boolean editReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException {

        return super.editReview(userId, restaurantId, stars, text);
    }
    
    /**
     * Rimuove una recensione dell’utente.
     */
    @Override
    public boolean removeReview(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.removeReview(userId, restaurantId);
    }
    
    /**
     * Restituisce la recensione dell’utente su un ristorante.
     */
    @Override
    public String[] getMyReview(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.getMyReview(userId, restaurantId);
    }

    /**
     * Restituisce quante pagine di recensioni ha l’utente.
     */
    @Override
    public int getMyReviewsPageCount(int userId)
            throws SQLException, InterruptedException {

        return super.getUserReviewsPages(userId);
    }
    
    /**
     * Restituisce una pagina di recensioni dell’utente.
     */
    @Override
    public List<String[]> getMyReviews(int userId, int page)
            throws SQLException, InterruptedException {

        String[][] arr = super.getUserReviews(userId, page);
        List<String[]> list = new ArrayList<>();
        for (String[] r : arr) list.add(r);
        return list;
    }

    /**
     * Restituisce la risposta del ristoratore a una recensione.
     */
    @Override
    public String getResponse(int reviewId)
            throws SQLException, InterruptedException {

        return super.getResponse(reviewId);
    }

    /**
     * Aggiunge una risposta a una recensione.
     */
    @Override
    public boolean addResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

        return super.addResponse(reviewId, text);
    }

    /**
     * Modifica una risposta già esistente.
     */
    @Override
    public boolean editResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

        return super.editResponse(reviewId, text);
    }

    /**
     * Rimuove una risposta del ristoratore.
     */
    @Override
    public boolean removeResponse(int reviewId)
            throws SQLException, InterruptedException {

        return super.removeResponse(reviewId);
    }

    /**
     * Verifica se un ristorante è tra i preferiti dell’utente.
     */
    @Override
    public boolean isFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.isFavourite(userId, restaurantId);
    }

    /**
     * Aggiunge un ristorante ai preferiti dell’utente.
     */
    @Override
    public boolean addFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.addFavourite(userId, restaurantId);
    }

    /**
     * Rimuove un ristorante dai preferiti dell’utente.
     */
    @Override
    public boolean removeFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.removeFavourite(userId, restaurantId);
    }

    /**
     * Restituisce le recensioni di un ristorante per pagina.
     */    
    @Override
    public String[][] getReviews(int restaurantId, int page)
            throws SQLException, InterruptedException {

        // Chiama l’implementazione originale
        String[][] arr = super.getReviews(restaurantId, page);

        // Sicurezza: se dovesse mai essere null, restituiamo un array vuoto
        if (arr == null)
            return new String[0][0];

        return arr;
    }
}
