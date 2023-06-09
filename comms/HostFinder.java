package comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

public class HostFinder implements Runnable {
    public static final String hostingMessage = "GAME HOSTING";
    public static final String notHostingMessage = "GAME NOT HOSTING";
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

            Enumeration<NetworkInterface> netInts = NetworkInterface.getNetworkInterfaces();
            while(netInts.hasMoreElements()) {
                try {
                    NetworkInterface n = netInts.nextElement();
                    socket.joinGroup(new InetSocketAddress(ClientFinder.group, ClientFinder.port), n);
                    System.out.println(n);
                } catch (Exception e) {}
            }
            
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
                String resp = new String(receiveData, StandardCharsets.UTF_8);
                System.out.println(resp);
                if (resp.trim().substring(0, hostingMessage.length()).equals(hostingMessage)) {
                    String[] sections = resp.trim().split("_");
                    HostData data = new HostData(new InetSocketAddress(sections[2], Integer.parseInt(sections[3])),
                            sections[1]);
                    if (!foundHosts.contains(data)) {
                        foundHosts.addElement(data);
                    }
                }
                else if(resp.trim().substring(0, notHostingMessage.length()).equals(notHostingMessage)) {
                    //time to remove it;
                    String[] sections = resp.trim().split("_");
                    HostData data = new HostData(new InetSocketAddress(sections[2], Integer.parseInt(sections[3])),
                            sections[1]);
                    if(!foundHosts.removeElement(data)) {
                        System.out.println("Must be an off-by-one error");
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
