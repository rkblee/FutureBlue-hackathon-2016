package broadcastComm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientListener extends Thread {

	public ObjectInputStream ois;

	public ClientListener(Socket socket) {
		super("ClientListener");
		System.out.println("clientlistener");
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			TextPacket packetFromServer;
			while ((packetFromServer = (TextPacket) ois.readObject()) != null) {
				packetFromServer.print();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
