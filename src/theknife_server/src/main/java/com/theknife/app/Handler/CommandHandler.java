package com.theknife.app.Handler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Interfaccia che definisce il ruolo per gli handler deputati alla gestione
 * dei comandi testuali inviati dal client.
 *
 * <p>
 * Ogni implementazione di questa interfaccia si occupa di:
 * </p>
 *
 * <ul>
 *     <li>riconoscere un sottoinsieme di comandi specifico (es. login, register, ecc.)</li>
 *     <li>interpretare parametri aggiuntivi ricevuti attraverso il {@link ClientContext}</li>
 *     <li>produrre una risposta corretta secondo il protocollo applicativo</li>
 * </ul>
 * @see ClientContext
 */
public interface CommandHandler {

    /**
     * Elabora il comando ricevuto dal client.
     *
     * <p>Flusso tipico:</p>
     * <ol>
     *     <li>verifica che il comando {@code cmd} sia supportato dall'handler</li>
     *     <li>legge eventuali parametri aggiuntivi tramite {@link ClientContext#read()}</li>
     *     <li>invoca i servizi di dominio opportuni</li>
     *     <li>scrive la risposta tramite {@link ClientContext#write(String)}</li>
     * </ol>
     *
     * <p>Il metodo <strong>non solleva direttamente eccezioni applicative</strong>,
     * ma notifica errori tramite eccezioni tecniche o messaggi sul canale di output.</p>
     *
     * @param cmd comando ricevuto dal client (prima riga del protocollo)
     * @param ctx contesto di comunicazione associato alla sessione client
     *
     * @return {@code true} se il comando è stato gestito correttamente da questo handler,
     *         {@code false} se il comando non rientra tra quelli supportati
     *
     * @throws IOException se avvengono errori di I/O sulla socket
     * @throws SQLException se avvengono errori nelle operazioni sul database
     * @throws InterruptedException se il thread di gestione viene interrotto
     */
    boolean handle(String cmd, ClientContext ctx) throws IOException, SQLException, InterruptedException;
}
