import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import broadcastComm.ClientListener;
import broadcastComm.ClientSender;
import broadcastComm.TextPacket;

public class Client {
	public static void main(String args[]) throws IOException {

		String sender = JOptionPane.showInputDialog("name");
		Socket socket = null;
		BlockingQueue<TextPacket> msgQueue = new LinkedBlockingQueue<TextPacket>();

		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 8080;
			socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}

		new ClientSender(socket, msgQueue).start();
		new ClientListener(socket).start();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));
		String userInput;

		while ((userInput = stdIn.readLine()) != null) {
			TextPacket packetToServer = new TextPacket();
			packetToServer.message = userInput;
			packetToServer.sender = sender;
			msgQueue.add(packetToServer);
		}

	}
}
