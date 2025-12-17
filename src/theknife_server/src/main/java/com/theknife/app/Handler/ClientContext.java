package com.theknife.app.Handler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Rappresenta il contesto di sessione associato ad un singolo client connesso al server.
 * <p>
 * Questa classe incapsula:
 * </p>
 * <ul>
 *     <li>il socket della connessione TCP</li>
 *     <li>lo stream di input e output testuale</li>
 *     <li>lo stato dell'utente autenticato</li>
 *     <li>lo stato di attività della sessione</li>
 * </ul>
 *
 * <p>
 * Il {@code ClientContext} viene istanziato per ogni nuovo client connesso
 * ed è responsabile della comunicazione in modalità request/response testuale,
 * tipica del protocollo applicativo del sistema TheKnife.
 * </p>
 *
 * <p>
 * La classe non gestisce autonomamente la business logic dei comandi;
 * funge invece da supporto per manager e handler
 * (es. {@link AuthHandler}, {@link CommandHandler})
 * fornendo un accesso thread-safe agli stream e allo stato utente.
 * </p>
 *
 * <p><b>Nota:</b> Una sessione viene considerata attiva fino a quando
 * il socket è aperto e non è stato invocato {@link #deactivate()}.</p>
 */
public class ClientContext {

    /** Socket della connessione client-server. */
    private final Socket socket;
    
    /** Reader usato per ricevere messaggi testuali dal client. */
    private final BufferedReader in;
    
    /** Writer usato per inviare messaggi testuali al client. */
    private final BufferedWriter out;

    /**
     * Identificatore utente della sessione corrente.
     * <p>
     * Valori speciali:
     * <ul>
     *     <li>{@code -1} → nessun utente autenticato</li>
     *     <li>{@code > 0} → id utente autenticato lato DB</li>
     * </ul>
     */    
    private int loggedUserId = -1;
    
    /**
     * Flag che indica se la connessione è ancora attiva.
     * Utilizzato dagli handler per interrompere elaborazioni future.
     */
    private volatile boolean active = true;

    /**
     * Costruisce un nuovo contesto sessione per un client.
     *
     * @param socket socket della connessione attiva
     * @throws IOException se non è possibile ottenere gli stream di I/O
     */
    public ClientContext(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );
        this.out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
        );
    }

    /**
     * Restituisce l'ID dell'utente attualmente autenticato.
     *
     * @return id utente oppure {@code -1} se non autenticato
     */
    public int getLoggedUserId() {
        return loggedUserId;
    }

    /**
     * Imposta l'utente autenticato nella sessione corrente.
     *
     * @param loggedUserId id utente ottenuto dal database
     */
    public void setLoggedUserId(int loggedUserId) {
        this.loggedUserId = loggedUserId;
    }

     /**
     * Indica se il contesto è ancora valido e utilizzabile.
     *
     * @return {@code true} se attivo, {@code false} dopo chiusura/disconnessione
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Marca la sessione come chiusa dal punto di vista logico
     * (lato protocollo o gestione interna).
     * <p>
     * La sessione rimane fisicamente connessa finché non si invoca {@link #close()}.
     * </p>
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Legge una riga testuale dal client.
     * <p>
     * Il metodo è bloccante finché non arriva un messaggio o il client chiude la connessione.
     * </p>
     *
     * @return la stringa letta oppure {@code null} se la connessione è stata chiusa
     * @throws IOException se si verificano errori di I/O sul socket
     */
    public String read() throws IOException {
        if(!active){
            return null;
        }
        return in.readLine();
    }

     /**
     * Invia un messaggio testuale al client seguito da newline.
     *
     * @param msg testo da inviare
     * @throws IOException se non è possibile scrivere sul socket
     */
    public void write(String msg) throws IOException {
        if(!active){
            return;
        }
        out.write(msg);
        out.write("\n");
        out.flush();
    }

     /**
     * Chiude tutte le risorse associate alla sessione:
     * <ul>
     *     <li>input stream</li>
     *     <li>output stream</li>
     *     <li>socket TCP</li>
     * </ul>
     *
     * <p>
     * Dopo l'invocazione di questo metodo, la sessione non è più considerata valida.
     * </p>
     */
    public void close() {
        active = false;
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
    }
}
