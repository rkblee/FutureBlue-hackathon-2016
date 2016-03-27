package datagramAudio;

import java.io.Serializable;

public class ConnectionInfo implements Serializable {

    public String hostname;
    public int port;

    @Override
    public String toString() {
        return hostname + ":" + port;
    }

    public boolean equals(ConnectionInfo ci) {
        return this.hostname.equals(ci.hostname) && this.port == ci.port;
    }
}
