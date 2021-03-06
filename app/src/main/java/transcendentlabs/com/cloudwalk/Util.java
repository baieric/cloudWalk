package transcendentlabs.com.cloudwalk;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Eric on 2016-01-23.
 */
public class Util {
    public static final String DEFAULT_COLOUR = "#49a9f5"; // light blue

    public static void setActionBarColour(ActionBar bar, Window window, Activity activity){
        setActionBarColour(bar, window, activity, DEFAULT_COLOUR);
    }

    public static void setActionBarColour(ActionBar bar, Window window, Activity activity, String colour){
        if(bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(colour)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                Bitmap icon = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);

                ActivityManager.TaskDescription taskDescription =
                        new ActivityManager.TaskDescription(
                                activity.getString(R.string.app_name),
                                icon,
                                Color.parseColor(colour));
                activity.setTaskDescription(taskDescription);

                float[] hsv = new float[3];
                int darkerColour = Color.parseColor(colour);
                Color.colorToHSV(darkerColour, hsv);
                hsv[2] *= 0.8f; // value component
                darkerColour = Color.HSVToColor(hsv);
                window.setStatusBarColor(darkerColour);
            }
        }
    }
}
