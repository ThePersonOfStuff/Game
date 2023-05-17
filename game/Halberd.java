package game;

import java.awt.Graphics;

public class Halberd extends Item {
    public Halberd(int id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(images.get("halberd"), (int)(getX() - getWidth() / 2), (int)(getY() - getHeight() / 2), null);
    }

    @Override
    public double getWidth() {
        return images.get("halberd").getWidth();
    }

    @Override
    public double getHeight() {
        return images.get("halberd").getHeight();
    }

    @Override
    public byte getType() {
        return 2;
    }

    @Override
    public int getLevelID() {
        return 3;
    }

}
