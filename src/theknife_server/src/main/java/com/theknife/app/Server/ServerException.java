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

    public ServerException(String message) {
        super(message);
        ServerLogger.getInstance().error(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
        ServerLogger.getInstance().error(message + " | causa: " + cause.getMessage());
    }
}
 