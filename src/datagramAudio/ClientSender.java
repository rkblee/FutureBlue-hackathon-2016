package datagramAudio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class ClientSender extends Thread {
    private ConcurrentMap<Integer, ConnectionInfo> connectionMap;
    private AudioFormat format;
    private DatagramSocket ds;

    public ClientSender(AudioFormat format, ConcurrentMap<Integer, ConnectionInfo> connectionMap) throws SocketException {
        super("ClientSender");
        System.out.println("ClientSender");
        this.ds = new DatagramSocket();
        this.connectionMap = connectionMap;
        this.format = format;
    }

    @Override
    public void run() {
        try {
            System.out.println("Listening from mic.");
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(micInfo);
            mic.open(format);
            System.out.println("Mic open.");
            byte tmpBuff[] = new byte[mic.getBufferSize() / 5];
            mic.start();
            boolean outVoice = true;
            while (outVoice) {
                int count = mic.read(tmpBuff, 0, tmpBuff.length);
                if (count > 0) {
                    synchronized (connectionMap) {
                        for (ConnectionInfo conn : connectionMap.values()) {
                            InetAddress address = InetAddress.getByName(conn.hostname);
                            DatagramPacket packet = new DatagramPacket(tmpBuff, count, address, conn.port);
                            ds.send(packet);
                        }
                    }
                }
            }
            mic.drain();
            mic.close();
            System.out.println("Stopped listening from mic.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
