package game;

import java.awt.Graphics;

public class Knife extends Item {
    public Knife(int id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(images.get("knives"), (int)(getX() - getWidth()/2), (int)(getY() - getHeight()/2), null);
    }

    @Override
    public double getWidth() {
        return images.get("knives").getWidth();
    }

    @Override
    public double getHeight() {
        return images.get("knives").getHeight();
    }

    @Override
    public byte getType() {
        return 1;
    }

    @Override
    public int getLevelID() {
        return 2;
    }
}
