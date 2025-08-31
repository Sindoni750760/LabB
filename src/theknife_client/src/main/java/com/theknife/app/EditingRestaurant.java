package com.theknife.app;

import java.io.IOException;

/**
 * Classe statica che gestisce lo stato del ristorante attualmente in modifica.
 * Memorizza le informazioni del ristorante selezionato e fornisce metodi
 * per aggiungere, modificare e recuperare i dati associati.
 */
public class EditingRestaurant {
        /** ID del ristorante attualmente in modifica. */
    private static int editing_id = -1;

    /** ID della recensione selezionata per risposta o modifica. */
    private static int review_id = -1;

    /** Informazioni del ristorante: nome, località, coordinate, servizi, valutazioni, ecc. */
    private static String name, nation, city, address, latitude, longitude;
    private static String avg_price, has_delivery, has_online;
    private static String avg_stars, n_reviews, categories;


    /**
     * Imposta l'ID del ristorante da modificare e carica le relative informazioni dal server.
     *
     * @param id ID del ristorante da modificare
     * @throws IOException se si verifica un errore nella comunicazione con il server
     */    
    public static void setEditing(int id) throws IOException {
        editing_id = id;

        Communicator.sendStream("getRestaurantInfo");
        Communicator.sendStream(Integer.toString(id));
        name = Communicator.readStream();
        nation = Communicator.readStream();
        city = Communicator.readStream();
        address = Communicator.readStream();
        latitude = Communicator.readStream();
        longitude = Communicator.readStream();
        avg_price = Communicator.readStream();
        categories = Communicator.readStream();
        has_delivery = Communicator.readStream();
        has_online = Communicator.readStream();
        avg_stars = Communicator.readStream();
        n_reviews = Communicator.readStream();
    }

    /**
     * Reimposta lo stato del ristorante e della recensione selezionata.
     * Utile quando si esce dalla modalità di modifica.
     */
    public static void reset() {
        editing_id = -1;
        review_id = -1;
    }

    /**
     * Restituisce l'ID del ristorante attualmente in modifica.
     *
     * @return ID del ristorante
     */
    public static int getId() {
        return editing_id;
    }

    /**
     * Restituisce tutte le informazioni del ristorante attualmente selezionato.
     *
     * @return array di stringhe contenente i dati del ristorante
     */
    public static String[] getInfo() {
        return new String[]{name, nation, city, address, latitude, longitude, avg_price, has_delivery, has_online, avg_stars, n_reviews, categories};
    }

    /**
     * Invia al server i dati per aggiungere un nuovo ristorante.
     *
     * @param name nome del ristorante
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param latitude latitudine
     * @param longitude longitudine
     * @param avg_price prezzo medio
     * @param categories categorie associate
     * @param has_delivery {@code true} se offre consegna a domicilio
     * @param has_online {@code true} se offre prenotazione online
     * @return risposta del server
     * @throws IOException se si verifica un errore nella comunicazione
     */
    public static String addRestaurant(String name, String nation, String city, String address, String latitude, String longitude, String avg_price, String categories, boolean has_delivery, boolean has_online) throws IOException {
        Communicator.sendStream("addRestaurant");
        Communicator.sendStream(name);
        Communicator.sendStream(nation);
        Communicator.sendStream(city);
        Communicator.sendStream(address);
        Communicator.sendStream(latitude);
        Communicator.sendStream(longitude);
        Communicator.sendStream(avg_price);
        Communicator.sendStream(categories);
        Communicator.sendStream(has_delivery ? "y" : "n");
        Communicator.sendStream(has_online ? "y" : "n");

        return Communicator.readStream();
    }

    /**
     * Invia al server i dati aggiornati per modificare un ristorante esistente.
     *
     * @param id ID del ristorante da modificare
     * @param name nome aggiornato
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param latitude latitudine
     * @param longitude longitudine
     * @param avg_price prezzo medio
     * @param categories categorie aggiornate
     * @param has_delivery {@code true} se offre consegna a domicilio
     * @param has_online {@code true} se offre prenotazione online
     * @return risposta del server
     * @throws IOException se si verifica un errore nella comunicazione
     */
    public static String editRestaurant(int id, String name, String nation, String city, String address, String latitude, String longitude, String avg_price, String categories, boolean has_delivery, boolean has_online) throws IOException {
        Communicator.sendStream("editRestaurant");
        Communicator.sendStream(Integer.toString(id));
        Communicator.sendStream(name);
        Communicator.sendStream(nation);
        Communicator.sendStream(city);
        Communicator.sendStream(address);
        Communicator.sendStream(latitude);
        Communicator.sendStream(longitude);
        Communicator.sendStream(avg_price);
        Communicator.sendStream(categories);
        Communicator.sendStream(has_delivery ? "y" : "n");
        Communicator.sendStream(has_online ? "y" : "n");

        return Communicator.readStream();
    }

    /**
     * Imposta l'ID della recensione selezionata per risposta o modifica.
     *
     * @param id ID della recensione
     */
    public static void setReviewId(int id) {
        review_id = id;
    }

    /**
     * Restituisce l'ID della recensione attualmente selezionata.
     *
     * @return ID della recensione
     */
    public static int getReviewId() {
        return review_id;
    }
}