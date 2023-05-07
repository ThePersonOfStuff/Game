package panels;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import comms.HostData;

import java.awt.Graphics;
import java.io.IOException;
import java.net.Socket;

public class LobbyMenu extends JPanel {
    private Screen parent;

    private JList<String> nameList;
    private DefaultListModel<String> names;

    private Socket hostSocket;

    public LobbyMenu(Screen p) {
        parent = p;

        names = new DefaultListModel<>();
        nameList = new JList<>(names);
    }

    public void joinHost(HostData hostData) {
        try {
            hostSocket = new Socket(hostData.address().getAddress(), hostData.address().getPort());

            

            receiveSocketInformation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        receiveSocketInformation();
    }

    private void receiveSocketInformation() {
        // names are terminated by null characters
        // the game starting will be signified by 255
        // names never have 0 or 255
        try {
            while (hostSocket != null && hostSocket.getInputStream().available() > 0) {
                byte[] bytes = hostSocket.getInputStream().readAllBytes();
                for(int i = 0; i < bytes.length; i++) {
                    if(bytes[i] == 255) {
                        parent.switchPanels(PanelType.PLAYING_GAME);
                    } 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
