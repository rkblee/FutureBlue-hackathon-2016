package datagramAudio;

import java.io.Serializable;

public class ConnectionDataPacket implements Serializable {

    public ConnectionDataPacket(ConnectionActionEnum action, int id) {
        this.action = action;
        this.id = id;
    }

    public ConnectionDataPacket(ConnectionActionEnum action, int id, ConnectionInfo info) {
        this.action = action;
        this.id = id;
        this.info = info;
    }

    public ConnectionDataPacket() {
        this.info = new ConnectionInfo();
    }

    public ConnectionInfo info;
    public ConnectionActionEnum action;
    public int id;
}
