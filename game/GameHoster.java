package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import comms.ClientData;

public class GameHoster implements Runnable {
    private boolean running;
    private ArrayList<Player> players;
    private DefaultListModel<ClientData> clients;
    private ArrayList<byte[]> clientBuffers; // stores i, followed by data
    private ArrayList<Item> items;
    private int framesPassed = 0;
    private Level level;

    public GameHoster(DefaultListModel<ClientData> clients) {
        this.clients = clients;
        clientBuffers = new ArrayList<>();
        players = new ArrayList<>();
        items = new ArrayList<>();

        try {
            FileInputStream f = new FileInputStream(new File("game/levels/level1.level"));
            ObjectInputStream o = new ObjectInputStream(f);

            level = (Level) o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
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

            framesPassed++;

            for(int i = 0; i < items.size(); i++) {
                items.get(i).move(level);
                for(Player player : players) {
                    if(player.collidesWith(items.get(i))) {
                        for(int j = 0; j < clients.size(); j++) {
                            items.get(i).sendRemoval(clients.get(j).socket());
                        }
                        items.remove(i);
                        i--;
                    }
                }
            }

            if(framesPassed%100 == 1 && items.size() < players.size()) {
                //summon a new item at a random position
                items.add(new Sword(players.size() + items.size(), Math.random()*level.getWidth(), 50));
            }

            // send data to clients
            for (int i = 0; i < clients.size(); i++) {
                for (int j = 0; j < players.size(); j++) {
                    if(j != i || framesPassed == 1) {
                        players.get(j).sendPositionData(clients.get(i).socket());
                    }
                }
                for(int j = 0; j < items.size(); j++) {
                    items.get(j).sendPositionData(clients.get(i).socket());
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