package broadcastComm;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientSender extends Thread {
	BlockingQueue<TextPacket> msgQueue;
	ObjectOutputStream out;

	public ClientSender(Socket socket, BlockingQueue<TextPacket> msgQueue) {
		super("ClientSender");
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			this.msgQueue = msgQueue;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			TextPacket packet;
			try {
				packet = msgQueue.take();
				out.writeObject(packet);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void send(TextPacket textPacket) {
		msgQueue.add(textPacket);
	}
}
