package transcendentlabs.com.cloudwalk;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Eric on 2016-01-23.
 */
public class Peer {
    public String deviceAddress;
    public String name;
    public boolean needsConnection;
    public WifiP2pDevice device;

    public Peer(String deviceAddress, String name, boolean needsConnection, WifiP2pDevice device) {
        this.deviceAddress = deviceAddress;
        this.name = name;
        this.needsConnection = needsConnection;
        this.device = device;
    }

}
