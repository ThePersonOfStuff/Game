package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import game.GameState;

public class GamePanel extends JPanel {
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

        getActionMap().put("Jump pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("JUMP", true);
            }
        });

        getActionMap().put("Jump released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("JUMP", false);
            }
        });

        getActionMap().put("Left pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("LEFT", true);
            }
        });

        getActionMap().put("Left released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("LEFT", false);
            }
        });

        getActionMap().put("Right pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("RIGHT", true);
            }
        });

        getActionMap().put("Right released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("RIGHT", false);
            }
        });

        getActionMap().put("Down pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("FALL", true);
            }
        });

        getActionMap().put("Down released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keysPressed.put("FALL", false);
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("pressed W"), "Jump pressed");
        getInputMap().put(KeyStroke.getKeyStroke("released W"), "Jump released");
        getInputMap().put(KeyStroke.getKeyStroke("pressed A"), "Left pressed");
        getInputMap().put(KeyStroke.getKeyStroke("released A"), "Left released");
        getInputMap().put(KeyStroke.getKeyStroke("pressed D"), "Right pressed");
        getInputMap().put(KeyStroke.getKeyStroke("released D"), "Right released");
        getInputMap().put(KeyStroke.getKeyStroke("pressed S"), "Down pressed");
        getInputMap().put(KeyStroke.getKeyStroke("released S"), "Down released");
    }

    public void startGame(Socket host) {
        state = new GameState(host);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        state.updatePositions(keysPressed);
        state.draw(g, getWidth(), getHeight());
    }

    public void leaveGame() {
        parent.switchPanels(PanelType.MAIN_MENU);
    }
}
