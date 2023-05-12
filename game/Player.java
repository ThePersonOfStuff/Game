package game;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Player implements Collidable{
    private int id;
    private double xPos;
    private double yPos;
    private String name;
    private double width = 50;
    private double height = 50;
    private double xVel = 0;
    private double yVel = 0;
    private int framesSinceLastTouchedGround = 100;
    private int framesSinceLastTouchedLeft = 100;
    private int framesSinceLastTouchedRight = 100;
    private HashMap<String, Boolean> lastKeysPressed;
    private HashMap<String, Integer> framesSinceLastKeysPressed;

    public Player(int id, double x, double y) {
        this.id = id;
        xPos = x;
        yPos = y;
    }

    public Player(byte[] initBytes) {
        if (initBytes.length != 17) {
            throw new IllegalArgumentException();
        }
        id = initBytes[0];
        xPos = toDouble(initBytes, 1);
        yPos = toDouble(initBytes, 9);

        lastKeysPressed = new HashMap<>();
        framesSinceLastKeysPressed = new HashMap<>();
    }

    private static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    private static double toDouble(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 8).getDouble();
    }

    public void sendPositionData(Socket socket) {
        // pattern: id, xPos, yPos
        try {
            socket.getOutputStream().write(id);
            socket.getOutputStream().write(toByteArray(xPos));
            socket.getOutputStream().write(toByteArray(yPos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readPositionData(byte[] data, int offset) {
        for (int i = offset; i <= data.length - 17; i += 17) {
            if (data[i] == id) {
                xPos = toDouble(data, i + 1);
                yPos = toDouble(data, i + 9);
            }
        }
    }

    public void readPositionData(byte[] data) {
        readPositionData(data, 0);
    }

    public double getX() {
        return xPos;
    }

    public double getY() {
        return yPos;
    }

    public void setX(double x) {
        xPos = x;
    }

    public void setY(double y) {
        yPos = y;
    }

    public int getID() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void draw(Graphics g, int x, int y) {
        g.setColor(Color.ORANGE);
        g.fillRect(x - (int) width / 2, y - (int) height / 2, (int) width, (int) height);
        g.setColor(Color.BLACK);
        drawCenteredString(g, name, x, y);
    }

    public void draw(Graphics g) {
        draw(g, (int) xPos, (int) yPos);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    private void drawCenteredString(Graphics g, String text, int xPos, int yPos) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int x = xPos - metrics.stringWidth(text) / 2;
        int y = yPos - metrics.getHeight() / 2 + metrics.getAscent();

        g.drawString(text, x, y);
    }

    public void move(Level level, HashMap<String, Boolean> keysPressed) {
        byte collisions = level.collisions(this);

        framesSinceLastTouchedGround++;
        framesSinceLastTouchedLeft++;
        framesSinceLastTouchedRight++;

        if((collisions & Level.DOWN_BITMASK) != 0) {
            framesSinceLastTouchedGround = 0;
        }
        if((collisions & Level.LEFT_BITMASK) != 0) {
            framesSinceLastTouchedLeft = 0;
        }
        if((collisions & Level.RIGHT_BITMASK) != 0) {
            framesSinceLastTouchedRight = 0;
        }
        
        boolean jump = false;
        if(keysPressed.get("JUMP") && framesSinceLastKeysPressed.getOrDefault("JUMP", 100) < 6) {
            jump = true;
        }

        if (keysPressed.get("LEFT")) {
            if (framesSinceLastTouchedGround < 5) {
                xVel -= 9;
            } else {
                xVel -= 2.5;
            }
        }
        if (keysPressed.get("RIGHT")) {
            if (framesSinceLastTouchedGround < 5) {
                xVel += 9;
            } else {
                xVel += 2.5;
            }
        }
        if (jump) {
            if (framesSinceLastTouchedGround < 5) {
                yVel = -30;
            } else if (framesSinceLastTouchedLeft < 5) {
                yVel = -20;
                xVel = 10;
            } else if (framesSinceLastTouchedRight < 5) {
                yVel = -20;
                xVel = -10;
            }
        }
        if (keysPressed.get("FALL")) {
            yVel += 10;
        }

        // more friction if touching ground
        xVel *= (collisions & Level.DOWN_BITMASK) != 0 ? 0.6 : 0.8;
        // more y friction if pushing against wall
        yVel *= (collisions & (Level.LEFT_BITMASK | Level.RIGHT_BITMASK)) != 0 ? 0.25 : 0.9;

        // gravity!
        yVel += 2;

        // if colliding with ground, eliminate all positive y velocity and snap up to
        // tile
        if ((collisions & Level.DOWN_BITMASK) != 0) {
            yVel = yVel > 0 ? 0 : yVel;
            level.snapUp(this);
        }

        // if colliding with up, eliminate all negative y velocity and snap down to tile
        if ((collisions & Level.UP_BITMASK) != 0) {
            yVel = yVel < 0 ? 0 : yVel;
            level.snapDown(this);
        }

        // if colliding with right, eliminate all positive x velocity and snap up to
        // tile
        if ((collisions & Level.RIGHT_BITMASK) != 0) {
            xVel = xVel > 0 ? 0 : xVel;
            level.snapLeft(this);
        }

        // if colliding with left, eliminate all negative x velocity and snap up to tile
        if ((collisions & Level.LEFT_BITMASK) != 0) {
            xVel = xVel < 0 ? 0 : xVel;
            level.snapRight(this);
        }

        xPos += xVel;
        yPos += yVel;

        for(Map.Entry<String, Boolean> entry : keysPressed.entrySet()) {
            if(!lastKeysPressed.getOrDefault(entry.getKey(), false) && entry.getValue()) {
                framesSinceLastKeysPressed.put(entry.getKey(), 0);
            }
            lastKeysPressed.put(entry.getKey(), entry.getValue());
        }

        for(String key : keysPressed.keySet()) {
            framesSinceLastKeysPressed.put(key, framesSinceLastKeysPressed.getOrDefault(key, 100) + 1);
        }
    }
}