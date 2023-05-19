package panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import comms.ClientData;
import comms.ClientFinder;
import game.GameHoster;

import javax.swing.JList;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.Socket;

public class HostGameMenu extends JPanel implements ActionListener {
    private Screen parent;

    private JButton startGameButton;
    private JButton returnToMenuButton;

    private JList<ClientData> clientList;

    private ClientFinder clientFinder;

    private Thread clientFinderThread;

    public HostGameMenu(Screen p) {
        setVisible(false);

        parent = p;

        setLayout(null);

        startGameButton = new MenuButton("Start game");
        add(startGameButton);
        startGameButton.addActionListener(this);
        returnToMenuButton = new MenuButton("Main menu");
        add(returnToMenuButton);
        returnToMenuButton.addActionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                clientList.setBounds(50, 50, getWidth() - 100, getHeight() - 200);
                startGameButton.setBounds(getWidth() / 2 - 225, getHeight() - 125, 150, 50);
                returnToMenuButton.setBounds(getWidth() / 2 + 75, getHeight() - 125, 150, 50);
            }
        });

        clientFinder = new ClientFinder(p.getName());

        clientList = new JList<>(clientFinder.clientList());
        add(clientList);
        clientList.setFixedCellHeight(50);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setCellRenderer(new PlayerCellRenderer());
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.BLACK);
        drawCenteredString(g, "Players", getWidth()/2, 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startGameButton) {
            stopClientSearch();
            //its gaming time
            Thread gameHosterThread = new Thread(new GameHoster(clientFinder.clientList()));
            gameHosterThread.start();

            //read own 255 message first
            try {
                int byt = clientFinder.selfSocket().getInputStream().read();
                if(byt != 255) {
                    System.exit(1);
                    throw new IllegalArgumentException();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            parent.switchPanels(PanelType.PLAYING_GAME);
        } else if (e.getSource() == returnToMenuButton) {
            parent.switchPanels(PanelType.MAIN_MENU);
            stopClientSearch();
        }
    }

    public void startClientSearch() {
        clientFinderThread = new Thread(clientFinder);
        clientFinderThread.start();
    }

    public void stopClientSearch() {
        clientFinder.stop();
    }

    public void setName(String name) {
        if(clientFinder != null) {
            clientFinder.updateName(name);
        }
    }

    public Socket getHost() {
        return clientFinder.selfSocket();
    }
    
    private void drawCenteredString(Graphics g, String text, int xPos, int yPos) {
        FontMetrics metrics = g.getFontMetrics(getFont());
        
        int x = xPos - metrics.stringWidth(text) / 2;
        int y = yPos - metrics.getHeight() / 2 + metrics.getAscent();
        
        g.setFont(getFont());
        g.drawString(text, x, y);
    }
}
