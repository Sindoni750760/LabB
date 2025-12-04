package com.theknife.app.Server;

import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository principale del server.
 * Espone un'unica istanza (singleton) e implementa QueryUser + QueryRestaurant.
 *
 * Tutta la logica SQL è implementata nei layer:
 * GenericCRUD -> UserCRUD -> RestaurateurCRUD -> RestaurantCRUD
 */
public class DBHandler extends RestaurantCRUD implements QueryUser, QueryRestaurant {

    /** Istanza singleton. */
    private static DBHandler instance = null;

    private DBHandler() {
        super();
    }

    public static synchronized DBHandler getInstance() {
        if (instance == null) {
            instance = new DBHandler();
        }
        return instance;
    }

    //Metodi QueryUser:
    @Override
    public boolean addUser(String nome, String cognome, String username,
                           String passwordHashed, Date dataNascita,
                           double lat, double lon, boolean isRistoratore)
            throws SQLException, InterruptedException {

        long birth = (dataNascita == null ? -1L : dataNascita.getTime());
        return super.addUser(nome, cognome, username, passwordHashed,
                             birth, lat, lon, isRistoratore);
    }

    @Override
    public boolean userExists(String username)
            throws SQLException, InterruptedException {

        return super.getUserLoginInfo(username) != null;
    }

    @Override
    public String[] getUserLoginInfo(String username)
            throws SQLException, InterruptedException {

        return super.getUserLoginInfo(username);
    }

    @Override
    public String[] getUserInfoById(int id)
            throws SQLException, InterruptedException {

        return super.getUserInfo(id);
    }


    /* 
    * Metodi QueryRestaurants
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

        // NB: ordine parametri originale: name, nation, city, address, lat, lon, price, tipo, delivery, online
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

    @Override
    public String[] getRestaurantInfo(int id)
            throws SQLException, InterruptedException {

        return super.getRestaurantInfo(id);
    }

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

    @Override
    public boolean deleteRestaurant(int id)
            throws SQLException, InterruptedException {

        return super.deleteRestaurant(id);
    }

    @Override
    public int getMyRestaurantsPageCount(int userId)
            throws SQLException, InterruptedException {

        return super.getUserRestaurantsPages(userId);
    }

    @Override
    public List<String[]> getMyRestaurants(int userId, int page)
            throws SQLException, InterruptedException {

        String[][] arr = super.getUserRestaurants(userId, page);
        List<String[]> list = new ArrayList<>();
        for (String[] r : arr) list.add(r);
        return list;
    }

    @Override
    public int getReviewsPageCount(int restaurantId)
            throws SQLException, InterruptedException {

        return super.getReviewsPageCount(restaurantId);
    }

    @Override
    public boolean addReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException {

        return super.addReview(userId, restaurantId, stars, text);
    }

    @Override
    public boolean editReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException {

        return super.editReview(userId, restaurantId, stars, text);
    }

    @Override
    public boolean removeReview(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.removeReview(userId, restaurantId);
    }

    @Override
    public String[] getMyReview(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.getMyReview(userId, restaurantId);
    }

    @Override
    public int getMyReviewsPageCount(int userId)
            throws SQLException, InterruptedException {

        return super.getUserReviewsPages(userId);
    }

    @Override
    public List<String[]> getMyReviews(int userId, int page)
            throws SQLException, InterruptedException {

        String[][] arr = super.getUserReviews(userId, page);
        List<String[]> list = new ArrayList<>();
        for (String[] r : arr) list.add(r);
        return list;
    }

    @Override
    public String getResponse(int reviewId)
            throws SQLException, InterruptedException {

        return super.getResponse(reviewId);
    }

    @Override
    public boolean addResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

        return super.addResponse(reviewId, text);
    }

    @Override
    public boolean editResponse(int reviewId, String text)
            throws SQLException, InterruptedException {

        return super.editResponse(reviewId, text);
    }

    @Override
    public boolean removeResponse(int reviewId)
            throws SQLException, InterruptedException {

        return super.removeResponse(reviewId);
    }

    @Override
    public boolean isFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.isFavourite(userId, restaurantId);
    }

    @Override
    public boolean addFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.addFavourite(userId, restaurantId);
    }

    @Override
    public boolean removeFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException {

        return super.removeFavourite(userId, restaurantId);
    }
    
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
