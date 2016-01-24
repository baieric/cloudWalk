package transcendentlabs.com.cloudwalk;

/**
 * Created by Eric on 2016-01-23.
 */
public class Peer {
    public String deviceAddress;
    public String name;
    public boolean needsConnection;

    public Peer(String deviceAddress, String name, boolean needsConnection) {
        this.deviceAddress = deviceAddress;
        this.name = name;
        this.needsConnection = needsConnection;
    }

}
