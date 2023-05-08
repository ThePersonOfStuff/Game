package panels;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

import comms.HostData;

import java.awt.Graphics;
import java.io.IOException;
import java.net.Socket;

public class LobbyMenu extends JPanel implements Runnable{
    private Screen parent;

    private JList<String> nameList;
    private DefaultListModel<String> names;

    private JButton leaveLobby;

    private Socket hostSocket;

    private Thread nameMonitor;

    private boolean monitoring;

    public LobbyMenu(Screen p) {
        parent = p;

        names = new DefaultListModel<>();
        nameList = new JList<>(names);
        add(nameList);
    }

    public void joinHost(HostData hostData) {
        try {
            hostSocket = new Socket();
            hostSocket.connect(hostData.address());

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
                        break mainloop;
                    } else if(byt == 0) {
                        names.addElement(nextName);
                    } else if(byt == 1) {
                        names.removeElement(nextName);
                    } else {
                        nextName += (char)byt;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
