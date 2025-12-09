package com.theknife.app;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementazione singleton di un logger lato server.
 * <p>
 * Questa classe centralizza tutte le operazioni di logging
 * provenienti dal backend, garantendo:
 * </p>
 * <ul>
 *     <li>consistenza nel formato dei messaggi</li>
 *     <li>thread-safety tramite sincronizzazione interna</li>
 *     <li>livelli di severità multipli (INFO, WARNING, ERROR, ALERT)</li>
 *     <li>timestamp associato a ogni output</li>
 * </ul>
 *
 * <p>
 * Pattern implementato: Singleton.
 * L'accesso è sincronizzato in {@link #getInstance()} per garantire
 * la sicurezza nel caso di inizializzazioni parallele.
 * </p>
 *
 * @author
 *     Mattia Sindoni 750760 VA<br>
 *     Erica Faccio 751654 VA<br>
 *     Giovanni Isgrò 753536 VA
 */
public class ServerLogger implements Logger {

    /** Riferimento all’istanza singleton del logger. */
    private static ServerLogger instance = null;

    /**
     * Formatter per data e ora dei messaggi di log.
     * <p>Formato: {@code yyyy-MM-dd HH:mm:ss}</p>
     */
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** Costruttore privato per impedire istanzazioni esterne. */
    private ServerLogger() {}

    /**
     * Restituisce l’unica istanza del logger server.
     * <p>
     * Il metodo è sincronizzato per garantire che in scenari multi-thread
     * la prima inizializzazione sia corretta.
     * </p>
     *
     * @return istanza globale di {@link ServerLogger}
     */
    public static synchronized ServerLogger getInstance() {
        if (instance == null)
            instance = new ServerLogger();
        return instance;
    }

    /**
     * Metodo interno, thread-safe, che stampa il messaggio di log
     * formattato secondo il seguente schema:
     *
     * <pre>
     * [yyyy-MM-dd HH:mm:ss][LEVEL] messaggio
     * </pre>
     *
     * @param level livello del messaggio (INFO, WARNING, ERROR, ALERT)
     * @param msg messaggio da loggare
     */
    private synchronized void log(String level, String msg) {
        String time = sdf.format(new Date());
        System.out.println("[" + time + "][" + level + "] " + msg);
    }

    /**
     * Log di tipo informativo.
     * <p>Utilizzare per eventi di normale funzionamento.</p>
     *
     * @param msg messaggio informativo
     */
    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    /**
     * Log di avviso.
     * <p>
     * Segnala condizioni non critiche ma potenzialmente anomale
     * o prossime a diventare errori.
     * </p>
     *
     * @param msg messaggio di warning
     */
    @Override
    public void warning(String msg) {
        log("WARNING", msg);
    }

    /**
     * Log di errore.
     * <p>
     * Indica un problema significativo nel flusso del server
     * ma che potrebbe non compromettere l’esecuzione generale.
     * </p>
     *
     * @param msg descrizione dell’errore
     */
    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    /**
     * Log critico/urgente.
     * <p>
     * Utilizzare quando l'errore richiede immediata attenzione dell’operatore
     * o consiste in una condizione instabile per la continuità del servizio.
     * </p>
     *
     * @param msg messaggio critico
     */
    @Override
    public void alert(String msg) {
        log("ALERT", msg);
    }
}
