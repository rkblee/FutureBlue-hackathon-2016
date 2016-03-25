package broadcastComm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

public class ServerHelper extends Thread {
	private ObjectInputStream in;
	private BlockingQueue<TextPacket> msgQueue;

	public ServerHelper(ObjectInputStream in, BlockingQueue<TextPacket> msgQueue) {
		super("ServerHelper");
		System.out.println("ServerHelper");
		this.in = in;
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		TextPacket packetFromClient;
		try {
			while ((packetFromClient = (TextPacket) in.readObject()) != null) {
				System.out.println(msgQueue.add(packetFromClient));
				System.out.println("received packet");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
