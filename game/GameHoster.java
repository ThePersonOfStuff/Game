package game;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import comms.ClientData;

public class GameHoster implements Runnable {
    private boolean running;
    private ArrayList<Player> players;
    private DefaultListModel<ClientData> clients;
    private ArrayList<byte[]> clientBuffers; // stores i, followed by data

    public GameHoster(DefaultListModel<ClientData> clients) {
        this.clients = clients;
        clientBuffers = new ArrayList<>();
        players = new ArrayList<>();
    }

    @Override
    public void run() {
        for (int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).outputStream().write(255);
                clients.get(i).outputStream().write(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < clients.size(); i++) {
            Player player = new Player(i, 50 + 50 * i, 50 + 50 * i);
            //player.sendPositionData(clients.get(i).socket());
            clientBuffers.add(new byte[18]);
            players.add(player);
        }

        // send starting data to clients
        for (int i = 0; i < clients.size(); i++) {
            for (int j = 0; j < players.size(); j++) {
                players.get(j).sendPositionData(clients.get(i).socket());
                try {
                    clients.get(i).outputStream().write(clients.get(j).name().length());
                    clients.get(i).outputStream().write(clients.get(j).name().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        running = true;
        while (running) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // send data to clients
            for (int i = 0; i < clients.size(); i++) {
                for (int j = 0; j < players.size(); j++) {
                    players.get(j).sendPositionData(clients.get(i).socket());
                }
            }

            // recieve data from clients
            for (int i = 0; i < clients.size(); i++) {
                try {
                    while (clients.get(i).socket().getInputStream().available() > 0) {
                        clientBuffers.get(i)[clientBuffers.get(i)[0] + 1] = (byte) clients.get(i).inputStream().read();
                        clientBuffers.get(i)[0]++;
                        if (clientBuffers.get(i)[0] >= 17) {
                            players.get(i).readPositionData(clientBuffers.get(i), 1);
                            clientBuffers.get(i)[0] = 0;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void stop() {
        running = false;
    }
}
