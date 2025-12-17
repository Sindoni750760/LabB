package com.theknife.app.Server;

/**
 * Interfaccia che definisce le operazioni di accesso ai dati relative alla gestione dei ristoranti.
 * 
 * <p>
 * Le implementazioni concrete di questa interfaccia sono responsabili
 * dell'esecuzione delle query SQL e della gestione delle eccezioni
 * di basso livello.
 * </p>
 * 
 * <p>
 * Questa interfaccia viene utilizzata dal layer {@code Server}
 * per separare la definizione delle operazioni dalla loro
 * implementazione concreta,
 * </p>
 */
public interface QueryRestaurant {

    /**
     * Inserisce un nuovo ristorante nel sistema.
     *
     * @param ownerId id dell'utente proprietario del ristorante
     * @param name nome del ristorante
     * @param nation nazione del ristorante
     * @param city città del ristorante
     * @param address indirizzo del ristorante
     * @param lat latitudine geografica
     * @param lon longitudine geografica
     * @param price fascia di prezzo
     * @param tipoCucina tipologia di cucina
     * @param delivery indica se è disponibile il servizio di delivery
     * @param online indica se è disponibile la prenotazione online
     * @return {@code true} se l'inserimento ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean addRestaurant(int ownerId, String name, String nation, String city,
                          String address, double lat, double lon,
                          int price, String tipoCucina,
                          boolean delivery, boolean online)
            throws Exception;

    /**
     * Modifica le informazioni di un ristorante esistente.
     *
     * @param restId id del ristorante
     * @param name nuovo nome del ristorante
     * @param nation nuova nazione
     * @param city nuova città
     * @param address nuovo indirizzo
     * @param lat nuova latitudine geografica
     * @param lon nuova longitudine geografica
     * @param price nuova fascia di prezzo
     * @param tipoCucina nuova tipologia di cucina
     * @param delivery indica se è disponibile il servizio di delivery
     * @param online indica se è disponibile la prenotazione online
     * @return {@code true} se la modifica ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean editRestaurant(int restId, String name, String nation, String city,
                           String address, double lat, double lon,
                           int price, String tipoCucina,
                           boolean delivery, boolean online)
            throws Exception;

    /**
     * Elimina un ristorante dal sistema.
     *
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean deleteRestaurant(int restId) throws Exception;
    
    /**
     * Restituisce le informazioni complete di un ristorante.
     *
     * @param restId id del ristorante
     * @return array contenente i dati del ristorante
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    String[] getRestaurantInfo(int restId) throws Exception;
}
