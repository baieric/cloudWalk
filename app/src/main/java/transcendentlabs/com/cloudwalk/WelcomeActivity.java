package transcendentlabs.com.cloudwalk;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity implements SensorEventListener {

    private final String TOTAL_STEPS = "totalSteps";
    private final String STEP_BALANCE = "stepBalance";
    // Declare Variable
    TextView logout;
    Button joinNetwork;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    private SensorManager sensorManager;
    private TextView count;
    private TextView balance;

    boolean activityRunning;
    ParseUser currentUser;
    int totalSteps;
    int stepBalance;
    boolean firstSensorEvent = true;
    int firstSensorValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from singleitemview.xml
        setContentView(R.layout.activity_welcome);
        // Retrieve current user from Parse.com

        ActionBar bar = getSupportActionBar();
        Window window = getWindow();
        Util.setActionBarColour(bar, window, this);

        currentUser = ParseUser.getCurrentUser();
        Number savedSteps = currentUser.getNumber(TOTAL_STEPS);
        Number savedBalance = currentUser.getNumber(STEP_BALANCE);
        if (savedSteps == null) {
            totalSteps = 0;
        } else {
            totalSteps = savedSteps.intValue();
        }
        if (savedBalance == null) {
            stepBalance = 0;
        } else{
            stepBalance = savedBalance.intValue();
        }


        // Convert currentUser into String
        String struser = currentUser.getUsername();

        Constants.setUsername(struser);

        // Locate TextView in welcome.xml
        TextView txtuser = (TextView) findViewById(R.id.txtuser);

        // Set the currentUser String into TextView
        txtuser.setText("Welcome, " + struser + "!");

        // Locate Button in welcome.xml
        logout = (TextView) findViewById(R.id.logout);

        // Logout Button Click Listener
        logout.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                ParseUser.logOut();
                Intent intent = new Intent(WelcomeActivity.this, LoginSignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        joinNetwork = (Button) findViewById(R.id.joinNetwork);

        joinNetwork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        WelcomeActivity.this,
                        NetworkActivity.class);
                startActivity(intent);
                finish();
            }
        });
        count = (TextView) findViewById(R.id.step_count);
        balance = (TextView) findViewById(R.id.step_balance);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        startRegistration();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (activityRunning) {
            if (firstSensorEvent) {
                firstSensorValue = (int) sensorEvent.values[0];
                firstSensorEvent = false;
            }
            totalSteps += sensorEvent.values[0] - firstSensorValue;
            stepBalance += sensorEvent.values[0] - firstSensorValue;
            firstSensorValue = (int) sensorEvent.values[0];
            count.setText(String.valueOf(totalSteps));
            balance.setText(String.valueOf(stepBalance));
            currentUser.put(TOTAL_STEPS, totalSteps);
            currentUser.put(STEP_BALANCE, stepBalance);
            currentUser.saveInBackground();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void startRegistration() {

        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);
        }
        Hotspot.configApState(this);

        //  Create a string map containing information about your service.
        Map<String, String> record = new HashMap<String, String>();
        record.put("username", Constants.getUserName());
        record.put("needsConnection", "false");

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
    }

}