package broadcastComm;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ServerBroadcastThread extends Thread {
	private List<ObjectOutputStream> outStreamList;
	private BlockingQueue<TextPacket> msgQueue;

	public ServerBroadcastThread(List<ObjectOutputStream> outStreamList,
			BlockingQueue<TextPacket> msgQueue) {
		super("ServerBroadcastThread");
		System.out.println("broadcast thread");
		this.outStreamList = outStreamList;
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		while (true) {
			TextPacket msg;
			try {
				msg = msgQueue.take();
				System.out.println(msg.message);
				List<ObjectOutputStream> removeList = new LinkedList<ObjectOutputStream>();
				synchronized (outStreamList) {
					for (ObjectOutputStream os : outStreamList) {
						try {
							os.writeObject(msg);
						} catch (IOException e) {
							removeList.add(os);
						}
					}
					outStreamList.removeAll(removeList);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
