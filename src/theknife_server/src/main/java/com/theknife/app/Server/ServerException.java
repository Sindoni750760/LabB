package com.theknife.app.Server;

import com.theknife.app.ServerLogger;

/**
 * Eccezione applicativa del server.
 *
 * <p>
 * Wrappa tutte le eccezioni critiche del backend
 * (DB, concorrenza, logica applicativa).
 * </p>
 *
 * <p>
 * Centralizza il logging degli errori gravi.
 * </p>
 */
public class ServerException extends RuntimeException {

    /**
     * Crea una ServerException con messaggio descrittivo
     * @param message messaggio di errore
     */
    public ServerException(String message) {
        super(message);
        ServerLogger.getInstance().error(message);
    }

    /**
     * Crea una ServerException con causa originale
     * @param message messaggio di errore
     * @param cause eccezione originaria
     */
    public ServerException(String message, Throwable cause) {
        super(message, cause);
        ServerLogger.getInstance().error(message + " | causa: " + cause.getMessage());
    }
}
 