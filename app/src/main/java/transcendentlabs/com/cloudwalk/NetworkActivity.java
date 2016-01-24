package transcendentlabs.com.cloudwalk;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkActivity extends AppCompatActivity{

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    PeerArrayAdapter mAdapter;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private ListView mPeerList;
    private ArrayList<Peer> peers = new ArrayList<>();
    final HashMap<String, Peer> allConnections = new HashMap<String, Peer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_network);

        ActionBar bar = getSupportActionBar();
        Window window = getWindow();
        Util.setActionBarColour(bar, window, this);

        Button connect = (Button) findViewById(R.id.connect);

        connect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                if (!Settings.System.canWrite(NetworkActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivity(intent);
                }
                Hotspot.connect(NetworkActivity.this);

            }
        });

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mAdapter = new PeerArrayAdapter(this, R.layout.peer_list_item, peers);
        mPeerList = (ListView) findViewById(R.id.peerList);
        mPeerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Peer peer = (Peer) mPeerList.getItemAtPosition(i);
                Log.e("ERIC", peer.name);
            }
        });
        mPeerList.setAdapter(mAdapter);

        startRegistration();
    }

    private void startRegistration() {
        //  Create a string map containing information about your service.
        Map<String, String> record = new HashMap<String, String>();
        record.put("username", Constants.getUserName());
        record.put("needsConnection", "true");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("CloudWalk", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });

        discoverService();
    }

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Peer peer = new Peer(srcDevice.deviceAddress, txtRecordMap.get("username"), txtRecordMap.get("needsConnection").equals("true"));
                allConnections.put(srcDevice.deviceAddress, peer);
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                if(instanceName.equals("CloudWalk")){
                    if(allConnections.containsKey(resourceType.deviceAddress)) {
                        Peer p = allConnections.get(resourceType.deviceAddress);
                        if(!p.needsConnection)
                        peers.add(p);
                    } else{
                        // TODO handle this error case?
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });
    }
}
