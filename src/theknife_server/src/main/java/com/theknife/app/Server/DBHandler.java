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

    /* ======================= RESTAURANTS ======================= */

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

    public boolean deleteRestaurant(int restId) {
        try {
            return restaurantCRUD.deleteRestaurant(restId);
        } catch (Exception e) {
            throw new ServerException("Errore deleteRestaurant", e);
        }
    }

    public String[] getRestaurantInfo(int restId) {
        try {
            return restaurantCRUD.getRestaurantInfo(restId);
        } catch (Exception e) {
            throw new ServerException("Errore getRestaurantInfo", e);
        }
    }

    public boolean hasAccess(int userId, int restId) {
        try {
            return restaurantCRUD.hasAccess(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore hasAccess", e);
        }
    }

    public int getUserRestaurantsPages(int userId) {
        try {
            return restaurantCRUD.getUserRestaurantsPages(userId);
        } catch (Exception e) {
            throw new ServerException("Errore getUserRestaurantsPages", e);
        }
    }

    public String[][] getUserRestaurants(int userId, int page) {
        try {
            return restaurantCRUD.getUserRestaurants(userId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getUserRestaurants", e);
        }
    }

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

    /* ======================= REVIEWS ======================= */

    public int getReviewsPageCount(int restId) {
        try {
            return reviewCRUD.getReviewsPageCount(restId);
        } catch (Exception e) {
            throw new ServerException("Errore getReviewsPageCount", e);
        }
    }

    public String[][] getReviews(int restId, int page) {
        try {
            return reviewCRUD.getReviews(restId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getReviews", e);
        }
    }

    public String[] getMyReview(int userId, int restId) {
        try {
            return reviewCRUD.getMyReview(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore getMyReview", e);
        }
    }

    public boolean addReview(int userId, int restId, int stars, String text) {
        try {
            return reviewCRUD.addReview(userId, restId, stars, text);
        } catch (Exception e) {
            throw new ServerException("Errore addReview", e);
        }
    }

    public boolean editReview(int userId, int restId, int stars, String text) {
        try {
            return reviewCRUD.editReview(userId, restId, stars, text);
        } catch (Exception e) {
            throw new ServerException("Errore editReview", e);
        }
    }

    public boolean removeReview(int userId, int restId) {
        try {
            return reviewCRUD.removeReview(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore removeReview", e);
        }
    }

    public int getUserReviewsPages(int userId) {
        try {
            return userCRUD.getUserReviewsPages(userId);
        } catch (Exception e) {
            throw new ServerException("Errore getUserReviewsPages", e);
        }
    }

    public String[][] getUserReviews(int userId, int page) {
        try {
            return userCRUD.getUserReviews(userId, page);
        } catch (Exception e) {
            throw new ServerException("Errore getUserReviews", e);
        }
    }

    /* ======================= RESPONSES ======================= */

    public boolean canRespond(int userId, int reviewId) {
        try {
            return responseCRUD.canRespond(userId, reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore canRespond", e);
        }
    }

    public String getResponse(int reviewId) {
        try {
            return responseCRUD.getResponse(reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore getResponse", e);
        }
    }

    public boolean addResponse(int reviewId, String text) {
        try {
            return responseCRUD.addResponse(reviewId, text);
        } catch (Exception e) {
            throw new ServerException("Errore addResponse", e);
        }
    }

    public boolean editResponse(int reviewId, String text) {
        try {
            return responseCRUD.editResponse(reviewId, text);
        } catch (Exception e) {
            throw new ServerException("Errore editResponse", e);
        }
    }

    public boolean removeResponse(int reviewId) {
        try {
            return responseCRUD.removeResponse(reviewId);
        } catch (Exception e) {
            throw new ServerException("Errore removeResponse", e);
        }
    }

    /* ======================= FAVOURITES ======================= */

    public boolean isFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.isFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore isFavourite", e);
        }
    }

    public boolean addFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.addFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore addFavourite", e);
        }
    }

    public boolean removeFavourite(int userId, int restId) {
        try {
            return favouriteCRUD.removeFavourite(userId, restId);
        } catch (Exception e) {
            throw new ServerException("Errore removeFavourite", e);
        }
    }

    /* ======================= USERS ======================= */

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

    public String[] getUserLoginInfo(String username) {
        try {
            return userCRUD.getUserLoginInfo(username);
        } catch (Exception e) {
            throw new ServerException("Errore getUserLoginInfo", e);
        }
    }

    public String[] getUserInfo(int id) {
        try {
            return userCRUD.getUserInfo(id);
        } catch (Exception e) {
            throw new ServerException("Errore getUserInfo", e);
        }
    }    
}
