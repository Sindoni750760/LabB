package com.theknife.app.Server;

/**
 * Interfaccia che definisce le operazioni di ricerca avanzata dei ristoranti.
 * 
 * <p>
 * Fornisce il contratto per l'esecuzione di ricerche complesse sui ristoranti,
 * combinando filtri geografici, economici, qualitativi e funzionali.
 * </p>
 *
 * <p>
 * Le implementazioni concrete di questa interfaccia sono responsabili
 * della costruzione delle query SQL dinamiche e della gestione
 * delle eccezioni di accesso ai dati.
 * </p>
 *
 * <p>
 * Questa interfaccia viene utilizzata dal layer {@code Server}
 * per separare la definizione delle operazioni di ricerca
 * dalla loro implementazione concreta.
 * </p>
 */
public interface QueryRestaurantSearch {
    
    /**
     * Esegue una ricerca avanzata di ristoranti applicando i filtri specificati.
     *
     * <p>
     * I parametri possono essere combinati liberamente; i valori {@code null}
     * o non validi vengono ignorati dall'implementazione concreta.
     * </p>
     *
     * @param page indice della pagina richiesta (0-based)
     * @param nation nazione del ristorante oppure {@code null}
     * @param city città del ristorante oppure {@code null}
     * @param lat latitudine geografica di riferimento oppure {@code null}
     * @param lon longitudine geografica di riferimento oppure {@code null}
     * @param rangeKm raggio massimo di ricerca in chilometri oppure {@code null}
     * @param priceMin prezzo minimo oppure {@code null}
     * @param priceMax prezzo massimo oppure {@code null}
     * @param delivery indica se filtrare per servizio di delivery
     * @param online indica se filtrare per prenotazione online
     * @param starsMin numero minimo di stelle oppure {@code null}
     * @param starsMax numero massimo di stelle oppure {@code null}
     * @param favouriteUserId id dell'utente per filtrare i preferiti
     *                         ({@code -1} se non richiesto)
     * @param category categoria del ristorante oppure {@code null}
     *
     * @return matrice contenente i ristoranti risultanti dalla ricerca
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
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
