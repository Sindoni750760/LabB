package com.theknife.app;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.theknife.app.Handler.AuthHandler;
import com.theknife.app.Handler.ClientContext;
import com.theknife.app.Handler.CommandHandler;
import com.theknife.app.Handler.DisconnectHandler;
import com.theknife.app.Handler.PingHandler;
import com.theknife.app.Handler.RestaurantHandler;

public class ClientThread extends Thread {

    private final Socket socket;
    private final ClientContext ctx;
    private final List<CommandHandler> handlers = new ArrayList<>();

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.ctx = new ClientContext(socket);

        // Registra gli handler (tutti singleton dove previsto)
        handlers.add(new PingHandler());
        handlers.add(new AuthHandler());
        handlers.add(RestaurantHandler.getInstance());
        handlers.add(new DisconnectHandler());

        start();
    }


    @Override
    public void run() {
        System.out.println("[Client " + socket.getInetAddress() + "] Connected");

        try {
            loop();
        } catch (Exception e) {
            System.out.println("[Client " + socket.getInetAddress() + "] Error: " + e.getMessage());
        } finally {
            ctx.close();
            System.out.println("[Client " + socket.getInetAddress() + "] Disconnected");
        }
    }

    private void loop() throws IOException, SQLException, InterruptedException {

        while (ctx.isActive()) {

            String cmd = ctx.read();
            if (cmd == null) {
                break; // client ha chiuso
            }

            boolean handled = false;

            for (CommandHandler h : handlers) {
                if (h.handle(cmd, ctx)) {
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                ctx.write("unknown");
            }
        }
    }
}