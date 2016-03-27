package datagramAudio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;

public class ClientHandshakeListenerThread extends Thread {
    ServerSocket serverSocket;
    ConcurrentMap<Integer, ConnectionInfo> connectionMap;
    int id;

    public ClientHandshakeListenerThread(int id, int listenPort, ConcurrentMap<Integer, ConnectionInfo> connectionMap) {
        super("ClientHandshakeListenerThread");
        try {
            this.serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
        this.connectionMap = connectionMap;
        this.id = id;
        System.out.println("Now listening on " + listenPort);
    }

    @Override
    public void run() {
        boolean listening = true;
        while (listening) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                System.out.println("New request!");
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                DatagramSocket ds = new DatagramSocket();
                int newPort = ds.getLocalPort();
                new ClientListener(AudioUtil.getAudioFormat(), ds).start();

                ConnectionDataPacket inPacket = (ConnectionDataPacket) in.readObject();
                System.out.println("???");
                connectionMap.put(inPacket.id, inPacket.info);
                ConnectionDataPacket outPacket = new ConnectionDataPacket();
                outPacket.info.hostname = InetAddress.getLocalHost().getHostAddress();
                outPacket.info.port = newPort;
                outPacket.id = this.id;
                out.writeObject(outPacket);

                System.out.println("new connection from " + inPacket.info);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
