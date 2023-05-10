package game;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Player {
    private int id;
    private double xPos;
    private double yPos;
    private String name;

    public Player(int id, double x, double y) {
        this.id = id;
        xPos = x;
        yPos = y;
    }

    public Player(byte[] initBytes) {
        if(initBytes.length != 17) {
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
        //pattern: id, xPos, yPos
        try {
            socket.getOutputStream().write(id);
            socket.getOutputStream().write(toByteArray(xPos));
            socket.getOutputStream().write(toByteArray(yPos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readPositionData(byte[] data, int offset) {
        for(int i = offset; i <= data.length - 17; i+=17) {
            if(data[i] == id) {
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
}
