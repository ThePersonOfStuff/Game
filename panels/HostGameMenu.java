package panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import comms.ClientData;
import comms.ClientFinder;

import javax.swing.JList;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class HostGameMenu extends JPanel implements ActionListener, ListSelectionListener {
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
        startGameButton.setEnabled(false);
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

        clientFinder = new ClientFinder("adkgjdskjfkdsjhdfjlsam");

        clientList = new JList<>(clientFinder.clientList());
        add(clientList);
        clientList.setFixedCellHeight(50);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setCellRenderer(new PlayerCellRenderer());
        clientList.addListSelectionListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startGameButton) {
            parent.switchPanels(PanelType.PLAYING_GAME);
            stopClientSearch();
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

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(clientList.getSelectedIndex() == -1) {
            startGameButton.setEnabled(false);
        } else {
            startGameButton.setEnabled(true);
        }
    }
}
