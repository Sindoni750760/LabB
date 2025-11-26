package com.theknife.app.Handler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientContext {

    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    private int loggedUserId = -1;
    private boolean active = true;

    public ClientContext(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );
        this.out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
        );
    }

    public int getLoggedUserId() {
        return loggedUserId;
    }

    public void setLoggedUserId(int loggedUserId) {
        this.loggedUserId = loggedUserId;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public String read() throws IOException {
        String msg = in.readLine();
        if (msg != null) {
            System.out.println("[Client " + socket.getInetAddress() + " IN] " + msg);
        }
        return msg;
    }

    public void write(String msg) throws IOException {
        out.write(msg);
        out.write("\n");
        out.flush();
        System.out.println("[Client " + socket.getInetAddress() + " OUT] " + msg);
    }

    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
