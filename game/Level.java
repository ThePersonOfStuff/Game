package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Level implements Serializable {
    private static final long serialVersionUID = 19283746L;
    private byte[][] tileMap;
    public static final byte UP_BITMASK = 0b1000000;
    public static final byte DOWN_BITMASK = 0b0100000;
    public static final byte LEFT_BITMASK = 0b0010000;
    public static final byte RIGHT_BITMASK = 0b0001000;
    public static final byte LAVA_BITMASK = 0b0000100;
    public static final byte WIN_BITMASK = 0b0000010;
    private static int tileSize = 25;
    private String instructionText;

    /*
     * tiles are 10x10 pixels each
     * MAP:
     * 0 - empty
     * 1 - wall
     */

    public Level() {
        this(new byte[100][100]);
    }

    public Level(byte[][] tiles) {
        this(tiles, "");
    }

    public Level(String instrs) {
        this(new byte[100][100], instrs);
    }

    public Level(byte[][] tiles, String instrs) {
        tileMap = tiles;
        instructionText = instrs;
    }

    public static void main(String[] args) {
        // level editor yay
        Level level = new Level();
        if (args.length < 1) {
            level = new Level(new byte[25][100]);
        } else {
            try {
                FileInputStream f = new FileInputStream(new File("game/levels/" + args[0]));
                ObjectInputStream o = new ObjectInputStream(f);

                level = (Level) o.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        JFrame frame = new JFrame();
        LevelEditor editor = new LevelEditor(level);
        frame.add(editor);
        frame.setVisible((true));
        frame.setSize(800, 600);
        frame.addKeyListener(editor);
        // frame.addMouseListener(editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static class LevelEditor extends JPanel implements MouseListener, KeyListener {
        private Level level;
        private int xPos;
        private int yPos;

        public LevelEditor(Level level) {
            this.level = level;
            xPos = -tileSize * level.tileMap[0].length / 2;
            yPos = -tileSize * level.tileMap.length / 2;
            addMouseListener(this);
        }

        @Override
        public void paintComponent(Graphics g) {
            g.fillRect(0, 0, getWidth(), getHeight());
            level.draw(g, xPos, yPos);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            return;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'a' -> xPos += 5;
                case 'd' -> xPos -= 5;
                case 's' -> yPos -= 5;
                case 'w' -> yPos += 5;
                case 'p' -> {
                    // save to file
                    try {
                        FileOutputStream f = new FileOutputStream(new File("game/levels/testing.level"));
                        ObjectOutputStream o = new ObjectOutputStream(f);

                        o.writeObject(level);

                        o.close();
                        f.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            return;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int tileX = (e.getX() - xPos) / tileSize;
            int tileY = (e.getY() - yPos) / tileSize;
            if (e.getButton() == MouseEvent.BUTTON1) {
                level.tileMap[tileY][tileX] += 1;
            } else {
                level.tileMap[tileY][tileX] -= 1;
            }

            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            return;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            return;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            return;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            return;
        }
    }

    public byte getNonTileCollisions(Collidable collider) {
        int xFirstOther = (int) (collider.getX() - collider.getWidth() / 2) / tileSize;
        int xLastOther = (int) Math.ceil((collider.getX() + collider.getWidth() / 2) / tileSize) - 1;
        int yFirstOther = (int) (collider.getY() - collider.getHeight() / 2) / tileSize;
        int yLastOther = (int) Math.ceil((collider.getY() + collider.getHeight() / 2) / tileSize) - 1;

        byte collisions = 0;

        for (int x = xFirstOther; x <= xLastOther; x++) {
            for (int y = yFirstOther; y <= yLastOther; y++) {
                try {
                    if (tileMap[y][x] == 2) {
                        collisions |= LAVA_BITMASK;
                    }
                    if (tileMap[y][x] == 3) {
                        collisions |= WIN_BITMASK;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        return collisions;
    }

    /**
     * Returns the sides, if any, that the player is colliding with.
     * 
     * @param collider - the player to check collisions with
     * @return the bitmask
     */
    public byte collisions(Collidable collider) {
        byte collisions = 0;

        int xFirstOther = (int) (collider.getX() - collider.getWidth() / 2) / tileSize;
        int xLastOther = (int) Math.ceil((collider.getX() + collider.getWidth() / 2) / tileSize) - 1;
        int yFirstOther = (int) (collider.getY() - collider.getHeight() / 2) / tileSize;
        int yLastOther = (int) Math.ceil((collider.getY() + collider.getHeight() / 2) / tileSize) - 1;

        double dXFirst = (collider.getX() - collider.getWidth() / 2);
        double dYFirst = (collider.getY() - collider.getHeight() / 2);
        double dXLast = (collider.getX() + collider.getWidth() / 2);
        double dYLast = (collider.getY() + collider.getHeight() / 2);

        for (int x = xFirstOther; x <= xLastOther; x++) {
            for (int y = yFirstOther; y <= yLastOther; y++) {
                try {
                    if (tileMap[y][x] == 2) {
                        collisions |= LAVA_BITMASK;
                    }
                    if (tileMap[y][x] == 3) {
                        collisions |= WIN_BITMASK;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        // check for collisions in lower tiles
        if (dYLast / tileSize >= tileMap.length) {
            collisions |= DOWN_BITMASK;
        } else if (dYLast / tileSize >= 0) {
            for (int i = (int) (dXFirst / tileSize); i < dXLast / tileSize; i++) {
                if (i < 0 || i >= tileMap[0].length) {
                    continue;
                }
                if (tileMap[(int) (dYLast / tileSize)][i] == 1) {
                    collisions |= DOWN_BITMASK;
                    yLastOther -= 1;
                    dYLast = Math.floor(dYLast / tileSize) * tileSize;
                    break;
                }
            }
        }

        // check for collisions in upper tiles
        if ((int) Math.ceil(dYFirst / tileSize - 1) < 0) {
            collisions |= UP_BITMASK;
        } else if ((int) Math.ceil(dYFirst / tileSize - 1) < tileMap.length) {
            for (int i = (int) (dXFirst / tileSize); i < dXLast / tileSize; i++) {
                if (i < 0 || i >= tileMap[0].length) {
                    continue;
                }
                if (tileMap[(int) Math.ceil(dYFirst / tileSize - 1)][i] == 1) {
                    collisions |= UP_BITMASK;
                    yFirstOther += 1;
                    break;
                }
            }
        }

        // check for collisions in right tiles
        if (dXLast / tileSize >= tileMap[0].length) {
            collisions |= RIGHT_BITMASK;
        } else if (dXLast / tileSize >= 0) {
            for (int i = (int) (dYFirst / tileSize); i < dYLast / tileSize; i++) {
                if (i < 0 || i >= tileMap.length) {
                    continue;
                }
                if (tileMap[i][(int) (dXLast / tileSize)] == 1) {
                    collisions |= RIGHT_BITMASK;
                    break;
                }
            }
        }

        // check for collisions in left tiles
        if ((int) Math.ceil(dXFirst / tileSize - 1) < 0) {
            collisions |= LEFT_BITMASK;
        } else if ((int) Math.ceil(dXFirst / tileSize - 1) < tileMap[0].length) {
            for (int i = (int) (dYFirst / tileSize); i < dYLast / tileSize; i++) {
                if (i < 0 || i >= tileMap.length) {
                    continue;
                }
                if (tileMap[i][(int) Math.ceil(dXFirst / tileSize - 1)] == 1) {
                    collisions |= LEFT_BITMASK;
                    break;
                }
            }
        }

        return collisions;
    }

    /**
     * Draws the level at the specified scale
     * 
     * @param g - the graphics object to draw to
     * @param x - the x coordinate of the upper left corner
     * @param y - the y coordinate of the upper left corner
     */
    public void draw(Graphics g, int x, int y) {
        int size = 4;

        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                switch (tileMap[i][j]) {
                    case 0 -> g.setColor(Color.WHITE);
                    case 1 -> g.setColor(Color.BLACK);
                    case 2 -> g.setColor(Color.RED);
                    case 3 -> g.setColor(Color.GREEN);
                    default -> g.setColor(Color.PINK);
                }

                g.fillRect(x + j * tileSize, y + i * tileSize, tileSize, tileSize);

                // ohohoho its fun time
                if (tileMap[i][j] != 1) {
                    g.setColor(Color.DARK_GRAY);
                    if (j == 0) {
                        g.fillRect(x + j * tileSize - size, y + i * tileSize - size, size, tileSize + size * 2);
                    } else if (j == tileMap[i].length - 1) {
                        g.fillRect(x + j * tileSize + tileSize, y + i * tileSize - size, size, tileSize + size * 2);
                    }
                    if (i == 0) {
                        g.fillRect(x + j * tileSize - size, y + i * tileSize - size, tileSize + size * 2, size);
                    } else if (i == tileMap.length - 1) {
                        g.fillRect(x + j * tileSize - size, y + i * tileSize + tileSize, tileSize + size * 2, size);
                    }
                }

                switch (tileMap[i][j]) {
                    case 1 -> {
                        g.setColor(Color.DARK_GRAY);
                        // check surrounding tiles for other solids
                        if (j > 0 && tileMap[i][j - 1] != 1) {
                            // left tile
                            g.fillRect(x + j * tileSize, y + i * tileSize, size, tileSize);
                        }
                        if (j < tileMap[i].length - 1 && tileMap[i][j + 1] != 1) {
                            // right tile
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize, size, tileSize);
                        }
                        if (i > 0 && tileMap[i - 1][j] != 1) {
                            // upper tile
                            g.fillRect(x + j * tileSize, y + i * tileSize, tileSize, size);
                        }
                        if (i < tileMap.length - 1 && tileMap[i + 1][j] != 1) {
                            // bottom tile
                            g.fillRect(x + j * tileSize, y + i * tileSize + tileSize - size, tileSize, size);
                        }

                        // check for corners
                        if (j > 0 && i > 0 && tileMap[i - 1][j - 1] != 1) {
                            //lower right corner
                            g.fillRect(x + j * tileSize, y + i * tileSize, size, size);
                        }
                        if (j > 0 && i < tileMap.length - 1 && tileMap[i + 1][j - 1] != 1) {
                            //lower left corner
                            g.fillRect(x + j * tileSize, y + i * tileSize + tileSize - size, size, size);
                        }
                        if (j < tileMap[i].length - 1&& i > 0 && tileMap[i - 1][j + 1] != 1) {
                            //upper right corner
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize, size, size);
                        }
                        if (j < tileMap[i].length - 1 && i < tileMap.length - 1 && tileMap[i + 1][j + 1] != 1) {
                            //upper left corner
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize + tileSize - size, size, size);
                        }
                    }
                    case 2 -> {
                        g.setColor(new Color(175, 0, 0));

                        // check surrounding tiles for other solids
                        if (j > 0 && tileMap[i][j - 1] == 0) {
                            // left tile
                            g.fillRect(x + j * tileSize, y + i * tileSize, size, tileSize);
                        }
                        if (j < tileMap[i].length - 1 && tileMap[i][j + 1] == 0) {
                            // right tile
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize, size, tileSize);
                        }
                        if (i > 0 && tileMap[i - 1][j] == 0) {
                            // upper tile
                            g.fillRect(x + j * tileSize, y + i * tileSize, tileSize, size);
                        }
                        if (i < tileMap.length - 1 && tileMap[i + 1][j] == 0) {
                            // bottom tile
                            g.fillRect(x + j * tileSize, y + i * tileSize + tileSize - size, tileSize, size);
                        }

                        // check for corners
                        if (j > 0 && i > 0 && tileMap[i - 1][j - 1] == 0) {
                            //lower right corner
                            g.fillRect(x + j * tileSize, y + i * tileSize, size, size);
                        }
                        if (j > 0 && i < tileMap.length - 1 && tileMap[i + 1][j - 1] == 0) {
                            //lower left corner
                            g.fillRect(x + j * tileSize, y + i * tileSize + tileSize - size, size, size);
                        }
                        if (j < tileMap[i].length - 1&& i > 0 && tileMap[i - 1][j + 1] == 0) {
                            //upper right corner
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize, size, size);
                        }
                        if (j < tileMap[i].length - 1 && i < tileMap.length - 1 && tileMap[i + 1][j + 1] == 0) {
                            //upper left corner
                            g.fillRect(x + j * tileSize + tileSize - size, y + i * tileSize + tileSize - size, size, size);
                        }
                    }
                }
            }
        }

        g.setColor(Color.BLACK);
        g.drawString(instructionText, x + 50, y + getHeight() - 100);
    }

    public void snapRight(Collidable collider) {
        // move the player up so their base is on a tile boundary
        int leftTile = (int) Math.ceil((collider.getX() - collider.getWidth() / 2) / tileSize) - 1;
        if (leftTile < 0) {
            leftTile = -1;
        }
        if (leftTile >= tileMap[0].length) {
            // just give up
            return;
        }
        // move the player up to the top of the bottom tile
        double rightOfTile = leftTile * tileSize + tileSize;
        collider.setX(rightOfTile + collider.getWidth() / 2);
    }

    public void snapDown(Collidable collider) {
        // move the player up so their base is on a tile boundary
        int topTile = (int) Math.ceil((collider.getY() - collider.getHeight() / 2) / tileSize) - 1;
        if (topTile < 0) {
            topTile = -1;
        }
        if (topTile >= tileMap.length) {
            // just give up
            return;
        }
        // move the player up to the top of the bottom tile
        double bottomOfTile = topTile * tileSize + tileSize;
        collider.setY(bottomOfTile + collider.getHeight() / 2);
    }

    public void snapLeft(Collidable collider) {
        // move the player up so their base is on a tile boundary
        int rightTile = (int) (collider.getX() + collider.getWidth() / 2) / tileSize;
        if (rightTile > tileMap[0].length) {
            rightTile = tileMap[0].length;
        }
        if (rightTile < 0) {
            // just give up
            return;
        }
        // move the player up to the top of the bottom tile
        double leftOfTile = rightTile * tileSize;
        collider.setX(leftOfTile - collider.getWidth() / 2);
    }

    public void snapUp(Collidable collider) {
        // move the player up so their base is on a tile boundary
        int bottomTile = (int) (collider.getY() + collider.getHeight() / 2) / tileSize;
        if (bottomTile >= tileMap.length) {
            bottomTile = tileMap.length;
        }
        if (bottomTile < 0) {
            // just give up
            return;
        }
        // move the player up to the top of the bottom tile
        double topOfTile = bottomTile * tileSize;
        collider.setY(topOfTile - collider.getHeight() / 2);
    }

    public int getWidth() {
        return tileMap[0].length * tileSize;
    }

    public int getHeight() {
        return tileMap.length * tileSize;
    }

    public void teleportToWin(Player player) {
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                if (tileMap[i][j] == 3) {
                    player.setX(j * tileSize);
                    player.setY(i * tileSize);
                }
            }
        }
    }
}