package com.theknife.app.Server;

/**
 * Interfaccia che definisce le operazioni di accesso ai dati relative alle risposte
 * alle recensioni
 * 
 * <p>
 * Le implementazioni concrete di questa interfaccia si occupano dell'esecuzione delle
 * query SQL e delle eccezioni di basso livello.
 * </p>
 * 
 * <p>
 * Questa interfaccia viene utilizzata dal layer {@code Server} per separare la definizione 
 * delle operazioni dalla loro implementazione concreta
 * </p>
 */
public interface QueryResponse {

    /**
     * Verifica se l'utente è autorizzato a rispondere a una recensione.
     *
     * <p>
     * Il controllo viene effettuato tipicamente verificando che l'utente
     * sia il proprietario del ristorante associato alla recensione.
     * </p>
     *
     * @param userId id dell'utente
     * @param reviewId id della recensione
     * @return {@code true} se l'utente può rispondere alla recensione
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean canRespond(int userId, int reviewId) throws Exception;

    /**
     * Restituisce la risposta associata a una recensione.
     *
     * @param reviewId id della recensione
     * @return testo della risposta oppure {@code null} se non presente
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    String getResponse(int reviewId) throws Exception;

    /**
     * Aggiunge una risposta a una recensione.
     *
     * @param reviewId id della recensione
     * @param text testo della risposta
     * @return {@code true} se l'inserimento ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean addResponse(int reviewId, String text) throws Exception;

    /**
     * Modifica la risposta associata a una recensione.
     *
     * @param reviewId id della recensione
     * @param text nuovo testo della risposta
     * @return {@code true} se la modifica ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean editResponse(int reviewId, String text) throws Exception;
    /**
     * Rimuove la risposta associata a una recensione.
     *
     * @param reviewId id della recensione
     * @return {@code true} se la rimozione ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean removeResponse(int reviewId) throws Exception;
}
