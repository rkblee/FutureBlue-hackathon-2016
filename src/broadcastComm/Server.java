package broadcastComm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		boolean listening = true;
		List<ObjectOutputStream> outStreamList = Collections
				.synchronizedList(new ArrayList<ObjectOutputStream>());
		BlockingQueue<TextPacket> msgQueue = new LinkedBlockingQueue<TextPacket>();

		new ServerBroadcastThread(outStreamList, msgQueue).start();

		try {
			serverSocket = new ServerSocket(8080);
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port!");
			System.exit(-1);
		}

		while (listening) {
			Socket socket = serverSocket.accept();
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			synchronized (outStreamList) {
				outStreamList.add(out);
			}
			new ServerHelper(in, msgQueue).start();
		}

		serverSocket.close();
	}
}
