package game;

import java.awt.Graphics;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;


public class GameState {
    private Socket hostSocket;

    private HashMap<Integer, Player> players;
    private Player me;

    private byte[] inputBuffer;
    private int inputBufferPos;

    private Level level = new Level();

    public GameState(Socket host) {
        hostSocket = host;

        // the host will send you your id to start, then it will start the player
        // sending loop in the pattern [list], [list], etc
        //the first time it sends a player info it will also send the name (names must be less than 255 characters)
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

        inputBuffer = new byte[17];
        inputBufferPos = 0;
    }

    public void updatePositions(HashMap<String, Boolean> keysPressed) {
        //move own player
        me.move(level, keysPressed);
        try {
            // send own position
            me.sendPositionData(hostSocket);

            // read all position updates besides self
            while (hostSocket.getInputStream().available() > 0) {
                inputBuffer[inputBufferPos] = (byte) hostSocket.getInputStream().read();
                inputBufferPos++;
                if (inputBufferPos >= 17) {
                    if (inputBuffer[0] != me.getID()) {
                        if (players.get(Integer.valueOf(inputBuffer[0])) == null) {
                            System.out.println("New player id: " + inputBuffer[0]);
                            Player player = new Player(inputBuffer);
                            players.put(Integer.valueOf(inputBuffer[0]), player);
                        } else {
                            players.get(Integer.valueOf(inputBuffer[0])).readPositionData(inputBuffer);
                        }
                    }
                    inputBufferPos = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g, int width, int height) {
        g.translate(-(int)me.getX() + width / 2, -(int)me.getY() + height/2);
        level.draw(g, 0, 0);
        for (Player player : players.values()) {
            player.draw(g);
        }
        g.translate((int)me.getX(), (int)me.getY());
    }
}
