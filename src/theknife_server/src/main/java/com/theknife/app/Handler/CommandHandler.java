package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

public interface CommandHandler {

    /**
     * Gestisce il comando se riconosciuto.
     *
     * @param cmd comando letto dal client (prima riga)
     * @param ctx contesto della sessione client
     * @return true se il comando è stato gestito, false se non è di competenza
     * @throws InterruptedException 
     */
    boolean handle(String cmd, ClientContext ctx) throws IOException, SQLException, InterruptedException;
}
