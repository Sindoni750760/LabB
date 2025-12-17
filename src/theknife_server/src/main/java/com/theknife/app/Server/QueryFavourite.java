package com.theknife.app.Server;

/**
 * Interfaccia che definisce le operazioni relative alla gestione dei ristoranti preferiti.
 * 
 * <p>
 * Le implementazioni concrete di questa interfaccia sono responsabili dell'esecuzione delle query SQL
 * e della gestione delle eccezioni.
 * </p>
 * <p>
 * Questa interfaccia viene utilizzata nel layer {@code Server} per separare la definizione delle operazioni
 * dalla loro implementazione concreta
 * </p>
 * 
 */
public interface QueryFavourite {

    /**
     * Verifica se un ristorante è presente tra i preferiti di un utente.
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se il ristorante è tra i preferiti dell'utente,
     *         {@code false} altrimenti
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean isFavourite(int userId, int restId) throws Exception;

    /**
     * Aggiunge un ristorante alla lista dei preferiti di un utente.
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se l'operazione di inserimento ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean addFavourite(int userId, int restId) throws Exception;

    /**
     * Rimuove un ristorante dalla lista dei preferiti di un utente.
     *
     * @param userId id dell'utente
     * @param restId id del ristorante
     * @return {@code true} se la rimozione ha avuto successo
     * @throws Exception in caso di errore durante l'accesso ai dati
     */
    boolean removeFavourite(int userId, int restId) throws Exception;
}
