package transcendentlabs.com.cloudwalk;

import com.parse.ParseUser;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements SensorEventListener {

    private final String TOTAL_STEPS = "totalSteps";
    private final String STEP_BALANCE = "stepBalance";
    // Declare Variable
    Button logout;
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
        String struser = currentUser.getUsername().toString();

        // Locate TextView in welcome.xml
        TextView txtuser = (TextView) findViewById(R.id.txtuser);

        // Set the currentUser String into TextView
        txtuser.setText("You are logged in as " + struser);

        // Locate Button in welcome.xml
        logout = (Button) findViewById(R.id.logout);

        // Logout Button Click Listener
        logout.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Logout current user
                ParseUser.logOut();
                finish();
            }
        });

        count = (TextView) findViewById(R.id.step_count);
        balance = (TextView) findViewById(R.id.step_balance);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
            count.setText(String.valueOf(totalSteps) + " total steps recorded");
            balance.setText(String.valueOf(stepBalance) + " Step Credits");
            currentUser.put(TOTAL_STEPS, totalSteps);
            currentUser.put(STEP_BALANCE, stepBalance);
            currentUser.saveInBackground();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}