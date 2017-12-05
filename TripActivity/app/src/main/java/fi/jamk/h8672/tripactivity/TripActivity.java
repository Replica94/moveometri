package fi.jamk.h8672.tripactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//Unused imports
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.os.Debug;
import android.os.IBinder;
import android.renderscript.RenderScript;

import java.util.ArrayList;

public class TripActivity extends AppCompatActivity {
    //As static the data is saved even if you go to the desktop or rotate view while it runs.
    static Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        checkPermission();

        addClickListeners();

        //TODO update textfield with current duration of the timer
        //TODO add map view to the app
        //TODO add current location to the map view
        //TODO when timer is stopped, draw markers and lines to show approximate path that was used
    }

    private void checkPermission(){
        //Looper.prepare();

        if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                Log.i("TripActivity", "Have trying to get access to Coarse location");
                uiToast("Need permission to use coarse location");
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 11111);
            }
        }
        else { uiToast("Have perm to coarse"); }

        if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i("TripActivity", "Have trying to get access to Fine location");
                uiToast("Need permission to use fine location");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11112);
            }
        }
        else { uiToast("Have perm to fine"); }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 11111: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //addClickListeners();
                    uiToast("Coarse Locations permission granted!");
                    //Log.i("TripActivity", "Location permissions granted!");
                }
                else {
                    uiToast("No permission to use coarse locations!");
                    //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11111);
                }
                return;
            }
            case 11112: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //addClickListeners();
                    uiToast("Fine Locations permission granted!");
                    //Log.i("TripActivity", "Location permissions granted!");
                }
                else {
                    uiToast("No permission to use fine locations!");
                    //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 11112);
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addClickListeners(){
        Button button = (Button)findViewById(R.id.buttonTimerStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TripActivity", "Timer started");
                Intent intent = new Intent(getApplicationContext(), TravelTimer.class);
                timer = new TravelTimer(getApplicationContext());
                timer.StartTimer();
            }
        });

        button = (Button)findViewById(R.id.buttonTimerPause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TripActivity", "Timer paused");
                timer.PauseTimer();
            }
        });
        button = (Button)findViewById(R.id.buttonTimerStop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TripActivity", "Timer stopped");
                timer.StopTimer();
                Log.i("TripActivity", "Time duration: " + timer.GetDuration());
                uiToast("Duration " + timer.GetDuration());
                ArrayList list = ((TravelTimer) timer).getLocations();
                for (Object item : list){
                    uiToast("Locations " + ((Location) item).toString());
                }
            }
        });
    }

    private void uiToast(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("TripActivity Error", "Toast failed");
                }
            }
        });
    }

}

/*
else
{
    //No permission to access location.
    ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    }, 11112);
}
*/

/*PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
Notification notification =
        new Notification.Builder(getApplicationContext())
        .setContentTitle("Service")
        .setContentText("Testing service")
        //.setSmallIcon(R.drawable.icon)
        .setContentIntent(pendingIntent)
        .build();
startForegroundService(notification);
*/
//startService(intent);
