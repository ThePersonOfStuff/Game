package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;

public class WinLevel extends Level {
    private ArrayList<Player> rankings;
    private Font titleFont;

    public WinLevel() {
        super(new byte[20][50]);
        rankings = new ArrayList<>();
        titleFont = new Font("Ariel", Font.BOLD, 30);
    }

    @Override
    public void draw(Graphics g, int x, int y) {
        super.draw(g, x, y);
        x += getWidth() / 2;
        y += getHeight() / 4;
        Font prevFont = g.getFont();
        g.setFont(titleFont);
        g.setColor(Color.BLACK);
        drawCenteredString(g, "Winners:", x, y);
        g.setFont(prevFont);
        y += 50;
        for (int i = 0; i < rankings.size(); i++) {
            drawCenteredString(g, (i + 1) + ": " + rankings.get(i).getName(), x, y);
            y += 25;
        }
    }

    private void drawCenteredString(Graphics g, String text, int xPos, int yPos) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int x = xPos - metrics.stringWidth(text) / 2;
        int y = yPos - metrics.getHeight() / 2 + metrics.getAscent();

        g.drawString(text, x, y);
    }

    public void addRank(Player player) {
        if (!rankings.contains(player))
            rankings.add(player);
    }
}
