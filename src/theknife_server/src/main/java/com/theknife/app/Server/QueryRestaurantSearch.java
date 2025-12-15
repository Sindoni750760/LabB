package com.theknife.app.Server;

public interface QueryRestaurantSearch {

    String[][] getRestaurantsWithFilter(
            int page,
            String nation,
            String city,
            Double lat, Double lon, Double rangeKm,
            Integer priceMin, Integer priceMax,
            boolean delivery, boolean online,
            Double starsMin, Double starsMax,
            int favouriteUserId,
            String category
    ) throws Exception;
}
