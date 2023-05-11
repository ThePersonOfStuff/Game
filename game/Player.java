package game;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Player {
    private int id;
    private double xPos;
    private double yPos;
    private String name;
    private double width = 50;
    private double height = 50;
    private double xVel = 0;
    private double yVel = 0;
    private int framesSinceLastTouchedGround = -1;
    private int framesSinceLastTouchedLeft = -1;
    private int framesSinceLastTouchedRight = -1;

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
        

        if (keysPressed.get("LEFT")) {
            if (framesSinceLastTouchedGround >= 0 && framesSinceLastTouchedGround < 5) {
                xVel -= 20;
            } else {
                xVel -= 5;
            }
        }
        if (keysPressed.get("RIGHT")) {
            if (framesSinceLastTouchedGround >= 0 && framesSinceLastTouchedGround < 5) {
                xVel += 20;
            } else {
                xVel += 5;
            }
        }
        if (keysPressed.get("JUMP")) {
            if (framesSinceLastTouchedGround >= 0 && framesSinceLastTouchedGround < 5) {
                yVel = -50;
            } else if (framesSinceLastTouchedLeft >= 0 && framesSinceLastTouchedLeft < 5) {
                yVel = -40;
                xVel = 20;
            } else if (framesSinceLastTouchedRight >= 0 && framesSinceLastTouchedRight < 5) {
                yVel = -40;
                xVel = -20;
            }
        }
        if (keysPressed.get("FALL")) {
            yVel += 10;
        }

        // more friction if touching ground
        xVel *= (collisions & Level.DOWN_BITMASK) != 0 ? 0.5 : 0.9;
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
    }
}
