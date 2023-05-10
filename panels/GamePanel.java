package panels;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;

import javax.swing.JPanel;

import game.GameState;

public class GamePanel extends JPanel implements MouseListener{
    private Screen parent;

    private GameState state;

    public GamePanel(Screen p) {
        parent = p;
        addMouseListener(this);
    }

    public void startGame(Socket host) {
        state = new GameState(host);
    }

    @Override
    public void paintComponent(Graphics g) {
        state.updatePositions();
        state.draw(g, getWidth(), getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        state.moveMeRight();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        state.moveMeRight();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        state.moveMeRight();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        state.moveMeRight();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        state.moveMeRight();
    }
}
