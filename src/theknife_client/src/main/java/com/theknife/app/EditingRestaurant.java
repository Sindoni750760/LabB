package com.theknife.app;

import java.io.IOException;

/**
 * Gestisce lo stato del ristorante attualmente selezionato nell'applicazione lato client.
 *
 * <p>Questa classe funge da contenitore statico (stato globale condiviso)
 * e memorizza sul client informazioni relative al ristorante aperto
 * per visualizzazione o modifica.</p>
 *
 * <p>È utilizzata nei workflow relativi a:</p>
 * <ul>
 *     <li>visualizzazione dettagli ristorante</li>
 *     <li>modifica dati di un ristorante appartenente al ristoratore</li>
 *     <li>gestione recensioni o risposte collegate al ristorante</li>
 * </ul>
 *
 * <p>Il metodo {@link #setEditing(int)} interroga il server e aggiorna
 * le informazioni locali del ristorante. Tutte le successive letture
 * vengono effettuate da memoria locale tramite {@link #getInfo()}.</p>
 */
public class EditingRestaurant {
    /** Identificativo del ristorante attualmente selezionato (o -1 se nessuno). */    
    private static int editing_id = -1;
    /** Identificativo della recensione selezionata del ristorante (se utilizzato). */
    private static int review_id = -1;

    private static String name, nation, city, address, latitude, longitude;
    private static String avg_price, has_delivery, has_online;
    private static String avg_stars, n_reviews, categories;

    /**
     * Imposta un ristorante come "attualmente selezionato" e ne recupera
     * tutte le informazioni dal server.
     *
     * <p>Esegue la richiesta:</p>
     * <pre>
     * getRestaurantInfo
     * id
     * </pre>
     *
     * <p>Il server risponde con 12 campi nell'ordine definito,
     * che vengono memorizzati localmente.</p>
     *
     * @param id identificativo del ristorante
     * @throws IOException se la connessione fallisce o il server non risponde
    */
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

    /**
     * Annulla lo stato attuale, eliminando riferimenti a ristorante
     * e a eventuali recensioni selezionate.
     *
     * <p>Usato durante la navigazione quando si torna alla lista generale.</p>
     */
    public static void reset() {
        editing_id = -1;
        review_id = -1;
    }

    /**
     * Restituisce l'ID del ristorante attualmente in uso.
     *
     * @return ID ristorante oppure -1 se nessuno
     */
    public static int getId() {
        return editing_id;
    }

    /**
     * Restituisce un vettore contenente tutte le informazioni memorizzate
     * sul ristorante, nell’ordine previsto dal protocollo di risposta server.
     *
     * Indici nel vettore risultante:
     * <pre>
     * 0  → nome
     * 1  → nazione
     * 2  → città
     * 3  → indirizzo
     * 4  → latitudine
     * 5  → longitudine
     * 6  → prezzo medio
     * 7  → flag delivery
     * 8  → flag prenotazione online
     * 9  → media stelle
     * 10 → numero recensioni
     * 11 → categorie
     * </pre>
     *
     * @return array di stringhe con informazioni ristorante
     */
    public static String[] getInfo() {
        return new String[]{
            name, nation, city, address, latitude, longitude,
            avg_price, has_delivery, has_online, avg_stars,
            n_reviews, categories
        };
    }

    /**
     * Invia al server i dati di un nuovo ristorante da inserire.
     *
     * <p>Il flusso prevede:</p>
     * <pre>
     * addRestaurant
     * campi nel rispettivo ordine
     * </pre>
     *
     * @param name nome ristorante
     * @param nation nazione
     * @param city città
     * @param address indirizzo
     * @param latitude latitudine
     * @param longitude longitudine
     * @param avg_price fascia di prezzo media
     * @param categories categorie cucina
     * @param has_delivery disponibilità delivery
     * @param has_online disponibilità prenotazione
     *
     * @return risposta grezza del server (tipicamente: "ok" | errore specifico)
     *
     * @throws IOException se interviene un errore di comunicazione
     */
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

    /**
     * Richiede al server la modifica dei dati del ristorante identificato da {@code id}.
     *
     * @param id identificativo ristorante
     * @param name nome aggiornato
     * @param nation nazione aggiornata
     * @param city città aggiornata
     * @param address indirizzo aggiornato
     * @param latitude latitudine aggiornata
     * @param longitude longitudine aggiornata
     * @param avg_price fascia prezzo aggiornata
     * @param categories categorie aggiornate
     * @param has_delivery flag delivery
     * @param has_online flag prenotazione online
     *
     * @return risposta del server (es. "ok", "error")
     * @throws IOException se la connessione fallisce
     */
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

   /**
     * Memorizza l'ID della recensione selezionata per operazioni
     * di risposta o modifica lato ristoratore.
     *
     * @param id identificativo recensione
     */
    public static void setReviewId(int id) {
        review_id = id;
    }
    
    /**
     * Restituisce l'ID della recensione memorizzata.
     *
     * @return review_id oppure -1 se nessuna
     */
    public static int getReviewId() {
        return review_id;
    }
}
