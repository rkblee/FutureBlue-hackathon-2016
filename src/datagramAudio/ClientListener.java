package datagramAudio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class ClientListener extends Thread {
    private DatagramSocket ds;
    private AudioFormat format;

    public ClientListener(AudioFormat format, DatagramSocket listenSocket) throws SocketException {
        super("ClientListener");
        System.out.println("ClientListener");
        this.ds = listenSocket;
        this.format = format;
    }

    @Override
    public void run() {
        try {
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();
            boolean isListening = true;
            while (isListening) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                ds.receive(packet);
                if (packet.getLength() > 0) {
                    System.out.println("Writing to audio output.");
                    speaker.write(packet.getData(), 0, packet.getLength());
                }
            }
            speaker.drain();
            speaker.close();
            System.out.println("Stopped listening to incoming audio.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
