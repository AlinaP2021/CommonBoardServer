package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {

    private final Socket socket;
    private final ObjectOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    public void send(String message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public void close() throws IOException {
        out.close();
        socket.close();
    }
}
