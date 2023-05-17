package panels;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import comms.HostData;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.Socket;

public class LobbyMenu extends JPanel implements Runnable, ActionListener{
    private Screen parent;

    private JList<String> nameList;
    private DefaultListModel<String> names;

    private JButton leaveLobbyButton;

    private Socket hostSocket;

    private Thread nameMonitor;

    private boolean monitoring;

    public LobbyMenu(Screen p) {
        parent = p;

        names = new DefaultListModel<>();
        nameList = new JList<>(names);
        add(nameList);
        nameList.setFixedCellHeight(50);
        nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nameList.setCellRenderer(new PlayerCellRenderer());

        leaveLobbyButton = new MenuButton("Leave lobby");
        add(leaveLobbyButton);
        leaveLobbyButton.addActionListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                nameList.setBounds(50, 50, getWidth() - 100, getHeight() - 200);
                leaveLobbyButton.setBounds(getWidth() / 2 + 75, getHeight() - 125, 150, 50);
            }
        });
    }

    public void joinHost(HostData hostData) {
        try {
            hostSocket = new Socket();
            hostSocket.connect(hostData.address());

            hostSocket.getOutputStream().write(parent.getName().getBytes());
            hostSocket.getOutputStream().write(0);

            nameMonitor = new Thread(this);
            nameMonitor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void leaveLobby() {
        //send the host 255 to indicate leaving and stop the monitor thread
        monitoring = false;
        try {
            hostSocket.getOutputStream().write(255);
            hostSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // names are terminated by 0 or 1 if deleting
        // the game starting will be signified by 255
        // names only contain "a-zA-z ",

        String nextName = "";

        monitoring = true;
        mainloop:
        while(monitoring) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                while(hostSocket.getInputStream().available() > 0) {
                    int byt = hostSocket.getInputStream().read();
                    if(byt == 255) {
                        //its gaming time
                        parent.switchPanels(PanelType.PLAYING_GAME);
                        monitoring = false;
                        break mainloop;
                    } else if(byt == 0) {
                        names.addElement(nextName);
                        nextName = "";
                    } else if(byt == 1) {
                        names.removeElement(nextName);
                        nextName = "";
                    } else {
                        nextName += (char)byt;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == leaveLobbyButton) {
            leaveLobby();
            parent.switchPanels(PanelType.JOIN_GAME);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public Socket getHost() {
        return hostSocket;
    }
}
