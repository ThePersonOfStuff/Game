package panels;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public class MenuButton extends JButton {
    public MenuButton(String name) {
        super(name);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(100, 175, 75));
    }

    @Override
    public void paintComponent(Graphics g) {
        Color b = getBackground();
        Color borderColor = new Color(b.getRed() / 3 * 2, b.getGreen() / 3 * 2, b.getBlue() / 3 * 2, b.getAlpha());
        Color fillColor = isEnabled() ? b : new Color(b.getRed() / 4 * 3, b.getGreen() / 4 * 3, b.getBlue() / 4 * 3, b.getAlpha());
        g.setColor(borderColor);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g.setColor(fillColor);
        g.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
        g.setColor(getForeground());
        drawCenteredString(g, getText(), getWidth()/2, getHeight()/2);
    }

    private void drawCenteredString(Graphics g, String text, int xPos, int yPos) {
        FontMetrics metrics = g.getFontMetrics(getFont());
        
        int x = xPos - metrics.stringWidth(text) / 2;
        int y = yPos - metrics.getHeight() / 2 + metrics.getAscent();
        
        g.setFont(getFont());
        g.drawString(text, x, y);
    }
}
