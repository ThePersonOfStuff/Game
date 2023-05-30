package comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.swing.DefaultListModel;

public class ClientFinder implements Runnable {
    public static InetAddress group;
    public static int port = 4000;
    public static NetworkInterface networkInterface;
    private DatagramSocket socket;
    private ServerSocket serverSocket;
    private byte[] sendData;
    private DatagramPacket sendPacket;
    private DefaultListModel<ClientData> clientSockets;
    private boolean running;
    private ClientData selfClient;
    private String name;
    private Socket selfSocket;
    private boolean exit = false;

    static {
        try {
            group = InetAddress.getByName("230.1.1.1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientFinder(String hostName) {
        try {

            socket = new DatagramSocket();

            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);

            sendData = (HostFinder.hostingMessage + "_" + hostName + "_" + InetAddress.getLocalHost().getHostAddress()
                    + "_" + serverSocket.getLocalPort()).getBytes(StandardCharsets.UTF_8);
            
            sendPacket = new DatagramPacket(sendData, sendData.length, group, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientSockets = new DefaultListModel<>();

        name = hostName;
    }

    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {

        }
        ClientFinder finder = new ClientFinder(args[0]);
        Thread finderThread = new Thread(finder);
        finderThread.start();
    }

    @Override
    public void run() {
        System.out.println("ran");
        if (selfClient == null || selfSocket == null) {
            try {
                selfSocket = new Socket();
                selfSocket.connect(serverSocket.getLocalSocketAddress());
                selfClient = new ClientData(serverSocket.accept(), name);
                clientSockets.addElement(selfClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        running = true;
        exit = false;
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                socket.send(sendPacket);

                // check for clients leaving
                for (int i = 0; i < clientSockets.size(); i++) {
                    if (clientSockets.get(i).inputStream().available() > 0) {
                        int byt = clientSockets.get(i).inputStream().read();
                        if (byt == 255) {
                            clientSockets.get(i).leave();
                            clientSockets.remove(i);
                        } else {
                            i--;
                            System.out.println("?????");
                        }
                    }
                }
                Socket acceptedSocket = serverSocket.accept();

                System.out.println("CLIENT FOUND");
                ClientData client = new ClientData(acceptedSocket);
                client.sendNames(clientSockets, selfClient);
                clientSockets.addElement(client);

            } catch (SocketTimeoutException e) {
                System.out.println("No clients found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // send leaving data out
            byte[] leaveData = (HostFinder.notHostingMessage + "_" + name + "_"
                    + InetAddress.getLocalHost().getHostAddress() + "_" + serverSocket.getLocalPort()).getBytes(StandardCharsets.UTF_8);
            DatagramPacket leavePacket = new DatagramPacket(leaveData, leaveData.length, group, port);
            socket.send(leavePacket);

            if (exit) {
                // tell all connected clients that the host is leaving, kicking them out
                for (int i = 0; i < clientSockets.size(); i++) {
                    clientSockets.get(i).outputStream().write(2);
                }

                // reset client list
                clientSockets.clear();

                // reset self client
                selfClient = null;
                selfSocket = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateName(String newName) {
        try {
            sendData = ("GAME HOSTING_" + newName + "_" + InetAddress.getLocalHost().getHostAddress() + "_"
                    + serverSocket.getLocalPort()).getBytes(StandardCharsets.UTF_8);

            sendPacket = new DatagramPacket(sendData, sendData.length, group, port);

            if (selfClient != null) {
                selfClient.setName(newName);
            }
            // remove old name and set new name
            for (int i = 0; i < clientSockets.size(); i++) {
                if (clientSockets.get(i) != selfClient) {
                    try {
                        clientSockets.get(i).outputStream().write(name.getBytes(StandardCharsets.UTF_8));
                        clientSockets.get(i).outputStream().write(1);
                        clientSockets.get(i).outputStream().write(newName.getBytes(StandardCharsets.UTF_8));
                        clientSockets.get(i).outputStream().write(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            name = newName;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public DefaultListModel<ClientData> clientList() {
        return clientSockets;
    }

    public void stop() {
        running = false;
    }

    public void exit() {
        running = false;
        exit = true;
    }

    public ClientData selfClient() {
        return selfClient;
    }

    public Socket selfSocket() {
        return selfSocket;
    }
}
