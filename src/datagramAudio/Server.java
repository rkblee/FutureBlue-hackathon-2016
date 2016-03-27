package datagramAudio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

public class Server {
    boolean outVoice = true;
    AudioFormat format = AudioUtil.getAudioFormat();
    private ServerSocket serverSocket;
    Socket server;
    static int newClientId = 0;

    public static void main(String args[]) throws ClassNotFoundException, IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        Map<ObjectOutputStream, Integer> connectionsMap = new HashMap<ObjectOutputStream, Integer>();

        try {
            serverSocket = new ServerSocket(3000);
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
            Socket socket = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            List<ObjectOutputStream> removeStreamList = new LinkedList<ObjectOutputStream>();

            ConnectionDataPacket newClientInput = (ConnectionDataPacket) in.readObject();
            newClientId++;

            ConnectionDataPacket newClientInfo = new ConnectionDataPacket(ConnectionActionEnum.ADD, newClientId, newClientInput.info);
            out.writeObject(newClientInfo);
            newClientInput = (ConnectionDataPacket) in.readObject();
            for (ObjectOutputStream os : connectionsMap.keySet()) {
                try {
                    os.writeObject(newClientInfo);
                    System.out.println(newClientInfo.info);
                } catch (IOException e) {
                    removeStreamList.add(os);
                }
            }
            for (ObjectOutputStream removeStream : removeStreamList) {
                connectionsMap.remove(removeStream);
            }
            connectionsMap.put(out, newClientId);
            System.out.println("done here");
        }

        serverSocket.close();

    }
}