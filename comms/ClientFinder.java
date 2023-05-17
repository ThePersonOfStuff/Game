package comms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;

public class ClientFinder implements Runnable {
    public static InetAddress group;
    public static int port = 4000;
    private DatagramSocket socket;
    private ServerSocket serverSocket;
    private byte[] sendData;
    private DatagramPacket sendPacket;
    private DefaultListModel<ClientData> clientSockets;
    private boolean running;
    private ClientData selfClient;
    private String name;
    private Socket selfSocket;

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
            socket.setBroadcast(true);
            
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);

            sendData = ("GAME HOSTING_" + hostName + "_" + InetAddress.getLocalHost().getHostAddress() + "_" + serverSocket.getLocalPort()).getBytes();
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
        if(selfClient == null || selfSocket == null) {
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
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                socket.send(sendPacket);
                
                //check for clients leaving
                for(int i = 0; i < clientSockets.size(); i++) {
                    if(clientSockets.get(i).inputStream().available() > 0) {
                        int byt = clientSockets.get(i).inputStream().read();
                        if(byt == 255) {
                            clientSockets.get(i).leave();
                        } else {
                            System.out.println("?????");
                        }
                    }
                }
                Socket acceptedSocket = serverSocket.accept();

                System.out.println("CLIENT FOUND");
                ClientData client = new ClientData(acceptedSocket);
                clientSockets.addElement(client);
                client.sendNames(clientSockets);

            } catch (SocketTimeoutException e) {
                System.out.println("No clients found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateName(String newName) {
        try {
            sendData = ("GAME HOSTING_" + newName + "_" + InetAddress.getLocalHost().getHostAddress() + "_" + serverSocket.getLocalPort()).getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, group, port);

            if(selfClient != null) {
                selfClient.setName(newName);
            }
            //remove old name and set new name
            for(int i = 0; i < clientSockets.size(); i++) {
                if(clientSockets.get(i) != selfClient) {
                    try {
                        clientSockets.get(i).outputStream().write(name.getBytes());
                        clientSockets.get(i).outputStream().write(1);
                        clientSockets.get(i).outputStream().write(newName.getBytes());
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

    public ClientData selfClient() {
        return selfClient;
    }

    public Socket selfSocket() {
        return selfSocket;
    }
}
