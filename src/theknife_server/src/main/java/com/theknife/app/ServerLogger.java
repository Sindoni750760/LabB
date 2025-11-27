package com.theknife.app;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger singleton per il lato server dell'applicazione.
 * Fornisce metodi per registrare messaggi di info, warning, error e alert.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ServerLogger implements Logger {

    /** Istanza singleton del logger server. */
    private static ServerLogger instance = null;

    /** Formato per visualizzare la data e l'ora nei log. */
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Costruttore privato per il pattern singleton.
     */
    private ServerLogger() {}

    /**
     * Restituisce l'istanza singleton del logger server.
     *
     * @return istanza singleton di ServerLogger
     */
    public static synchronized ServerLogger getInstance() {
        if (instance == null)
            instance = new ServerLogger();
        return instance;
    }

    /**
     * Registra un messaggio nel log con il livello specificato.
     *
     * @param level livello del log (INFO, WARNING, ERROR, ALERT)
     * @param msg messaggio da registrare
     */
    private synchronized void log(String level, String msg) {
        String time = sdf.format(new Date());
        System.out.println("[" + time + "][" + level + "] " + msg);
    }

    /**
     * Registra un messaggio di livello INFO.
     *
     * @param msg messaggio da registrare
     */
    @Override
    public void info(String msg) {
        log("INFO", msg);
    }

    /**
     * Registra un messaggio di livello WARNING.
     *
     * @param msg messaggio da registrare
     */
    @Override
    public void warning(String msg) {
        log("WARNING", msg);
    }

    /**
     * Registra un messaggio di livello ERROR.
     *
     * @param msg messaggio da registrare
     */
    @Override
    public void error(String msg) {
        log("ERROR", msg);
    }

    /**
     * Registra un messaggio di livello ALERT.
     *
     * @param msg messaggio da registrare
     */
    @Override
    public void alert(String msg) {
        log("ALERT", msg);
    }
}
