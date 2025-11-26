package com.theknife.app.Server;

import java.sql.SQLException;
import java.util.List;

public interface QueryRestaurant {

    boolean addRestaurant(
            int ownerId,
            String nome,
            String nazione,
            String citta,
            String indirizzo,
            double lat,
            double lon,
            int fasciaPrezzo,
            boolean delivery,
            boolean online,
            String tipoCucina
    ) throws SQLException;

    List<String[]> listRestaurants(
            int page,
            String lat, String lon, String rangeKm,
            String priceMin, String priceMax,
            String hasDelivery, String hasOnline,
            String starsMin, String starsMax,
            String category,
            String nearMe,
            Integer userId, // può servire per preferiti
            boolean onlyFavourites
    ) throws SQLException;

    String[] getRestaurantInfo(int id) throws SQLException, InterruptedException;

    boolean editRestaurant(
            int id,
            String nome,
            String nazione,
            String citta,
            String indirizzo,
            double lat,
            double lon,
            int fasciaPrezzo,
            boolean delivery,
            boolean online,
            String tipoCucina
    ) throws SQLException;

    boolean deleteRestaurant(int id) throws SQLException, InterruptedException;

    // MIO RISTORANTI
    int getMyRestaurantsPageCount(int userId) throws SQLException, InterruptedException;

    List<String[]> getMyRestaurants(int userId, int page) throws SQLException, InterruptedException;


    // RECENSIONI
    int getReviewsPageCount(int restaurantId) throws SQLException, InterruptedException;

    List<String[]> getReviews(int restaurantId, int page) throws SQLException, InterruptedException;

    boolean addReview(int userId, int restaurantId, int stars, String text) throws SQLException, InterruptedException;

    boolean editReview(int userId, int restaurantId, int stars, String text) throws SQLException, InterruptedException;

    boolean removeReview(int userId, int restaurantId) throws SQLException, InterruptedException;

    String[] getMyReview(int userId, int restaurantId) throws SQLException, InterruptedException;

    int getMyReviewsPageCount(int userId) throws SQLException, InterruptedException;

    List<String[]> getMyReviews(int userId, int page) throws SQLException, InterruptedException;


    // RISPOSTE
    String getResponse(int reviewId) throws SQLException, InterruptedException;

    boolean addResponse(int reviewId, String text) throws SQLException, InterruptedException;

    boolean editResponse(int reviewId, String text) throws SQLException, InterruptedException;

    boolean removeResponse(int reviewId) throws SQLException, InterruptedException;


    // PREFERITI
    boolean isFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;

    boolean addFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;

    boolean removeFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;
}
