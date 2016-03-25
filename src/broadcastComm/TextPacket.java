package broadcastComm;
import java.io.Serializable;

public class TextPacket implements Serializable {

	public String sender;
	public String message;

	public void print() {
		System.out.println(sender + ": " + message);
	}

}
