package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Interfaccia per i gestori dei comandi del client.
 * Ogni handler elabora uno specifico insieme di comandi e fornisce
 * la logica necessaria per rispondere ai comandi del client.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public interface CommandHandler {

    /**
     * Gestisce il comando se riconosciuto.
     * Ritorna false se il comando non è di competenza di questo handler,
     * permettendo la delega ad altri handler.
     *
     * @param cmd comando letto dal client (prima riga)
     * @param ctx contesto della sessione client con cui comunicare
     * @return true se il comando è stato gestito, false se non è di competenza
     * @throws IOException se si verifica un errore di I/O durante la comunicazione
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    boolean handle(String cmd, ClientContext ctx) throws IOException, SQLException, InterruptedException;
}
