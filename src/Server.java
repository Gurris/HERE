import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Marthin on 2016-09-07.
 */
public class Server {
    private int port;
    private DatagramSocket sock;
    private DatagramPacket receivedPack;
    private DatagramPacket sendPack;
    private Client currentClient;
    private Queue<Client> clientQueue;
    private byte[] receivedData;
    private byte[] sendData;
    private String strReceived;
    private boolean ready;
    public Server(int port) {
        this.port = port;
        sock = null;
        receivedData = new byte[1024];
        sendData = new byte[1024];
        receivedPack = new DatagramPacket(receivedData, receivedData.length);
        clientQueue = new LinkedList<Client>();
    }
    public void start(){
        try {
            this.sock = new DatagramSocket(this.port);
            System.out.println("Starting server...");
            while(true){
                while(listen());
                startGame();
                currentClient = null;
                sock.setSoTimeout(0);

            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } finally {
            sock.close();
            System.out.println("server shutdown");
        }
    }
    private boolean listen(){
        nextPlayer();
        try {
            sock.receive(receivedPack);
            strReceived = new String(receivedPack.getData(), receivedPack.getOffset(), receivedPack.getLength());
            if(currentClient == null) {
                currentClient = new Client(receivedPack.getAddress(), receivedPack.getPort());
                ready = false;
                sock.setSoTimeout(10000);
            }
            if((currentClient.getPort() != receivedPack.getPort()) || !currentClient.getAddr().equals(receivedPack.getAddress())) {
                managePreoccupation(new Client(receivedPack.getAddress(), receivedPack.getPort()), strReceived);
                return true;
            }
            if(strReceived.equals("HELLO")) {
                send("OK", currentClient);
                ready = true;
            } else if (strReceived.equals("START") && ready) {
                send("READY",currentClient);
                return false;
            }
            else {
                send("ERROR",currentClient);
            }
        } catch (SocketTimeoutException e) {
            send("CLOSED SESSION", currentClient);
            currentClient= null;
            System.out.println("closed session");
            try {
                sock.setSoTimeout(0);
            } catch (SocketException e1) {
                e1.getMessage();
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    private void startGame() {
        System.out.println("starting game");
        GameSession session = new GameSession(sock, currentClient, this);
        session.start();
    }
    public void managePreoccupation(Client client, String str){
        if(str.equals("HELLO")) {
            addClientToQueue(client);
        } else {
            send("BUSY, TYPE \"HELLO\" TO JOIN THE QUEUE",client);
        }

    }
    private void addClientToQueue(Client client){
        boolean contains = false;
        if (clientQueue.size() < 1) {
            clientQueue.add(client);
            send("BUSY, ADDED TO THE QUEUE",client);
        } else {
            for (Client c : clientQueue) {
                if (c.getAddr() == client.getAddr() && c.getPort() == client.getPort()) {
                    contains = true;
                    send("ALREADY IN THE QUEUE",client);
                }
            }
            if (!contains) {
                clientQueue.add(client);
                send("BUSY, ADDED TO THE QUEUE",client);
            }
        }
    }
    private void nextPlayer() {
        if(currentClient == null && clientQueue.size()>0){
            currentClient=clientQueue.poll();
            send("READY",currentClient);
            try {
                sock.setSoTimeout(10000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }
    public void send(String response, Client client){
        sendPack = new DatagramPacket(sendData, sendData.length, client.getAddr(), client.getPort());
        sendData = response.getBytes();
        sendPack.setData(sendData, 0, sendData.length);
        try {
            sock.send(sendPack);
        } catch (IOException e) {
            e.getMessage();
        }
    }
}

