package com.theknife.app;

import com.theknife.app.Handler.AuthHandler;
import com.theknife.app.Handler.ClientContext;
import com.theknife.app.Handler.CommandHandler;
import com.theknife.app.Handler.DisconnectHandler;
import com.theknife.app.Handler.FavouriteHandler;
import com.theknife.app.Handler.RestaurantHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread dedicato alla gestione di un singolo client connesso al server.
 *
 * <p>Ogni istanza viene avviata alla connessione del client
 * e gestisce comunicazione, comandi e chiusura della sessione.</p>
 *
 * <p>La gestione dei comandi è delegata a una lista ordinata di
 * {@link CommandHandler}, inoltrando i comandi finché uno li riconosce.</p>
 *
 * Lato server ogni client:
 * <ul>
 *     <li>ha un proprio socket</li>
 *     <li>ha un proprio contesto di sessione</li>
 *     <li>viene gestito in un thread dedicato</li>
 * </ul>
 *
 * @author Mattia Sindoni
 * @author Erica Faccio
 * @author Giovanni Isgrò
 */
public class ClientThread extends Thread {

    /** Socket associato alla sessione del client. */
    private final Socket socket;

    /** Contesto per comunicazione e stato sessione. */
    private final ClientContext ctx;

    /** Lista ordinata di handler registrati per l'elaborazione dei comandi. */
    private final List<CommandHandler> handlers = new ArrayList<>();

    private static final int READ_TIMEOUT = 1000;

    private volatile boolean running = true;

    /**
     * Costruisce una nuova istanza del thread client,
     * associandola al socket ricevuto.
     *
     * <p>Inizializza il {@link ClientContext} e registra gli handler,
     * rispettando l’ordine di priorità:</p>
     *
     * <ol>
     *     <li>{@link AuthHandler} → autenticazione</li>
     *     <li>{@link RestaurantHandler} → ristoranti e recensioni</li>
     *     <li>{@link DisconnectHandler} → disconnessione client</li>
     * </ol>
     *
     * <p>Il thread viene avviato automaticamente tramite {@link #start()}.</p>
     *
     * @param socket socket della connessione stabilita dal client
     * @throws IOException se fallisce la creazione del contesto
     */
    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.socket.setSoTimeout(READ_TIMEOUT);
        this.ctx = new ClientContext(socket);

        handlers.add(AuthHandler.getInstance());
        handlers.add(RestaurantHandler.getInstance());
        handlers.add(DisconnectHandler.getInstance());
        handlers.add(FavouriteHandler.getInstance());

        start();
    }

    /**
     * Metodo principale eseguito nel thread dedicato al client.
     *
     * <p>Si occupa esclusivamente di gestire la vita della sessione:</p>
     * <ul>
     *     <li>Segna la connessione a log</li>
     *     <li>Esegue il loop di ricezione comandi</li>
     *     <li>Segna la disconnessione</li>
     *     <li>Chiude connessione e risorse</li>
     * </ul>
     */
    @Override
    public void run() {
        System.out.println("[Client " + socket.getInetAddress() + "] Connected");
        try {
            loop();
        } catch (InterruptedException e) {
            // Thread interrotto esplicitamente
            Thread.currentThread().interrupt(); // Ripristina il flag
        } catch (Exception e) {
            System.out.println("[Client " + socket.getInetAddress() + "] Disconnected - " + e.getClass().getSimpleName());
        } finally {
            try {
                DisconnectHandler.getInstance().handle("handler disconnesso", ctx);
            } catch (Exception ignored) {
            } finally {
                close();
                // Rimuovi questo client dalla lista del server
                ServerApplication.getInstance().removeClient(this);
                System.out.println("[Client " + socket.getInetAddress() + "] Cleaned up");
            }
        }
    }

    /**
     * Loop operativo principale del thread.
     *
     * <p>Funzioni svolte:</p>
     * <ul>
     *     <li>attendere comandi dal client</li>
     *     <li>identificare l’handler corretto</li>
     *     <li>inoltrare il comando all’handler competente</li>
     *     <li>chiudere quando il client interrompe la comunicazione</li>
     * </ul>
     *
     * <p>Se nessun handler riconosce il comando, il server ignora
     * l’input.</p>
     *
     * @throws IOException errori di rete
     * @throws SQLException errori sul database lato handler
     * @throws InterruptedException gestione operazioni concorrenti
     */
    private void loop() throws IOException, SQLException, InterruptedException {
        while(running && ctx.isActive()){
            String cmd = null;
            try{
                cmd = ctx.read();
            }catch(SocketTimeoutException e){
                continue;
            }
            if(cmd == null){
                break;
            }

            System.out.println("[Client " + socket.getInetAddress() + " IN] " + cmd);
            boolean handled = false;
            for(CommandHandler h : handlers){
                if(h.handle(cmd, ctx)){
                    handled = true;
                    break;
                }
            }
            if(!handled){
                ctx.write("unkown_command");
                continue;
            }
        }
    }

    /**
     * Chiude tutte le risorse associate al client:
     * <ul>
     *     <li>socket di rete</li>
     *     <li>streams di lettura/scrittura</li>
     *     <li>stato contestuale</li>
     * </ul>
     *
     * <p>Viene invocato automaticamente quando il thread termina.</p>
     */
    private void close() {
        ctx.close();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
    
    /**
     * Permette l'arresto forzato del thread client
     * <p>
     * Il metodo viene invocato dal server durante la fase di shutdown globale per interrompere in modo ordinato tutte le connessioni attive.
     * </p> 
     * <p>
     * L'operazione imposta il flag {@code running} a {@code false} e chiude il socket associato,
     * causando lo sblocco di eventuali operazioni di lettura bloccanti ({@link ClientContext#read()}).
     * </p>
     * 
     * <p>
     * Dopo l'invocazione, il thread terminerà automaticamente il proprio ciclo di esecuzione
     * </p>
     */
    public void shutdown() {
        running = false;
        // Imposta un timeout molto breve per sbloccare la lettura
        try {
            socket.setSoTimeout(100);
        } catch (IOException ignored) {
        }
        // Interrompi il thread
        this.interrupt();
        close();
    }
}
