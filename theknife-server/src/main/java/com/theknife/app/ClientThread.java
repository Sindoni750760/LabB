package main.java.com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {
    Socket socket;
    String ip;
    BufferedReader reader;
    OutputStream os;

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        ip = socket.getInetAddress().toString();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        os = socket.getOutputStream();
        start();
    }

    public void run() {
        try {
            exec();
        } catch(InterruptedException | IOException e) {
            if(e.getMessage().equals("Connection reset"))
                log("Disconnected");
            else
                e.printStackTrace();
        }
    }

    private void log(String msg) {
        System.out.println("[" + ip + "] " + msg);
    }

    private String readStream() throws IOException {
        return reader.readLine();
    }

    private void sendStream(String msg) throws IOException {
        os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
    }

    private void exec() throws InterruptedException, IOException {
        log("Connected");

        String cmd = readStream();
        while(!cmd.equals("disconnect")) {
            log("Received: " + cmd);
            switch (cmd) {
                case "ping":
                    sendStream("pong");
                    break;
                default:
                    sendStream("Unknown command");
                    break;
            }
            cmd = readStream();
        }
    }
}