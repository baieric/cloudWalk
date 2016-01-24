package transcendentlabs.com.cloudwalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class NetworkActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener{

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    PeerArrayAdapter mAdapter;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private ListView mPeerList;
    private ArrayList<Peer> peers = new ArrayList<>();
    final HashMap<String, Peer> allConnections = new HashMap<String, Peer>();
    Handler mServiceBroadcastingHandler = new Handler();
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    Context context;
    boolean sentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_network);

        ActionBar bar = getSupportActionBar();
        Window window = getWindow();
        Util.setActionBarColour(bar, window, this);

        Intent intent = getIntent();

        sentRequest = intent.getBooleanExtra("request", true);

        context = this;

        Button hotspot = (Button) findViewById(R.id.hotspot);

        hotspot.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                if (!Settings.System.canWrite(NetworkActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivity(intent);
                }
                // Hotspot.configApState(NetworkActivity.this);

            }
        });

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



        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mAdapter = new PeerArrayAdapter(this, R.layout.peer_list_item, peers);
        mPeerList = (ListView) findViewById(R.id.peerList);

        mPeerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Constants.requester = true;
                Peer peer = (Peer) mPeerList.getItemAtPosition(i);
                Log.e("ERIC", peer.name);
                //obtain a peer from the WifiP2pDeviceList
                WifiP2pDevice device = peer.device;
                WifiP2pConfig config = new WifiP2pConfig();

                config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        //success logic
                    }

                    @Override
                    public void onFailure(int reason) {
                        //failure logic
                    }
                });
            }
        });
        mPeerList.setAdapter(mAdapter);

        startRegistration();
    }

    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int error) {
                }
            });
            mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, 1000);
        }
    };

    public void startBroadcastingService(final WifiP2pDnsSdServiceInfo serviceInfo){
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.addLocalService(mChannel, serviceInfo,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // service broadcasting started
                                mServiceBroadcastingHandler
                                        .postDelayed(mServiceBroadcastingRunnable,
                                                1000);
                            }

                            @Override
                            public void onFailure(int error) {
                                // react to failure of adding the local service
                            }
                        });
            }

            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
            }
        });
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

        startBroadcastingService(serviceInfo);

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
                boolean needsConnection = true; // TODO set this properly later
                Peer peer = new Peer(srcDevice.deviceAddress, txtRecordMap.get("username"), needsConnection, srcDevice);
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
//                    if(allConnections.containsKey(resourceType.deviceAddress)) {
//                        Peer p = allConnections.get(resourceType.deviceAddress);
//                        peers.add(p);
//                    } else{
//                        // TODO handle this error case?
//                    }
//                    mAdapter.notifyDataSetChanged();
                    Log.e("Device Found!", " sentRequest = " + (sentRequest ? "true" : "false"));
                    if(sentRequest) {
                        mServiceBroadcastingHandler.postDelayed(mHotspotRunnable, 1000);
                    } else{
                        if (!Settings.System.canWrite(NetworkActivity.this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            startActivity(intent);
                        }
                        Hotspot.configApState(context);
                    }
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        startServiceDiscovery();
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            startServiceDiscovery();
        }
    };

    private Runnable mHotspotRunnable = new Runnable() {
        @Override
        public void run() {
            Hotspot.connect(context);
        }
    };

    private void startServiceDiscovery() {
        mManager.removeServiceRequest(mChannel, serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mManager.addServiceRequest(mChannel, serviceRequest,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        mManager.discoverServices(mChannel,
                                                new WifiP2pManager.ActionListener() {

                                                    @Override
                                                    public void onSuccess() {
                                                        //service discovery started
                                                        Handler mServiceDiscoveringHandler = new Handler();
                                                        mServiceDiscoveringHandler.postDelayed(
                                                                mServiceDiscoveringRunnable,
                                                                1000);
                                                    }

                                                    @Override
                                                    public void onFailure(int error) {
                                                        // react to failure of starting service discovery
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(int error) {
                                        // react to failure of adding service request
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int reason) {
                        // react to failure of removing service request
                    }
                });
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    public void peersFound(WifiP2pDeviceList peerList){
        final TextView peersFound = (TextView) findViewById(R.id.findPeers);
        Collection<WifiP2pDevice> peers = peerList.getDeviceList();
        if(peers.size() > 0){
            final WifiP2pDevice device = peers.iterator().next();
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    peersFound.setText("Connected to" + device.deviceName);
                }

                @Override
                public void onFailure(int reason) {
                    peersFound.setText("Failure to connect to peer");
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
//        if(info.groupFormed && !info.isGroupOwner) {
//            if (!Settings.System.canWrite(NetworkActivity.this)) {
//                Intent intent1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                NetworkActivity.this.startActivity(intent1);
//            }
//            Log.d("BROADCAST", "OPENINGJAIS@@@@@DHAJSDLKAS");
//            Hotspot.configApState(NetworkActivity.this);
//        }
//
    }
}
