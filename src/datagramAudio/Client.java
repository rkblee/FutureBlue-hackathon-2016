package datagramAudio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Client {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ConcurrentMap<Integer, ConnectionInfo> connectionMap = new ConcurrentHashMap<Integer, ConnectionInfo>();

        String serverName = "localhost";
        int port = 3000;
        int listenPortBase = 5000;
        System.out.println("Connecting to server:" + serverName + " Port:" + port);

        Socket socket = null;

        try {
            socket = new Socket(serverName, port);
        } catch (UnknownHostException e) {
            System.err.println("ERROR: Don't know where to connect!!");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("ERROR: Couldn't get I/O for the connection.");
            System.exit(1);
        }

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        ConnectionDataPacket newClientPacket = new ConnectionDataPacket();
        newClientPacket.info.hostname = InetAddress.getLocalHost().getHostAddress();
        newClientPacket.info.port = listenPortBase;
        newClientPacket.action = ConnectionActionEnum.ADD;
        out.writeObject(newClientPacket);

        ConnectionDataPacket incomingDataPacket = (ConnectionDataPacket) in.readObject();
        System.out.println(incomingDataPacket.info);
        int id = incomingDataPacket.id;
        int listenPort = listenPortBase + id;

        // start thread to handle incoming handshakes
        new ClientHandshakeListenerThread(id, listenPort, connectionMap).start();

        // start sender thread
        new ClientSender(AudioUtil.getAudioFormat(), connectionMap).start();

        out.writeObject(new ConnectionDataPacket());

        while ((incomingDataPacket = (ConnectionDataPacket) in.readObject()) != null) {
            switch (incomingDataPacket.action) {
            case ADD:
                // do handshake with incomingDataPacket.info
                String newClientHostname = incomingDataPacket.info.hostname;
                int newClientPort = incomingDataPacket.info.port + incomingDataPacket.id;
                System.out.println("sending request to " + newClientHostname + ":" + newClientPort);
                Socket newClientSocket = null;
                try {
                    newClientSocket = new Socket(newClientHostname, newClientPort);
                    ObjectOutputStream outHS = new ObjectOutputStream(newClientSocket.getOutputStream());
                    ObjectInputStream inHS = new ObjectInputStream(newClientSocket.getInputStream());
                    ConnectionDataPacket outPacket = new ConnectionDataPacket();
                    DatagramSocket ds = new DatagramSocket();
                    new ClientListener(AudioUtil.getAudioFormat(), ds).start();
                    outPacket.id = id;
                    outPacket.info.hostname = InetAddress.getLocalHost().getHostAddress();
                    outPacket.info.port = ds.getLocalPort();
                    System.out.println("???");
                    outHS.writeObject(outPacket);
                    System.out.println("???");
                    ConnectionDataPacket inPacket = (ConnectionDataPacket) inHS.readObject();
                    connectionMap.put(inPacket.id, inPacket.info);

                    System.out.println("new connection to " + inPacket.info);
                } catch (UnknownHostException e) {
                    System.err.println("ERROR: Don't know where to connect!!");
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("ERROR: Couldn't get I/O for the connection.");
                    System.exit(1);
                }

                break;
            case REMOVE:
                connectionMap.remove(incomingDataPacket.id);
                break;
            default:
                break;
            }
        }

    }
}