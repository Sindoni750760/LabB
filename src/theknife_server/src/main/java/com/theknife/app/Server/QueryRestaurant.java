package com.theknife.app.Server;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia che definisce tutte le operazioni di interrogazione,
 * modifica e gestione dei ristoranti e dei relativi contenuti associati
 * (recensioni, risposte e preferiti).
 * <p>
 * I metodi previsti includono:
 * <ul>
 *     <li>CRUD sui ristoranti</li>
 *     <li>Gestione recensioni e risposte</li>
 *     <li>Gestione preferiti degli utenti</li>
 *     <li>Recupero di elementi paginati</li>
 * </ul>
 *
 * Le eccezioni {@link SQLException} e {@link InterruptedException}
 * rappresentano rispettivamente:
 * <ul>
 *     <li>errori SQL o di accesso al DB</li>
 *     <li>interruzione del thread in operazioni bloccanti</li>
 * </ul>
 * 
 * Implementato dalla classe {@link DBHandler}.
 *
 * @author Mattia Sindoni
 * @author Erica Faccio
 * @author Giovanni Isgrò
 */

public interface QueryRestaurant {

    /**
     * Inserisce un nuovo ristorante nel database associandolo al proprietario.
     *
     * @param ownerId ID del proprietario registrato
     * @param nome nome del ristorante
     * @param nazione nazione
     * @param citta città
     * @param indirizzo indirizzo specifico
     * @param lat latitudine geografica
     * @param lon longitudine geografica
     * @param fasciaPrezzo fascia di prezzo espressa come intero (1-3)
     * @param delivery {@code true} se offre consegna a domicilio
     * @param online {@code true} se supporta prenotazione online
     * @param tipoCucina etichetta della categoria culinaria
     * @return {@code true} se l'inserimento va a buon fine, {@code false} altrimenti
     * @throws SQLException se si verificano errori SQL o transazionali
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
     * Elenca i ristoranti secondo criteri e filtri dinamici, con supporto alla paginazione.
     * <p>
     * I parametri stringa vengono interpretati dinamicamente dal livello sottostante.
     *
     * @param page indice della pagina (≥0)
     * @param lat latitudine per filtro geografico, null o vuota per ignorare
     * @param lon longitudine per filtro geografico
     * @param rangeKm raggio di ricerca geografica in km
     * @param priceMin prezzo minimo, oppure null/"vuoto"
     * @param priceMax prezzo massimo
     * @param hasDelivery "y" per filtrare solo locali con delivery
     * @param hasOnline "y" per filtrare solo locali con prenotazione online
     * @param starsMin rating minimo (0-5)
     * @param starsMax rating massimo (0-5)
     * @param category categoria culinaria da filtrare
     * @param nearMe "y" per attivare la ricerca su coordinate utente
     * @param userId ID utente, usato ad esempio per preferiti
     * @param onlyFavourites {@code true} se mostrare solo ristoranti preferiti
     * @return Lista paginata dei risultati come array di stringhe (id, nome, …)
     * @throws SQLException se fallisce l'accesso al database
     * @throws InterruptedException se l'operazione viene interrotta
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
     * Recupera tutte le informazioni dettagliate di un ristorante.
     *
     * @param id ID univoco del ristorante
     * @return array contenente attributi del ristorante predefiniti,
     *         oppure {@code null} se non esistente
     * @throws SQLException in caso di errori DB
     * @throws InterruptedException se l’operazione viene interrotta
     */
    String[] getRestaurantInfo(int id) throws SQLException, InterruptedException;

     /**
     * Modifica i dati di un ristorante.
     *
     * @param id identificatore del ristorante
     * @param nome nuovo nome
     * @param nazione nuova nazione
     * @param citta nuova città
     * @param indirizzo nuovo indirizzo
     * @param lat latitudine
     * @param lon longitudine
     * @param fasciaPrezzo nuova fascia prezzo
     * @param delivery nuovo stato delivery
     * @param online nuovo stato prenotazione online
     * @param tipoCucina nuova categoria culinaria
     * @return {@code true} se la modifica ha successo
     * @throws SQLException se qualcosa va storto nel database
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
     * Cancella un ristorante esistente.
     *
     * @param id ID del ristorante da eliminare
     * @return {@code true} se l’eliminazione è andata a buon fine
     * @throws SQLException se fallisce la cancellazione
     * @throws InterruptedException se l'esecuzione è interrotta
     */
    boolean deleteRestaurant(int id) throws SQLException, InterruptedException;

    // Metodi MyRestaurants
    /**
     * Restituisce il numero totale di pagine contenenti i ristoranti gestiti dal ristoratore.
     * L'output permette gestione lato client e impaginazione
     * @param userId ID dell'utente ristoratore
     * @return numero totale di pagine
     * @throws SQLException in caso di errore del DB
     * @throws InterruptedException se l'operazione viene interrotta
     */
    int getMyRestaurantsPageCount(int userId) throws SQLException, InterruptedException;

    /**
     * Restituisce l'elenco impaginato dei ristoranti appartenenti ad un ristoratore.
     * Ogni entry contiene almeno ID e nome del ristorante
     * 
     * @param userId ID dell'utente ristoratore
     * @param page numero pagine
     * @return lista di tuple [id,nome], lista vuota nel caso non contiene elementi
     * @throws SQLException in caso di errore del DB
     * @throws InterruptedException se l'esecuzione viene interrotta
     */
    List<String[]> getMyRestaurants(int userId, int page) throws SQLException, InterruptedException;

    /**
     * Restituisce il numero di pagine contenenti recensioni
     * per un determinato ristorante.
     *
     * @param restaurantId ID del ristorante
     * @return totale pagine (≥ 0)
     * @throws SQLException in caso di errore DB
     * @throws InterruptedException se interrotto
     */
    int getReviewsPageCount(int restaurantId) throws SQLException, InterruptedException;

    /**
     * Restituisce l'elenco paginato delle recensioni di un ristorante.
     * Ogni riga contiene i campi:
     *  - reviewId
     *  - stelle assegnate
     *  - testo recensione
     *  - risposta del ristoratore (oppure null)
     *
     * @param restaurantId ID del ristorante
     * @param page numero pagina 0-based
     * @return matrice bidimensionale con le recensioni
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    String[][] getReviews(int restaurantId, int page) throws SQLException, InterruptedException;

    /**
     * Inserisce una nuova recensione creata dall'utente sul ristorante indicato.
     *
     * @param userId ID autore recensione
     * @param restaurantId ID ristorante recensito
     * @param stars numero stelle (1–5)
     * @param text testo recensione
     * @return true se inserita correttamente, false altrimenti
     * @throws SQLException errore database
     * @throws InterruptedException se interrotto
     */
    boolean addReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException;

    /**
     * Modifica una recensione esistente dell'utente loggato.
     *
     * @param userId ID autore recensione
     * @param restaurantId ID ristorante recensito
     * @param stars nuove stelle assegnate
     * @param text nuovo contenuto recensione
     * @return true se modificata correttamente
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean editReview(int userId, int restaurantId, int stars, String text)
            throws SQLException, InterruptedException;

    /**
     * Rimuove la recensione associata all'utente per il ristorante indicato.
     *
     * @param userId ID autore recensione
     * @param restaurantId ID ristorante recensito
     * @return true se eliminata, false altrimenti
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean removeReview(int userId, int restaurantId)
            throws SQLException, InterruptedException;

    /**
     * Ottiene la recensione dell'utente sul ristorante indicato.
     *
     * @param userId ID autore recensione
     * @param restaurantId ID ristorante
     * @return array [stelle, testo], oppure null se non esiste
     * @throws SQLException errore database
     * @throws InterruptedException se interrotto
     */
    String[] getMyReview(int userId, int restaurantId)
            throws SQLException, InterruptedException;

    /**
     * Restituisce il numero di pagine contenenti recensioni scritte dall'utente.
     *
     * @param userId ID autore recensioni
     * @return numero pagine disponibili
     * @throws SQLException errore database
     * @throws InterruptedException se interrotto
     */
    int getMyReviewsPageCount(int userId) throws SQLException, InterruptedException;

    /**
     * Restituisce l'elenco paginato delle recensioni scritte dall'utente.
     * Ogni entry contiene almeno:
     *  - nome ristorante
     *  - stelle assegnate
     *  - testo recensione
     *
     * @param userId ID autore recensioni
     * @param page pagina 0-based
     * @return lista di risultati (vuota se nessuna recensione)
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    List<String[]> getMyReviews(int userId, int page)
            throws SQLException, InterruptedException;
    
    /**
     * Restituisce la risposta (commento del ristoratore) associata alla recensione.
     *
     * @param reviewId ID della recensione
     * @return testo risposta oppure null se non esiste
     * @throws SQLException errore database
     * @throws InterruptedException se interrotto
     */
    String getResponse(int reviewId) throws SQLException, InterruptedException;

    /**
     * Inserisce una risposta del ristoratore alla recensione identificata.
     *
     * @param reviewId ID recensione
     * @param text contenuto della risposta
     * @return true se aggiunta, false altrimenti
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean addResponse(int reviewId, String text)
            throws SQLException, InterruptedException;

    /**
     * Aggiorna una risposta esistente.
     *
     * @param reviewId ID recensione collegata
     * @param text nuovo contenuto risposta
     * @return true se modificata correttamente
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean editResponse(int reviewId, String text)
            throws SQLException, InterruptedException;

    /**
     * Elimina la risposta associata alla recensione.
     *
     * @param reviewId ID review associata
     * @return true se eliminata correttamente
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean removeResponse(int reviewId) throws SQLException, InterruptedException;

    /**
     * Verifica se un ristorante è già tra i preferiti dell'utente.
     *
     * @param userId ID utente
     * @param restaurantId ID ristorante
     * @return true se preferito, false se non presente
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean isFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException;

    /**
     * Aggiunge un ristorante ai preferiti dell'utente.
     *
     * @param userId ID utente
     * @param restaurantId ID ristorante
     * @return true se aggiunto correttamente
     * @throws SQLException errore DB
     * @throws InterruptedException se interrotto
     */
    boolean addFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException;

    /**
     * Rimuove un ristorante dai preferiti dell'utente.
     *
     * @param userId ID utente
     * @param restaurantId ID ristorante
     * @return true se eliminato correttamente
     * @throws SQLException errore database
     * @throws InterruptedException se interrotto
     */
    boolean removeFavourite(int userId, int restaurantId)
            throws SQLException, InterruptedException;
}
