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
import javax.swing.ListModel;

public class ClientFinder implements Runnable {
    private DatagramSocket socket;
    private ServerSocket serverSocket;
    private byte[] sendData;
    private DatagramPacket sendPacket;
    private DefaultListModel<ClientData> clientSockets;
    private boolean running;
    private ClientData selfClient;

    public ClientFinder(String hostName) {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);
            
            sendData = ("GAME HOSTING_" + hostName + "_" + InetAddress.getLocalHost().getHostAddress() + "_" + serverSocket.getLocalPort()).getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 9999);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientSockets = new DefaultListModel<>();
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
        if(selfClient == null) {
            try {
                selfClient = new ClientData(new Socket("127.0.0.1", serverSocket.getLocalPort()));
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
                
                Socket acceptedSocket = serverSocket.accept();

                System.out.println("CLIENT FOUND!!!!! WOOO!!!");
                clientSockets.addElement(new ClientData(acceptedSocket));
            } catch (SocketTimeoutException e) {
                System.out.println("No clients found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateName(String newName) {
        try {
            sendData = ("GAME_HOSTING_" + newName).getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 9999);

            if(selfClient != null) {
                selfClient.setName(newName);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public ListModel<ClientData> clientList() {
        return clientSockets;
    }

    public void stop() {
        running = false;
    }
}
