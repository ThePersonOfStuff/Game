package comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.DefaultListModel;

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

            boolean nameFound = false;
            while(!nameFound) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("Waiting for name");
                }

                while(inputStream.available() > 0) {
                    int byt = inputStream.read();
                    if(byt == 0) {
                        nameFound = true;
                        break;
                    } else {
                        clientName += (char)byt;
                    }
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

    public void sendNames(DefaultListModel<ClientData> otherClients) {
        for(int i = 0; i < otherClients.size(); i++) {
            String clientName = otherClients.get(i).name();
            try {
                outputStream.write(clientName.getBytes(StandardCharsets.UTF_8));
                outputStream.write(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void leave() {
        try {
            outputStream.write(clientName.getBytes(StandardCharsets.UTF_8));
            outputStream.write(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
