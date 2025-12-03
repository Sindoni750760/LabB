package com.theknife.app.Server;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia che definisce i metodi per le query ristorante.
 * Specifica le operazioni CRUD per i ristoranti e le relative recensioni nel database.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public interface QueryRestaurant {

    /**
     * Aggiunge un nuovo ristorante al database.
     *
     * @param ownerId ID del proprietario (ristoratore)
     * @param nome nome del ristorante
     * @param nazione nazione
     * @param citta città
     * @param indirizzo indirizzo
     * @param lat latitudine
     * @param lon longitudine
     * @param fasciaPrezzo fascia di prezzo (1-3)
     * @param delivery true se offre delivery
     * @param online true se offre prenotazione online
     * @param tipoCucina tipo di cucina
     * @return true se l'inserimento ha successo, false altrimenti
     * @throws SQLException se si verifica un errore di database
     */
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

    /**
     * Recupera la lista di ristoranti applicando filtri.
     *
     * @param page numero della pagina
     * @param lat latitudine per filtro geografico
     * @param lon longitudine per filtro geografico
     * @param rangeKm raggio in km per il filtro geografico
     * @param priceMin prezzo minimo
     * @param priceMax prezzo massimo
     * @param hasDelivery "y" per filtrare per delivery
     * @param hasOnline "y" per filtrare per prenotazione online
     * @param starsMin valutazione stellare minima
     * @param starsMax valutazione stellare massima
     * @param category categoria di cucina
     * @param nearMe "y" per filtrare i ristoranti vicini
     * @param userId ID dell'utente (per filtri personalizzati)
     * @param onlyFavourites true per mostrare solo i preferiti
     * @return lista di array contenenti le informazioni dei ristoranti
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException 
     */
    List<String[]> listRestaurants(
            int page,
            String lat, String lon, String rangeKm,
            String priceMin, String priceMax,
            String hasDelivery, String hasOnline,
            String starsMin, String starsMax,
            String category,
            String nearMe,
            Integer userId,
            boolean onlyFavourites
    ) throws SQLException, InterruptedException;

    /**
     * Recupera le informazioni dettagliate di un ristorante.
     *
     * @param id ID del ristorante
     * @return array contenente le informazioni del ristorante, oppure null se non trovato
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    String[] getRestaurantInfo(int id) throws SQLException, InterruptedException;

    /**
     * Modifica le informazioni di un ristorante esistente.
     *
     * @param id ID del ristorante
     * @param nome nuovo nome
     * @param nazione nuova nazione
     * @param citta nuova città
     * @param indirizzo nuovo indirizzo
     * @param lat nuova latitudine
     * @param lon nuova longitudine
     * @param fasciaPrezzo nuova fascia di prezzo
     * @param delivery nuovo stato delivery
     * @param online nuovo stato prenotazione online
     * @param tipoCucina nuovo tipo di cucina
     * @return true se la modifica ha successo, false altrimenti
     * @throws SQLException se si verifica un errore di database
     */
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

    /**
     * Elimina un ristorante dal database.
     *
     * @param id ID del ristorante da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    boolean deleteRestaurant(int id) throws SQLException, InterruptedException;

    // MIO RISTORANTI
    int getMyRestaurantsPageCount(int userId) throws SQLException, InterruptedException;

    List<String[]> getMyRestaurants(int userId, int page) throws SQLException, InterruptedException;


    // recensioni
    int getReviewsPageCount(int restaurantId) throws SQLException, InterruptedException;

    String[][] getReviews(int restaurantId, int page) throws SQLException, InterruptedException;

    boolean addReview(int userId, int restaurantId, int stars, String text) throws SQLException, InterruptedException;

    boolean editReview(int userId, int restaurantId, int stars, String text) throws SQLException, InterruptedException;

    boolean removeReview(int userId, int restaurantId) throws SQLException, InterruptedException;

    String[] getMyReview(int userId, int restaurantId) throws SQLException, InterruptedException;

    int getMyReviewsPageCount(int userId) throws SQLException, InterruptedException;

    List<String[]> getMyReviews(int userId, int page) throws SQLException, InterruptedException;


    // Risposte
    String getResponse(int reviewId) throws SQLException, InterruptedException;

    boolean addResponse(int reviewId, String text) throws SQLException, InterruptedException;

    boolean editResponse(int reviewId, String text) throws SQLException, InterruptedException;

    boolean removeResponse(int reviewId) throws SQLException, InterruptedException;


    // Preferiti
    boolean isFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;

    boolean addFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;

    boolean removeFavourite(int userId, int restaurantId) throws SQLException, InterruptedException;
}
