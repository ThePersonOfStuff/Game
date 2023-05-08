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

            clientName = "";

            int byt = -1;
            while(byt != 0) {
                while(byt == -1) {
                    try {
                        byt = inputStream.read();
                    } catch (IOException e) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }

                if(byt != 0) {
                    clientName += (char)byt;
                    byt = -1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientData(Socket s, String name) {
        socket = s;
        try {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();

            clientName = name;
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
