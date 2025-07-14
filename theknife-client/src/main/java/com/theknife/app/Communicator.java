package main.java.com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Communicator {
    Socket socket;
    BufferedReader reader;
    OutputStream os;

    public init(String ip, int port) throws UnknownHostException, IOException {
        socket = new Socket(ip, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        os = socket.getOutputStream();
    }

    public String readStream() throws IOException {
        return reader.readLine();
    }

    public void sendStream(String msg) throws IOException {
        os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
    }
}
