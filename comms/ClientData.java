package comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientData {
    private Socket socket;
    private String clientName;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ClientData(Socket s) {
        socket = s;
        try {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();

            clientName = socket.getInetAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket socket() {
        return socket;
    }

    public String name() {
        return clientName;
    }

    public InputStream inputStream() {
        return inputStream;
    }

    public OutputStream outputStream() {
        return outputStream;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ClientData)) {
            return false;
        }
        ClientData otherClient = (ClientData)other;
        return socket.equals(otherClient.socket()) && clientName.equals(otherClient.name());
    }

    public void setName(String n) {
        clientName = n;
    }
}
