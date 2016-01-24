package transcendentlabs.com.cloudwalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private NetworkActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       NetworkActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//             Respond to new connection or disconnections
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                if(!Constants.requester){
                    if (!Settings.System.canWrite(mActivity)) {
                        Intent intent1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        mActivity.startActivity(intent1);
                    }
                    Log.d("BROADCAST", "OPENINGJAIS@@@@@DHAJSDLKAS");
                    Hotspot.configApState(mActivity);
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
//            if (!Settings.System.canWrite(mActivity)) {
//                Intent intent1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                mActivity.startActivity(intent1);
//            }
//            Log.d("BROADCAST", "OPENINGJAIS@@@@@DHAJSDLKAS");
//            Hotspot.configApState(mActivity);
        }
    }
}