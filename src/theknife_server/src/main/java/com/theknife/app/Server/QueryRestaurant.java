package com.theknife.app.Server;

public interface QueryRestaurant {

    boolean addRestaurant(int ownerId, String name, String nation, String city,
                          String address, double lat, double lon,
                          int price, String tipoCucina,
                          boolean delivery, boolean online)
            throws Exception;

    boolean editRestaurant(int restId, String name, String nation, String city,
                           String address, double lat, double lon,
                           int price, String tipoCucina,
                           boolean delivery, boolean online)
            throws Exception;

    boolean deleteRestaurant(int restId) throws Exception;

    String[] getRestaurantInfo(int restId) throws Exception;
}
