package com.theknife.app.Handler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Contesto di una sessione client.
 * Gestisce la lettura e scrittura di messaggi tramite socket,
 * mantiene lo stato della sessione (utente loggato, stato attivo).
 * Fornisce metodi thread-safe per l'accesso ai dati della sessione.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ClientContext {

    /** Socket della connessione client. */
    private final Socket socket;
    
    /** Reader per leggere messaggi dal client. */
    private final BufferedReader in;
    
    /** Writer per inviare messaggi al client. */
    private final BufferedWriter out;

    /** ID dell'utente attualmente loggato (-1 se nessuno è loggato). */
    private int loggedUserId = -1;
    
    /** Flag che indica se la sessione è attiva. */
    private boolean active = true;

    /**
     * Costruttore che inizializza il contesto con un socket.
     *
     * @param socket socket della connessione client
     * @throws IOException se si verifica un errore durante la creazione degli stream
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
     * Restituisce l'ID dell'utente attualmente loggato.
     *
     * @return ID dell'utente loggato, oppure -1 se nessuno è loggato
     */
    public int getLoggedUserId() {
        return loggedUserId;
    }

    /**
     * Imposta l'ID dell'utente loggato per questa sessione.
     *
     * @param loggedUserId ID dell'utente loggato
     */
    public void setLoggedUserId(int loggedUserId) {
        this.loggedUserId = loggedUserId;
    }

    /**
     * Verifica se la sessione è attiva.
     *
     * @return true se la sessione è attiva, false altrimenti
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Disattiva la sessione.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Legge una riga di testo dal client.
     * Registra il messaggio nel log.
     *
     * @return la riga letta, oppure null se il client ha chiuso la connessione
     * @throws IOException se si verifica un errore di I/O
     */
    public String read() throws IOException {
        String msg = in.readLine();
        if (msg != null) {
            System.out.println("[Client " + socket.getInetAddress() + " IN] " + msg);
        }
        return msg;
    }

    /**
     * Invia una riga di testo al client.
     * Registra il messaggio nel log.
     *
     * @param msg messaggio da inviare
     * @throws IOException se si verifica un errore di I/O
     */
    public void write(String msg) throws IOException {
        out.write(msg);
        out.write("\n");
        out.flush();
        System.out.println("[Client " + socket.getInetAddress() + " OUT] " + msg);
    }

    /**
     * Chiude la connessione client in modo pulito.
     * Chiude il reader, il writer e il socket.
     */
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
