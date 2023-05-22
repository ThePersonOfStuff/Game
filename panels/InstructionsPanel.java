package panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class InstructionsPanel extends JPanel implements ActionListener {
    private Screen parent;

    private JLabel instructions;
    private JButton returnToMainMenu;

    public InstructionsPanel(Screen p) {
        setLayout(null);

        parent = p;

        instructions = new JLabel("<html><p>In this game, the goal is to obtain an item and then navigate to the green section of each level. You will be racing against other players, or you can play on your own. To host a game, enter a username (can only contain letters and spaces) and press \"Host game\". To join a game, make sure someone on your wifi network is hosting, enter a username, and press join game. Any hosts should appear in a list; select the one you want and press join. The host can start a game whenever they want, and when they do, all players in the lobby are transported to the first level. The controls are WASD for movement.</p></html>");
        
                add(instructions);

        returnToMainMenu = new MenuButton("Return to menu");
        add(returnToMainMenu);
        returnToMainMenu.addActionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                returnToMainMenu.setBounds(getWidth()/2 - 75, getHeight() - 100, 150, 50);
                instructions.setBounds(50, 50, getWidth()-100, getHeight() - 200);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == returnToMainMenu) {
            parent.switchPanels(PanelType.MAIN_MENU);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
