package com.theknife.app;

/**
 * Interfaccia per il logging dell'applicazione.
 * Definisce metodi standard per registrare messaggi a diversi livelli di gravità.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public interface Logger {

    /**
     * Registra un messaggio di livello INFO.
     *
     * @param msg messaggio da registrare
     */
    void info(String msg);
    
    /**
     * Registra un messaggio di livello WARNING.
     *
     * @param msg messaggio da registrare
     */
    void warning(String msg);
    
    /**
     * Registra un messaggio di livello ERROR.
     *
     * @param msg messaggio da registrare
     */
    void error(String msg);
    
    /**
     * Registra un messaggio di livello ALERT.
     *
     * @param msg messaggio da registrare
     */
    void alert(String msg);

}
