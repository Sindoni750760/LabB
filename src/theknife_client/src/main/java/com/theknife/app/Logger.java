package com.theknife.app;

/**
 * Interfaccia di astrazione per la gestione dei log nell'applicazione.
 * <p>
 * Permette l'utilizzo di differenti implementazioni concrete (es. console log,
 * scrittura su file, remote logging, ecc.) mantenendo un'unica API uniforme.
 * </p>
 *
 * <p>I livelli previsti sono:</p>
 * <ul>
 *     <li>{@code info} → messaggi informativi e di flusso</li>
 *     <li>{@code warning} → notifiche non bloccanti ma potenzialmente rilevanti</li>
 *     <li>{@code error} → errori applicativi o di comunicazione</li>
 *     <li>{@code alert} → messaggi "critici" o di rilievo immediato per l'utente</li>
 * </ul>
 */
public interface Logger {

    /**
     * Registra un messaggio di livello informativo.
     *
     * @param msg messaggio da registrare
     */
    void info(String msg);

    /**
     * Registra un messaggio di livello warning.
     * Usato per segnalare condizioni anomale ma non critiche.
     *
     * @param msg messaggio da registrare
     */
    void warning(String msg);

    /**
     * Registra un messaggio di errore.
     * Utilizzato quando la logica o la comunicazione falliscono.
     *
     * @param msg messaggio da registrare
     */
    void error(String msg);

    /**
     * Registra un messaggio di livello alert.
     * Indicato per notifiche urgenti o particolarmente rilevanti.
     *
     * @param msg messaggio da registrare
     */
    void alert(String msg);
}
