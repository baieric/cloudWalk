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
    // Declare Variable
    Button logout;
    private SensorManager sensorManager;
    private TextView count;
    boolean activityRunning;
    ParseUser currentUser;
    int totalSteps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from singleitemview.xml
        setContentView(R.layout.activity_welcome);
        // Retrieve current user from Parse.com
        currentUser = ParseUser.getCurrentUser();
        totalSteps = currentUser.getNumber(TOTAL_STEPS).intValue();

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
            totalSteps = (int) sensorEvent.values[0];
            count.setText(String.valueOf(totalSteps) + "steps");
            currentUser.put(TOTAL_STEPS,totalSteps);
            currentUser.saveInBackground();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}