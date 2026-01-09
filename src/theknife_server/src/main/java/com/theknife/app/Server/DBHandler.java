package com.theknife.app.Server;

/**
 * Facade centrale di accesso al database.
 *
 * <p>
 * Questa classe rappresenta l'unico punto di accesso ai dati
 * per il livello {@code Handler}. Implementa il pattern
 * <b>Facade</b>, delegando le operazioni ai CRUD specializzati
 * e fornendo un'API uniforme verso l'esterno.
 * </p>
 *
 * <p>
 * Responsabilità principali:
 * </p>
 * <ul>
 *     <li>Delegare le operazioni ai CRUD corretti</li>
 *     <li>Centralizzare la gestione delle eccezioni SQL</li>
 *     <li>Wrappare ogni errore in {@link ServerException}</li>
 *     <li>Garantire coerenza e isolamento del layer DB</li>
 * </ul>
 *
 * <p>
 * Nessun {@code SQLException} o {@code InterruptedException}
 * viene propagato oltre questo livello.
 * </p>
 *
 * <p>
 * Pattern applicati:
 * </p>
 * <ul>
 *     <li><b>Singleton</b></li>
 *     <li><b>Facade</b></li>
 *     <li><b>Separation of Concerns</b></li>
 * </ul>
 */
public final class DBHandler {

    /** Istanza singleton del DBHandler. */
    private static DBHandler instance;

    private final RestaurantCRUD restaurantCRUD;
    private final ReviewCRUD reviewCRUD;
    private final ResponseCRUD responseCRUD;
    private final FavouriteCRUD favouriteCRUD;
    private final UserCRUD userCRUD;

    /**
     * Costruttore privato.
     * Inizializza i CRUD concreti.
     */
    private DBHandler() {
        this.restaurantCRUD = new RestaurantCRUD();
        this.reviewCRUD     = new ReviewCRUD();
        this.responseCRUD   = new ResponseCRUD();
        this.favouriteCRUD  = new FavouriteCRUD();
        this.userCRUD       = new UserCRUD();
    }

    /**
     * Restituisce l'unica istanza del DBHandler.
     *
     * @return istanza singleton
     */
    public static synchronized DBHandler getInstance() {
        if (instance == null)
            instance = new DBHandler();
        return instance;
    }

    /**
    * Inserisce un nuovo ristorante nel sistema
    * <p>
    * delega l'operazione al metodo {@link RestaurantCRUD#addRestaurant}
    * </p>
    * 
    * @param ownerId id del ristoratore proprietario
    * @param name nome del ristorante
    * @param nation nazione
    * @param city città
    * @param address indirizzo
    * @param lat latitudine
    * @param lon longitudine
    * @param price fascia di prezzo
    * @param tipoCucina tipologia di cucina
    * @param delivery disponibilità servizio delivery
    * @param online disponibilità prenotazione online
    * @return {@code true} se l'inserimento ha successo, {@code false} altrimenti
    * @throws ServerException in caso di errore di accesso al database
    */
    public boolean addRestaurant(int ownerId, String name, String nation, String city,
                                 String address, double lat, double lon,
                                 int price, String tipoCucina,
                                 boolean delivery, boolean online) {

        try {
            return restaurantCRUD.addRestaurant(
                    ownerId, name, nation, city, address,
                    lat, lon, price, tipoCucina, delivery, online
            );
        } catch (Exception e) {
            throw new ServerException("Errore addRestaurant", e);
        }
    }

    /**
     * Modifica i dati di un ristorante esistente.
     * <p>
     * delega l'operazione al metodo {@link RestaurantCRUD#editRestaurant}
     * </p>
     * @param restId id del ristorante
     * @param name nome del ristorante
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param lat latitudine
     * @param lon longitudine
     * @param price fascia di prezzo
     * @param tipoCucina tipologia di cucina
     * @param delivery disponibilità servizio delivery
     * @param online disponibilità prenotazione online
     * @return {@code true} se la modifica ha successo
     * @throws ServerException in caso di errore di accesso al database
     */
    public boolean editRestaurant(int restId, String name, String nation, String city,
                                  String address, double lat, double lon,
                                  int price, String tipoCucina,
                                  boolean delivery, boolean online) {

        try {
            return restaurantCRUD.editRestaurant(
                    restId, name, nation, city, address,
                    lat, lon, price, tipoCucina, delivery, online
            );
        } catch (Exception e) {
            throw new ServerException("Errore editRestaurant", e);
        }
    }

    /**
     * Elimina un ristorante dal sistema.
     * <p>
     * delega l'operazione al metodo {@link RestaurantCRUD#deleteRestaurant}
     * </p>
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean deleteRestaurant(int restId) {
        try {
            return restaurantCRUD.deleteRestaurant(restId);
        } catch (Exception e) {
            throw new ServerException("Errore deleteRestaurant", e);
        }
    }

    /**
     * Recupera le informazioni di un ristorante.
     * <p>
     * delega l'operazione al metodo {@link RestaurantCRUD#getRestaurantInfo}
     * </p>
     * @param restId id del ristorante
     * @return array di stringhe contenente i dati del ristorante
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[] getRestaurantInfo(int restId) {
        try {
            return restaurantCRUD.getRestaurantInfo(restId);
        } catch (Exception e) {
            throw new ServerException("Errore getRestaurantInfo", e);
        }
    }

    /**
     * Verifica se un utente ha accesso alla gestione di un ristorante.
     * <p>
     * delega l'operazione al metodo {@link RestaurantCRUD#hasAccess}
     * </p>
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se l'utente ha accesso
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean hasAccess(int userId, int restId) {
        try {
            return restaurantCRUD.hasAccess(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore hasAccess", e);
        }
    }

    /**
     * Restituisce il numero di pagine di ristoranti posseduti dall'utente.
     * <p>
     * delega l'operazione al metodo {@link RestaurantCRUD#getUserRestaurantsPages}
     * </p>
     * @param userId id dell'utente
     * @return numero di pagine disponibili
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public int getUserRestaurantsPages(int userId) {
        try {
            return restaurantCRUD.getUserRestaurantsPages(userId);
        } catch (Exception e) {
            throw new ServerException("Errore getUserRestaurantsPages", e);
        }
    }

    /**
     * Recupera l'elenco dei ristoranti posseduti dall'utente per una specifica pagina.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link RestaurantCRUD#getUserRestaurants(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param page numero di pagina richiesta
     * @return matrice contenente i ristoranti dell'utente
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[][] getUserRestaurants(int userId, int page) {
        try {
            return restaurantCRUD.getUserRestaurants(userId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getUserRestaurants", e);
        }
    }

    /**
     * Recupera l'elenco dei ristoranti applicando filtri di ricerca avanzata.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link RestaurantCRUD#getRestaurantsWithFilter(int, String, String, Double, Double, Double, Integer, Integer, boolean, boolean, Double, Double, int, String)}.
     * </p>
     *
     * @param page pagina richiesta
     * @param nation nazione
     * @param city città
     * @param lat latitudine
     * @param lon longitudine
     * @param rangeKm raggio di ricerca in chilometri
     * @param priceMin prezzo minimo
     * @param priceMax prezzo massimo
     * @param delivery filtro servizio delivery
     * @param online filtro prenotazione online
     * @param starsMin valutazione minima
     * @param starsMax valutazione massima
     * @param favouriteUserId id utente per filtro preferiti
     * @param category categoria del ristorante
     * @return matrice contenente i ristoranti filtrati
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[][] getRestaurantsWithFilter(
            int page,
            String nation,
            String city,
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String category) {

        try {
            return restaurantCRUD.getRestaurantsWithFilter(
                    page, nation, city,
                    lat, lon, rangeKm,
                    priceMin, priceMax,
                    delivery, online,
                    starsMin, starsMax,
                    favouriteUserId, category
            );
        } catch (Exception e) {
            throw new ServerException("Errore getRestaurantsWithFilter", e);
        }
    }

    /**
     * Restituisce il numero di pagine di recensioni associate a un ristorante.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#getReviewsPageCount(int)}.
     * </p>
     *
     * @param restId id del ristorante
     * @return numero di pagine disponibili
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public int getReviewsPageCount(int restId) {
        try {
            return reviewCRUD.getReviewsPageCount(restId);
        } catch (Exception e) {
            throw new ServerException("Errore getReviewsPageCount", e);
        }
    }

    /**
     * Recupera le recensioni di un ristorante per una specifica pagina.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#getReviews(int, int)}.
     * </p>
     *
     * @param restId id del ristorante
     * @param page numero di pagina richiesta
     * @return matrice contenente le recensioni
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[][] getReviews(int restId, int page) {
        try {
            return reviewCRUD.getReviews(restId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getReviews", e);
        }
    }

    /**
     * Recupera la recensione scritta dall'utente per un determinato ristorante.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#getMyReview(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return array contenente la recensione dell'utente
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[] getMyReview(int userId, int restId) {
        try {
            return reviewCRUD.getMyReview(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore getMyReview", e);
        }
    }

    /**
     * Inserisce una nuova recensione nel sistema.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#addReview(int, int, int, String)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @param stars valutazione assegnata
     * @param text testo della recensione
     * @return {@code true} se l'inserimento ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean addReview(int userId, int restId, int stars, String text) {
        try {
            return reviewCRUD.addReview(userId, restId, stars, text);
        } catch (Exception e) {
            throw new ServerException("Errore addReview", e);
        }
    }

    /**
     * Modifica una recensione esistente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#editReview(int, int, int, String)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @param stars valutazione aggiornata
     * @param text testo aggiornato della recensione
     * @return {@code true} se la modifica ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean editReview(int userId, int restId, int stars, String text) {
        try {
            return reviewCRUD.editReview(userId, restId, stars, text);
        } catch (Exception e) {
            throw new ServerException("Errore editReview", e);
        }
    }

    /**
     * Rimuove una recensione dal sistema.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ReviewCRUD#removeReview(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean removeReview(int userId, int restId) {
        try {
            return reviewCRUD.removeReview(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore removeReview", e);
        }
    }

    /**
     * Restituisce il numero di pagine di recensioni scritte dall'utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link UserCRUD#getUserReviewsPages(int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @return numero di pagine disponibili
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public int getUserReviewsPages(int userId) {
        try {
            return userCRUD.getUserReviewsPages(userId);
        } catch (Exception e) {
            throw new ServerException("Errore getUserReviewsPages", e);
        }
    }

    /**
     * Recupera le recensioni scritte dall'utente per una specifica pagina.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link UserCRUD#getUserReviews(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param page numero di pagina richiesta
     * @return matrice contenente le recensioni dell'utente
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[][] getUserReviews(int userId, int page) {
        try {
            return userCRUD.getUserReviews(userId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getUserReviews", e);
        }
    }

    /**
     * Verifica se l'utente ha il permesso di rispondere a una recensione.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ResponseCRUD#canRespond(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param reviewId id della recensione
     * @return {@code true} se l'utente può rispondere
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean canRespond(int userId, int reviewId) {
        try {
            return responseCRUD.canRespond(userId, reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore canRespond", e);
        }
    }

    /**
     * Recupera la risposta associata a una recensione.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ResponseCRUD#getResponse(int)}.
     * </p>
     *
     * @param reviewId id della recensione
     * @return testo della risposta
     * @throws ServerException in caso di errore di accesso ai dati
     */

    public String getResponse(int reviewId) {
        try {
            return responseCRUD.getResponse(reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore getResponse", e);
        }
    }

    /**
     * Inserisce una risposta a una recensione.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ResponseCRUD#addResponse(int, String)}.
     * </p>
     *
     * @param reviewId id della recensione
     * @param text testo della risposta
     * @return {@code true} se l'inserimento ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean addResponse(int reviewId, String text) {
        try {
            return responseCRUD.addResponse(reviewId, text);
        } catch (Exception e) {
            throw new ServerException("Errore addResponse", e);
        }
    }

    /**
     * Modifica una risposta esistente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ResponseCRUD#editResponse(int, String)}.
     * </p>
     *
     * @param reviewId id della recensione
     * @param text testo aggiornato della risposta
     * @return {@code true} se la modifica ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean editResponse(int reviewId, String text) {
        try {
            return responseCRUD.editResponse(reviewId, text);
        } catch (Exception e) {
            throw new ServerException("Errore editResponse", e);
        }
    }

    /**
     * Rimuove una risposta associata a una recensione.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link ResponseCRUD#removeResponse(int)}.
     * </p>
     *
     * @param reviewId id della recensione
     * @return {@code true} se la rimozione ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean removeResponse(int reviewId) {
        try {
            return responseCRUD.removeResponse(reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore removeResponse", e);
        }
    }

    /**
     * Verifica se un ristorante è presente tra i preferiti dell'utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link FavouriteCRUD#isFavourite(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se il ristorante è tra i preferiti
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean isFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.isFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore isFavourite", e);
        }
    }

    /**
     * Aggiunge un ristorante alla lista dei preferiti dell'utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link FavouriteCRUD#addFavourite(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se l'inserimento ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean addFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.addFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore addFavourite", e);
        }
    }

    /**
     * Rimuove un ristorante dalla lista dei preferiti dell'utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link FavouriteCRUD#removeFavourite(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean removeFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.removeFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore removeFavourite", e);
        }
    }

    /**
     * Inserisce un nuovo utente nel sistema.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link UserCRUD#addUser(String, String, String, String, long, double, double, boolean)}.
     * </p>
     *
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username scelto
     * @param hashPassword password cifrata
     * @param birth data di nascita (epoch)
     * @param lat latitudine
     * @param lon longitudine
     * @param isRistoratore ruolo dell'utente
     * @return {@code true} se l'inserimento ha successo
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public boolean addUser(String nome, String cognome, String username,
                           String hashPassword, long birth,
                           double lat, double lon, boolean isRistoratore) {

        try {
            return userCRUD.addUser(
                    nome, cognome, username,
                    hashPassword, birth,
                    lat, lon, isRistoratore
            );
        } catch (Exception e) {
            throw new ServerException("Errore addUser", e);
        }
    }

    /**
     * Recupera le informazioni di login di un utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link UserCRUD#getUserLoginInfo(String)}.
     * </p>
     *
     * @param username username dell'utente
     * @return array contenente le informazioni di login
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[] getUserLoginInfo(String username) {
        try {
            return userCRUD.getUserLoginInfo(username);
        } catch (Exception e) {
            throw new ServerException("Errore getUserLoginInfo", e);
        }
    }

    /**
     * Recupera le informazioni complete di un utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link UserCRUD#getUserInfo(int)}.
     * </p>
     *
     * @param id id dell'utente
     * @return array contenente i dati dell'utente
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[] getUserInfo(int id) {
        try {
            return userCRUD.getUserInfo(id);
        } catch (Exception e) {
            throw new ServerException("Errore getUserInfo", e);
        }
    }
     /**
     * Restituisce il numero di pagine dei ristoranti preferiti dell'utente.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link FavouriteCRUD#getFavouritesPages(int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @return numero di pagine disponibili
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public int getFavouritesPages(int userId){
        try{
            return favouriteCRUD.getFavouritesPages(userId);
        }catch(Exception e){
            throw new ServerException("Errore getFavouritesPages", e);
        }
    }
     /**
     * Recupera i ristoranti preferiti dell'utente per una specifica pagina.
     *
     * <p>
     * Delega l'operazione al metodo
     * {@link FavouriteCRUD#getFavourites(int, int)}.
     * </p>
     *
     * @param userId id dell'utente
     * @param page numero di pagina richiesta
     * @return matrice contenente i ristoranti preferiti
     * @throws ServerException in caso di errore di accesso ai dati
     */
    public String[][] getFavourites(int userId, int page) {
        try {
            return favouriteCRUD.getFavourites(userId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getFavourites", e);
        }
    }
}
