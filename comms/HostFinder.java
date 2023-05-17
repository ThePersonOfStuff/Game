package comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

public class HostFinder implements Runnable {
    private static final String hostingMessage = "GAME HOSTING";
    private MulticastSocket socket;
    private byte[] receiveData;
    private DatagramPacket recievePacket;
    private DefaultListModel<HostData> foundHosts;
    private boolean running;

    public static void main(String[] args) {
        Thread thread = new Thread(new HostFinder());
        thread.start();
    }

    public HostFinder() {
        try {
            receiveData = new byte[1000];
            recievePacket = new DatagramPacket(receiveData, receiveData.length);

            socket = new MulticastSocket(ClientFinder.port);
            socket.joinGroup(new InetSocketAddress(ClientFinder.group, ClientFinder.port), null);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        foundHosts = new DefaultListModel<>();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                for (int i = 0; i < receiveData.length; i++) {
                    receiveData[i] = 0;
                }
                socket.receive(recievePacket);
                String resp = new String(receiveData);
                System.out.println(resp);
                if (resp.trim().substring(0, hostingMessage.length()).equals(hostingMessage)) {
                    String[] sections = resp.trim().split("_");
                    HostData data = new HostData(new InetSocketAddress(sections[2], Integer.parseInt(sections[3])),
                            sections[1]);
                    if (!foundHosts.contains(data)) {
                        foundHosts.addElement(data);
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("No hosts found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ListModel<HostData> foundHosts() {
        return foundHosts;
    }

    public void stop() {
        running = false;
    }
}
