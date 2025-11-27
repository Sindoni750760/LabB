package com.theknife.app;

import com.theknife.app.Handler.AuthHandler;
import com.theknife.app.Handler.ClientContext;
import com.theknife.app.Handler.CommandHandler;
import com.theknife.app.Handler.DisconnectHandler;
import com.theknife.app.Handler.RestaurantHandler;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread dedicato alla gestione di un singolo client connesso al server.
 * Legge i comandi dal client e li instrada ai rispettivi handler per l'elaborazione.
 * Ogni connessione client viene gestita da una istanza di questa classe eseguita in un thread separato.
 * 
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class ClientThread extends Thread {

    /** Socket della connessione client. */
    private final Socket socket;
    
    /** Contesto della sessione client, gestisce lettura/scrittura. */
    private final ClientContext ctx;
    
    /** Lista degli handler che elaborano i comandi del client. */
    private final List<CommandHandler> handlers = new ArrayList<>();

    /**
     * Costruttore che inizializza il thread client.
     * Registra gli handler nel corretto ordine di precedenza.
     *
     * @param socket socket della connessione client
     * @throws IOException se si verifica un errore durante la creazione del contesto
     */
    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.ctx = new ClientContext(socket);

        // Registro gli handler in ordine
        handlers.add(AuthHandler.getInstance());
        handlers.add(RestaurantHandler.getInstance());
        handlers.add(DisconnectHandler.getInstance());

        start();
    }

    /**
     * Esecuzione principale del thread client.
     * Legge i comandi dal client e li elabora tramite gli handler.
     */
    @Override
    public void run() {
        System.out.println("[Client " + socket.getInetAddress() + "] Connected");
        try {
            loop();
        } catch (Exception e) {
            System.out.println("[Client " + socket.getInetAddress() + "] Disconnected");
        } finally {
            close();
        }
    }

    /**
     * Loop principale che legge e elabora i comandi del client.
     * Legge una riga di comando dal client e la instrada agli handler in ordine.
     *
     * @throws IOException se si verifica un errore di I/O
     * @throws SQLException se si verifica un errore di database
     * @throws InterruptedException se il thread viene interrotto
     */
    private void loop() throws IOException, SQLException, InterruptedException {
        while (true) {
            String cmd = ctx.read();
            if (cmd == null) break; // client chiuso

            System.out.println("[Client " + socket.getInetAddress() + " IN] " + cmd);

            boolean handled = false;
            for (CommandHandler h : handlers) {
                if (h.handle(cmd, ctx)) {
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                // comando sconosciuto, opzionale:
                // ctx.write("error");
            }
        }
    }

    /**
     * Chiude la connessione con il client in modo pulito.
     * Chiude il contesto e il socket.
     */
    private void close() {
        ctx.close();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
