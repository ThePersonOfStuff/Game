package game;

import java.awt.Graphics;

public class Sword extends Item {
    public Sword(int id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(images.get("sword"), (int)(getX() - getWidth()/2), (int)(getY() - getHeight()/2), null);
    }

    @Override
    public byte getType() {
        return 0;
    }

    @Override
    public double getWidth() {
        return images.get("sword").getWidth();
    }

    @Override
    public double getHeight() {
        return images.get("sword").getHeight();
    }
    
    @Override
    public int getLevelID() {
        return 1;
    }
}
