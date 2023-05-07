package panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import comms.HostData;
import comms.HostFinder;

import javax.swing.JList;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JoinGameMenu extends JPanel implements ActionListener, ListSelectionListener {
    private Screen parent;

    private JButton joinGameButton;
    private JButton returnToMenuButton;

    private JList<HostData> hostList;

    private HostFinder hostFinder;

    private Thread hostFinderThread;

    public JoinGameMenu(Screen p) {
        setVisible(false);

        parent = p;

        setLayout(null);

        joinGameButton = new MenuButton("Join game");
        joinGameButton.setEnabled(false);
        add(joinGameButton);
        joinGameButton.addActionListener(this);
        returnToMenuButton = new MenuButton("Main menu");
        add(returnToMenuButton);
        returnToMenuButton.addActionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                hostList.setBounds(50, 50, getWidth() - 100, getHeight() - 200);
                joinGameButton.setBounds(getWidth() / 2 - 225, getHeight() - 125, 150, 50);
                returnToMenuButton.setBounds(getWidth() / 2 + 75, getHeight() - 125, 150, 50);
            }
        });

        hostFinder = new HostFinder();

        hostList = new JList<>(hostFinder.foundHosts());
        add(hostList);
        hostList.setFixedCellHeight(50);
        hostList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hostList.setCellRenderer(new PlayerCellRenderer());
        hostList.addListSelectionListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == joinGameButton) {
            parent.switchPanels(PanelType.GAME_LOBBY);
            stopHostSearch();
        } else if (e.getSource() == returnToMenuButton) {
            parent.switchPanels(PanelType.MAIN_MENU);
            stopHostSearch();
        }
    }

    public void startHostSearch() {
        hostFinderThread = new Thread(hostFinder);
        hostFinderThread.start();
    }

    public void stopHostSearch() {
        hostFinder.stop();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(hostList.getSelectedIndex() == -1) {
            joinGameButton.setEnabled(false);
        } else {
            joinGameButton.setEnabled(true);
        }
    }
}
