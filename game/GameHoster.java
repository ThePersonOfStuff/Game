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
    private ArrayList<Integer> takenIds;
    private int framesPassed = 0;
    private int swordsCount = 0;
    private int knivesCount = 0;
    private Level[] levels;

    public GameHoster(DefaultListModel<ClientData> clients) {
        this.clients = clients;
        clientBuffers = new ArrayList<>();
        players = new ArrayList<>();
        items = new ArrayList<>();
        takenIds = new ArrayList<>();

        try {
            File levelFolder = new File("game/levels");
            File[] levelFiles = levelFolder.listFiles();
            levels = new Level[levelFiles.length];
            for (int i = 0; i < levelFiles.length; i++) {
                FileInputStream f = new FileInputStream(new File("game/levels/level" + (i + 1) + ".level"));
                ObjectInputStream o = new ObjectInputStream(f);
                levels[i] = (Level) o.readObject();
            }
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
            clientBuffers.add(new byte[19]);
            players.add(player);
            takenIds.add(i);
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

            // Item collision detection
            itemloop: for (int i = 0; i < items.size(); i++) {
                items.get(i).move(levels[items.get(i).getLevelID() - 1]);
                for (Player player : players) {
                    if (player.collidesWith(items.get(i)) && !player.hasCollected()) {
                        player.collect();
                        if (items.get(i).getOwner() != null) {
                            items.get(i).getOwner().resetCollected();
                        }
                        items.get(i).setOwner(player);
                        continue itemloop;
                    }
                }
            }

            // remove items whos owner has trancended
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getOwner() != null
                        && items.get(i).getOwner().getLevelID() != items.get(i).getLevelID()) {
                    for (int j = 0; j < clients.size(); j++) {
                        items.get(i).sendRemoval(clients.get(j).socket());
                    }
                    takenIds.remove(Integer.valueOf(items.get(i).getID()));
                    items.remove(i);
                    i--;
                }
            }

            if (framesPassed % 100 == 0 && swordsCount < players.size()) {
                // summon a new sword at a random position
                items.add(new Sword(getNewID(), (Math.random() * (levels[0].getWidth() - 200) + 100), 50));
                swordsCount++;
            }

            if (framesPassed % 100 == 0 && knivesCount < players.size()) {
                // summon a new knife at upper right corner
                System.out.println("New knife!");
                items.add(new Knife(getNewID(), levels[1].getWidth() - 50 - Math.random() * 150, 50));
                knivesCount++;
            }

            // detect player win/lose
            for (Player player : players) {
                byte collisions = levels[player.getLevelID() - 1].getNonTileCollisions(player);
                if ((collisions & Level.LAVA_BITMASK) != 0) {
                    // send player back to beginning
                    player.setX(50);
                    player.setY(50);
                    for (int i = 0; i < clients.size(); i++) {
                        player.sendPositionData(clients.get(i).socket());
                    }
                } else if ((collisions & Level.WIN_BITMASK) != 0 && player.hasCollected()) {
                    System.out.println("E");
                    // send player back to beginning and increment level
                    player.setX(50);
                    player.setY(50);
                    player.setLevelID(player.getLevelID() + 1);
                    player.resetCollected();
                    for (int i = 0; i < clients.size(); i++) {
                        player.sendPositionData(clients.get(i).socket());
                    }
                }
            }

            // send data to clients
            for (int i = 0; i < clients.size(); i++) {
                for (int j = 0; j < players.size(); j++) {
                    if (j != i || framesPassed == 1) {
                        players.get(j).sendPositionData(clients.get(i).socket());
                    }
                }
                for (int j = 0; j < items.size(); j++) {
                    items.get(j).sendPositionData(clients.get(i).socket());
                }
            }

            // recieve data from clients
            for (int i = 0; i < clients.size(); i++) {
                try {
                    while (clients.get(i).socket().getInputStream().available() > 0) {
                        clientBuffers.get(i)[clientBuffers.get(i)[0] + 1] = (byte) clients.get(i).inputStream().read();
                        clientBuffers.get(i)[0]++;
                        if (clientBuffers.get(i)[0] >= 18) {
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

    private int getNewID() {
        int id = 0;
        while (takenIds.contains(id)) {
            id++;
        }
        takenIds.add(id);

        return id;
    }
}
