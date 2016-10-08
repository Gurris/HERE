import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Marthin on 2016-09-21.
 */
public class GameSession {
    private DatagramSocket sock;
    private DatagramPacket receivedPack;
    private DatagramPacket sendPack;
    private Client currentClient;
    private byte[] receivedData;
    private byte[] sendData;
    private String strReceived;
    private boolean isRunning;
    private Server server;

    public GameSession(DatagramSocket sock, Client currentClient, Server server) {
        this.sock = sock;
        this.currentClient = currentClient;
        isRunning = true;
        receivedData = new byte[1024];
        sendData = new byte[1024];
        receivedPack = new DatagramPacket(receivedData, receivedData.length);
        sendPack = new DatagramPacket(sendData, sendData.length, currentClient.getAddr(), currentClient.getPort());
        this.server = server;
    }
    public void start() {
        String regex = "\\d+";
        Random rng = new Random();
        int ans = rng.nextInt(100) + 1;
        System.out.println(ans);
        while(isRunning){
            try {
                sock.setSoTimeout(5000);
                sock.receive(receivedPack);
                strReceived = new String(receivedPack.getData(), receivedPack.getOffset(), receivedPack.getLength());
                if((currentClient.getPort() != receivedPack.getPort()) || !currentClient.getAddr().equals(receivedPack.getAddress())) {
                    server.managePreoccupation(new Client(receivedPack.getAddress(), receivedPack.getPort()), strReceived);
                } else {
                    if(!strReceived.matches(regex)) {
                        server.send("ERROR",currentClient);
                    } else if (ans == Integer.parseInt(strReceived)) {
                        server.send("CORRECT, GAME ENDING",currentClient);
                        isRunning = false;
                    } else if (ans > Integer.parseInt(strReceived)) {
                        server.send("TOO LOW",currentClient);
                    } else if (ans < Integer.parseInt(strReceived)) {
                        server.send("TOO HIGH",currentClient);
                    }
                }
            } catch (SocketTimeoutException e) {
                server.send("CLOSED SESSION", currentClient);
                isRunning=false;
                System.out.println("closed session");
                try {
                    sock.setSoTimeout(0);
                } catch (SocketException e1) {
                    e1.getMessage();
                }
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
    }
}
