package game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.imageio.ImageIO;

public abstract class Item implements Serializable, Collidable {
    public static HashMap<String, BufferedImage> images;
    private static final long serialVersionUID = 48768768L;
    private double xPos;
    private double yPos;
    private double xVel = 0;
    private double yVel = 0;
    private int id;
    private Player owner = null;

    static {
        images = new HashMap<>();
        try {
            images.put("sword", ImageIO.read(new File("game/Images/Sword.png")));
            images.put("player", ImageIO.read(new File("game/Images/Player.png")));
            images.put("knives", ImageIO.read(new File("game/Images/Knives.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Item(int id, double x, double y) {
        this.id = id;
        xPos = x;
        yPos = y;
    }

    public abstract void draw(Graphics g);

    public abstract double getWidth();

    public abstract double getHeight();

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

    public abstract byte getType();

    @Override
    public abstract int getLevelID();

    public void sendPositionData(Socket host) {
        try {
            host.getOutputStream().write(id);
            host.getOutputStream().write(getType());
            host.getOutputStream().write(toByteArray(xPos));
            host.getOutputStream().write(toByteArray(yPos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRemoval(Socket host) {
        try {
            host.getOutputStream().write(id);
            host.getOutputStream().write(-1);
            host.getOutputStream().write(toByteArray(xPos));
            host.getOutputStream().write(toByteArray(yPos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readPositionData(byte[] data, int offset) {
        for (int i = offset; i <= data.length - 18; i += 18) {
            if (data[i] == id) {
                xPos = toDouble(data, i + 2);
                yPos = toDouble(data, i + 10);
            }
        }
    }

    public void readPositionData(byte[] data) {
        readPositionData(data, 0);
    }

    private static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    private static double toDouble(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 8).getDouble();
    }

    public static Item newItem(byte[] data, int offset) {
        int id = data[offset];
        Constructor<? extends Item> itemConstructor = null;
        try {
            switch (data[offset + 1]) {
                case 0 -> itemConstructor = Sword.class.getDeclaredConstructor(int.class, double.class, double.class);
                case 1 -> itemConstructor = Knife.class.getDeclaredConstructor(int.class, double.class, double.class);
                default -> throw new IllegalArgumentException("That item type does not exist");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        double xPos = toDouble(data, offset + 2);
        double yPos = toDouble(data, offset + 10);

        Item item = null;
        try {
            item = itemConstructor.newInstance(id, xPos, yPos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public static Item newItem(byte[] data) {
        return newItem(data, 0);
    }

    public void move(Level level) {
        if (owner == null) {
            yVel += 1;

            byte collisions = level.collisions(this);
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
        } else {
            //lerp bottom part of item to top of player
            double goalX = owner.getX();
            double goalY = owner.getY() - getHeight() / 2 - owner.getHeight()/2 - 10;
            xPos = xPos * 0.6 + goalX * 0.4;
            yPos = yPos * 0.6 + goalY * 0.4;
        }
    }

    public void setOwner(Player player) {
        owner = player;
    }

    public Player getOwner() {
        return owner;
    }
}
