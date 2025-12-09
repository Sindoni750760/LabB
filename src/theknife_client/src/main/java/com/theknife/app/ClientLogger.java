package com.theknife.app;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementazione concreta del logger per il lato client dell'applicazione.
 *
 * <p>Questa classe fornisce un meccanismo di logging centralizzato basato
 * sul pattern singleton e implementa l'interfaccia {@link Logger}.</p>
 *
 * <p>Caratteristiche principali:</p>
 * <ul>
 *     <li>metodi di log differenziati per livello (info, warning, error, alert)</li>
 *     <li>timestamp unificato secondo formato <code>yyyy-MM-dd HH:mm:ss</code></li>
 *     <li>sincronizzazione dei metodi di scrittura per garantire thread-safety</li>
 *     <li>output diretto su standard output</li>
 * </ul>
 *
 * <p>Questo logger è utilizzato da tutti i controller client per tracciare
 * l'esecuzione delle azioni dell'applicazione.</p>
 *
 * @see Logger
 */

public class ClientLogger implements Logger {

    /** Istanza singleton della classe. */
    private static ClientLogger instance = null;

    /** Formato timestamp applicato a ogni messaggio di log. */
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Costruttore privato per rispettare il pattern Singleton.
     */
    private ClientLogger() {}

    /**
     * Restituisce l'unica istanza disponibile del logger.
     *
     * @return oggetto {@link ClientLogger} condiviso
     */
    public static synchronized ClientLogger getInstance() {
        if (instance == null)
            instance = new ClientLogger();
        return instance;
    }

    /**
     * Registra un messaggio generico, associandolo a un livello
     * e precedendolo da timestamp formattato.
     *
     * <p>Questo metodo è sincronizzato per permettere invocazioni da thread multipli.</p>
     *
     * @param level livello del log (INFO, WARNING, ERROR, ALERT)
     * @param msg   testo del messaggio da registrare
     */
    private synchronized void log(String level, String msg) {
        String time = sdf.format(new Date());
        System.out.println("[" + time + "][" + level + "] " + msg);
    }

    /**
     * Registra messaggi informativi relativi a operazioni riuscite
     * o flussi logici rilevanti.
     *
     * @param msg contenuto del messaggio
     */
    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    /**
     * Registra messaggi di avviso riguardanti operazioni non interrotte,
     * ma che potrebbero richiedere attenzione.
     *
     * @param msg contenuto del messaggio
     */
    @Override
    public void warning(String msg) {
        log("WARNING", msg);
    }

    /**
     * Registra messaggi relativi a errori che interrompono operazioni
     * o comportamenti inattesi dell'applicazione.
     *
     * @param msg contenuto del messaggio
     */
    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    /**
     * Registra messaggi di tipo alert, utili per segnalare
     * stati imprevisti all'utente o condizioni di pericolo logico.
     *
     * @param msg contenuto del messaggio
     */
    @Override
    public void alert(String msg) {
        log("ALERT", msg);
    }
}
