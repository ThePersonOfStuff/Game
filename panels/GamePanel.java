package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JPanel;

import game.GameState;

public class GamePanel extends JPanel implements KeyListener {
    private Screen parent;

    private GameState state;
    private HashMap<String, Boolean> keysPressed;

    public GamePanel(Screen p) {
        parent = p;
        keysPressed = new HashMap<>();
        keysPressed.put("LEFT", false);
        keysPressed.put("RIGHT", false);
        keysPressed.put("JUMP", false);
        keysPressed.put("FALL", false);
    }

    public void startGame(Socket host) {
        addKeyListener(this);
        parent.addKeyListener(this);
        requestFocus();

        state = new GameState(host);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        state.updatePositions(keysPressed);
        state.draw(g, getWidth(), getHeight());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        return;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar() == 'a') {
            keysPressed.put("LEFT", true);
        } else if(e.getKeyChar() == 'd') {
            keysPressed.put("RIGHT", true);
        } else if(e.getKeyChar() == 'w') {
            keysPressed.put("JUMP", true);
        } else if(e.getKeyChar() == 's') {
            keysPressed.put("FALL", true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyChar() == 'a') {
            keysPressed.put("LEFT", false);
        } else if(e.getKeyChar() == 'd') {
            keysPressed.put("RIGHT", false);
        } else if(e.getKeyChar() == 'w') {
            keysPressed.put("JUMP", false);
        } else if(e.getKeyChar() == 's') {
            keysPressed.put("FALL", false);
        }
    }
}
