package game;

import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;

public class GameState {
    private Socket hostSocket;

    private HashMap<Integer, Player> players;
    private HashMap<Integer, Item> items;
    private Player me;

    private byte[] inputBuffer;
    private int inputBufferPos;

    private Level level;

    public GameState(Socket host) {
        hostSocket = host;
        try {
            FileInputStream f = new FileInputStream(new File("game/levels/level1.level"));
            ObjectInputStream o = new ObjectInputStream(f);

            level = (Level) o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // the host will send you your id to start, then it will start the player
        // sending loop in the pattern [list], [list], etc
        // the first time it sends a player info it will also send the name (names must
        // be less than 255 characters)
        try {
            int myID = host.getInputStream().read();
            byte[] playerData = new byte[17];
            players = new HashMap<>();
            // wait for duplicate players before finishing init
            while (true) {
                for (int i = 0; i < playerData.length;) {
                    if (host.getInputStream().available() > 0) {
                        playerData[i] = (byte) host.getInputStream().read();
                        i++;
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (players.containsKey(Integer.valueOf(playerData[0]))) {
                    players.get(Integer.valueOf(playerData[0])).readPositionData(playerData);
                    break;
                }

                Player player = new Player(playerData);
                if (playerData[0] == myID) {
                    me = player;
                }
                byte[] name = new byte[host.getInputStream().read()];
                host.getInputStream().read(name);
                System.out.println(new String(name));
                player.setName(new String(name));
                players.put(Integer.valueOf(playerData[0]), player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputBuffer = new byte[18];
        inputBufferPos = 0;
        items = new HashMap<>();
    }

    public void updatePositions(HashMap<String, Boolean> keysPressed) {
        // move own player
        me.move(level, keysPressed);
        try {
            // send own position
            me.sendPositionData(hostSocket);

            // read all position updates besides self
            while (hostSocket.getInputStream().available() > 0) {
                inputBuffer[inputBufferPos] = (byte) hostSocket.getInputStream().read();
                inputBufferPos++;
                if(inputBufferPos >= 17 && players.containsKey(Integer.valueOf(inputBuffer[0]))) {
                    players.get(Integer.valueOf(inputBuffer[0])).readPositionData(inputBuffer);
                    
                    inputBufferPos = 0;
                }
                else if (inputBufferPos >= 18) {
                    //It's iteming time
                    if(items.containsKey(Integer.valueOf(inputBuffer[0]))) {
                        if(inputBuffer[1] == -1) {
                            items.remove(Integer.valueOf(inputBuffer[0]));
                        } else {
                            items.get(Integer.valueOf(inputBuffer[0])).readPositionData(inputBuffer);
                        }
                    } else {
                        System.out.println("New item!");
                        items.put(Integer.valueOf(inputBuffer[0]), Item.newItem(inputBuffer));
                    }
                    inputBufferPos = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g, int width, int height) {
        g.translate(-(int) me.getX() + width / 2, -(int) me.getY() + height / 2);
        level.draw(g, 0, 0);
        for (Player player : players.values()) {
            player.draw(g);
        }
        for(Item item : items.values()) {
            item.draw(g);
        }
    }
}
