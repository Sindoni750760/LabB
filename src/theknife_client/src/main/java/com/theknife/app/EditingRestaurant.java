package com.theknife.app;

import java.io.IOException;

/**
 * Classe statica che gestisce lo stato del ristorante attualmente in modifica.
 * Memorizza le informazioni del ristorante selezionato e fornisce metodi
 * per aggiungere, modificare e recuperare i dati associati.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class EditingRestaurant {

    private static int editing_id = -1;
    private static int review_id = -1;

    private static String name, nation, city, address, latitude, longitude;
    private static String avg_price, has_delivery, has_online;
    private static String avg_stars, n_reviews, categories;

    public static void setEditing(int id) throws IOException {
        editing_id = id;

        Communicator.send("getRestaurantInfo");
        Communicator.send(Integer.toString(id));

        name         = Communicator.read();
        nation       = Communicator.read();
        city         = Communicator.read();
        address      = Communicator.read();
        latitude     = Communicator.read();
        longitude    = Communicator.read();
        avg_price    = Communicator.read();
        categories   = Communicator.read();
        has_delivery = Communicator.read();
        has_online   = Communicator.read();
        avg_stars    = Communicator.read();
        n_reviews    = Communicator.read();
    }

    public static void reset() {
        editing_id = -1;
        review_id = -1;
    }

    public static int getId() {
        return editing_id;
    }

    public static String[] getInfo() {
        return new String[]{
            name, nation, city, address, latitude, longitude,
            avg_price, has_delivery, has_online, avg_stars,
            n_reviews, categories
        };
    }

    public static String addRestaurant(
            String name, String nation, String city, String address,
            String latitude, String longitude, String avg_price,
            String categories, boolean has_delivery, boolean has_online
    ) throws IOException {

        Communicator.send("addRestaurant");
        Communicator.send(name);
        Communicator.send(nation);
        Communicator.send(city);
        Communicator.send(address);
        Communicator.send(latitude);
        Communicator.send(longitude);
        Communicator.send(avg_price);
        Communicator.send(categories);
        Communicator.send(has_delivery ? "y" : "n");
        Communicator.send(has_online ? "y" : "n");

        return Communicator.read();
    }

    public static String editRestaurant(
            int id, String name, String nation, String city, String address,
            String latitude, String longitude, String avg_price,
            String categories, boolean has_delivery, boolean has_online
    ) throws IOException {

        Communicator.send("editRestaurant");
        Communicator.send(Integer.toString(id));
        Communicator.send(name);
        Communicator.send(nation);
        Communicator.send(city);
        Communicator.send(address);
        Communicator.send(latitude);
        Communicator.send(longitude);
        Communicator.send(avg_price);
        Communicator.send(categories);
        Communicator.send(has_delivery ? "y" : "n");
        Communicator.send(has_online ? "y" : "n");

        return Communicator.read();
    }

    public static void setReviewId(int id) {
        review_id = id;
    }

    public static int getReviewId() {
        return review_id;
    }
}
