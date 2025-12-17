package com.theknife.app.Server;

/**
 * Interfaccia che definisce le operazioni di accesso ai dati relative alle
 * recensioni dei ristoaranti
 * 
 * <p>
 * Le implementazioni concrete di questa interfaccia sono responsabili
 * dell'esecuzione delle query SQL e della gestione delle eccezioni
 * di basso livello.
 * </p>
 * 
 * <p>
 * Questa interfaccia viene utilizzata dal layer {@code Server} per separare la definizione
 * delle operazioni dalla loro implementazione concreta.
 * </p>
 */
public interface QueryReview {

    /**
     * Restituisce il numero totale di pagine di recensioni
     * associate a un ristorante.
     *
     * @param restId id del ristorante
     * @return numero di pagine disponibili
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    int getReviewsPageCount(int restId) throws Exception;

    /**
     * Restituisce una pagina di recensioni associate a un ristorante.
     *
     * @param restId id del ristorante
     * @param page indice della pagina richiesta (0-based)
     * @return matrice contenente le recensioni della pagina
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    String[][] getReviews(int restId, int page) throws Exception;

    /**
     * Restituisce la recensione inserita da un utente per un ristorante.
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return array contenente i dati della recensione oppure {@code null}
     *         se l'utente non ha inserito una recensione
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    String[] getMyReview(int userId, int restId) throws Exception;

    /**
     * Inserisce una nuova recensione per un ristorante.
     *
     * @param userId id dell'utente autore della recensione
     * @param restId id del ristorante
     * @param stars valutazione in stelle
     * @param text testo della recensione
     * @return {@code true} se l'inserimento ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean addReview(int userId, int restId, int stars, String text) throws Exception;

    /**
     * Modifica una recensione esistente.
     *
     * @param userId id dell'utente autore della recensione
     * @param restId id del ristorante
     * @param stars nuova valutazione in stelle
     * @param text nuovo testo della recensione
     * @return {@code true} se la modifica ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean editReview(int userId, int restId, int stars, String text) throws Exception;

    /**
     * Rimuove una recensione inserita da un utente per un ristorante.
     *
     * @param userId id dell'utente autore della recensione
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean removeReview(int userId, int restId) throws Exception;
}
