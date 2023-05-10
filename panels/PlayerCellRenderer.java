package panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import comms.ClientData;
import comms.HostData;

public class PlayerCellRenderer extends JPanel implements ListCellRenderer<Object> {
    private String displayText;
    private boolean selected;

    @Override
    public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if(value instanceof HostData) {
            displayText = ((HostData)value).name();
        } else if(value instanceof ClientData) {
            displayText = ((ClientData)value).name();
        } else {
            displayText = value.toString();
        }
        selected = isSelected;
        return this;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(selected ? Color.GRAY : Color.LIGHT_GRAY);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g.setColor(Color.BLACK);
        drawCenteredString(g, displayText, getWidth()/2, getHeight()/2);
    }

    private void drawCenteredString(Graphics g, String text, int xPos, int yPos) {
        FontMetrics metrics = g.getFontMetrics(getFont());
        
        int x = xPos - metrics.stringWidth(text) / 2;
        int y = yPos - metrics.getHeight() / 2 + metrics.getAscent();
        
        g.setFont(getFont());
        g.drawString(text, x, y);
    }
}
