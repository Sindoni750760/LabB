package com.theknife.app.Handler;

import java.io.IOException;

/**
 * Eccezione personalizzata sollevata lato server durante la gestione di una sessione client.
 * <p>
 * Estende {@link IOException} poiché viene tipicamente generata in situazioni legate
 * alla comunicazione socket o alla manipolazione dello stream del client.
 * </p>
 *
 * <p>Può essere utilizzata in contesti quali:</p>
 * <ul>
 *     <li>messaggi non validi inviati dal client</li>
 *     <li>violazioni del protocollo applicativo</li>
 *     <li>errori di I/O relativi alla sessione client</li>
 *     <li>terminazioni improvvise della connessione</li>
 * </ul>
 *
 * <p>
 * L'uso di una specifica eccezione migliora la distinzione semantica tra
 * errori di comunicazione previsti e eccezioni generiche I/O.
 * </p>
 */
public class ClientException extends IOException{
     /**
     * Costruisce una nuova eccezione client con un messaggio descrittivo.
     *
     * @param message testo esplicativo della causa dell'errore
     */
    public ClientException(String message){
        super(message);
    }
}
