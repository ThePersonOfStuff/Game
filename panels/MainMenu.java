package panels;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;

public class MainMenu extends JPanel implements ActionListener{
    private Screen parent;

    private JButton joinGameButton;
    private JButton hostGameButton;

    public MainMenu(Screen p) {
        parent = p;

        setLayout(null);

        joinGameButton = new MenuButton("Join game");
        add(joinGameButton);
        joinGameButton.addActionListener(this);
        hostGameButton = new MenuButton("Host game");
        add(hostGameButton);
        hostGameButton.addActionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                joinGameButton.setBounds(getWidth()/2 - 75, getHeight() / 2, 150, 50);
                hostGameButton.setBounds(getWidth()/2 - 75, getHeight() / 2 + 100, 150, 50);
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == joinGameButton) {
            parent.switchPanels(PanelType.JOIN_GAME);
        } else if(e.getSource() == hostGameButton) {
            parent.switchPanels(PanelType.HOST_GAME);
        }
    }
}
