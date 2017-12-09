package fi.jamk.student.moveometri;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TripActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trip);

        checkPermissions();
        addClickListeners();
        configure();
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

    //Permissions
    private void checkPermissions(){
        boolean[] result = new boolean[] {false, false, false, false};
        String[] perm = {"","","",""};

        //Check permission. if not granted, request that permission.
        if(!(result[0] = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)))  { perm[0] = Manifest.permission.ACCESS_COARSE_LOCATION; }
        if(!(result[1] = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)))    { perm[1] = Manifest.permission.ACCESS_FINE_LOCATION; }
        if(!(result[2] = checkPermission(Manifest.permission.INTERNET)))                { perm[2] = Manifest.permission.INTERNET; }
        if(!(result[3] = checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)))    { perm[3] = Manifest.permission.ACCESS_NETWORK_STATE; }

        if(result[0] && result[1] && result[2] && result[3]) {
            //Permissions granted
            initializeMapValues();
        }
        else {
            uiToast("Missed permission actually?");
            ActivityCompat.requestPermissions(this, perm, 11111);
        }
    }
    private boolean checkPermission(final String permission) {
        if((ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                Log.i("TripActivity", "Trying to get access to " + permission);
                uiToast("Need permission to use " + permission);
            }
        }
        else {
            uiToast("Have permission to " + permission);
            return true;
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 11111) {
            for(int i = 0; i < permissions.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    uiToast(permissions[i] + " permission granted!");
                }
                else {
                    uiToast("No " + permissions[i] + " permission!");
                }
            }
        }
    }

    //Map Sync
    private void configure(){
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                LatLng pos = new LatLng(62.2307, 25.762);
                mMap.addMarker(new MarkerOptions().position(pos).title("JKL"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            }
        });
    }

    //Timer values
    private Timer timer;
    private boolean[] timerRuns;
    private Location currentLocation;
    private GoogleMap mMap;
    private int polylineColor;
    private PolylineOptions path;
    private void setPathColor(final int color) {
        polylineColor = color;
    }
    private void newPath() {
        path = new PolylineOptions().color(polylineColor);
    }
    private void initializeMapValues() {
        currentLocation = new Location(LocationManager.PASSIVE_PROVIDER);
        timerRuns = new boolean[] {false, false};
        polylineColor = Color.MAGENTA;
        newPath();
    }

    //Updates for timer run
    private class TimerRunner {
        //TODO replace start button to some other view to show current trip progress...
        private Button startButton;
        public void setButton(Button btn) {
            startButton = btn;
        }
        public TimerRunner() {}

        private final Handler handler = new Handler();
        private final Runnable runnable = new Runnable() {
            @Override public void run() {
                checkTime();
            }
        };

        public void startRun(){
            handler.post(runnable);
        }

        private void checkTime() {
            //If timer runs, continue posting
            if(timerRuns[0]) {
                handler.postDelayed(runnable, 1000);
            }
            else {
                startButton.setText("Duration: " + timer.GetDuration() + "\n" + startButton.getText() + "\nNew trip?");
                return;
            }

            //If timer isn't stopped
            if(!timerRuns[1]){
                long time = (System.currentTimeMillis()/1000 - timer.recordDate.getTime()/1000);

                //Draw new drawings to the map
                if(currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(
                            new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                    path.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    mMap.addPolyline(path);

                    double distance = (double) pathLength();
                    startButton.setText("Total time: " + time + "\nDistance: " + distance);
                }
                else {
                    startButton.setText("Total time: " + time);
                }
                //Show total time on timer
                if(startButton.getCurrentTextColor() == Color.RED) startButton.setTextColor(Color.BLACK);
            }
            else {
                if(startButton.getCurrentTextColor() != Color.RED) startButton.setTextColor(Color.RED);
            }
        }
    }
    private TimerRunner timerRunner;

    //Buttons
    private void addClickListeners(){
        timerRunner = new TimerRunner();
        timerRunner.setButton((Button) findViewById(R.id.buttonTimerStart));
        Button button = (Button)findViewById(R.id.buttonTimerStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerStarts();
            }
        });

        button = (Button)findViewById(R.id.buttonTimerPause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerPauses();
            }
        });

        button = (Button)findViewById(R.id.buttonTimerStop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerStops();
            }
        });
    }
    private void timerStarts() {
        Log.i("TripActivity", "Timer started");
        Intent intent = new Intent(getApplicationContext(), TravelTimer.class);
        timer = new TravelTimer(getApplicationContext(), currentLocation);
        timer.StartTimer();
        newPath();

        //Set timer true and pause false
        timerRuns[0] = true;
        timerRuns[1] = false;

        timerRunner.startRun();
    }
    private void timerPauses() {
        Log.i("TripActivity", "Timer paused");
        timer.PauseTimer();

        //Set pause
        timerRuns[1] = !timerRuns[1];
    }
    private void timerStops() {
        Log.i("TripActivity", "Timer stopped");
        timer.StopTimer();

        Log.i("TripActivity", "Time duration: " + timer.GetDuration());
        uiToast("Duration " + timer.GetDuration());

        //Clear map only once to show the route
        if(timerRuns[0]) {
            //Draw the route in the map
            newPath();
            ArrayList list = ((TravelTimer) timer).getLocations();
            for (Object item : list) {
                Location loc = (Location) item;
                path.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                //uiToast("Locations " + ((Location) item).toString());
            }

            //Clear the map from old drawings
            if (mMap != null) {
                mMap.clear();
                mMap.addPolyline(path);

                //Add start and end markers
                if (list.size() > 0) {
                    Location start = (Location) list.get(0);
                    Location end = (Location) list.get(list.size() - 1);
                    mMap.addMarker(new MarkerOptions().position(
                            new LatLng(start.getLatitude(), start.getLongitude())).title("Trip start"));
                    mMap.addMarker(new MarkerOptions().position(
                            new LatLng(end.getLatitude(), end.getLongitude())).title("Trip end"));
                }
            }
        }

        //Set booleans to false
        timerRuns[0] = false;
        timerRuns[1] = false;

        //Send result to the main activity
        Intent result = new Intent();
        //TODO categories
        result.putExtra("Category", "TODO categories");
        result.putExtra("Date", timer.recordDate.getTime());
        result.putExtra("Duration", (double) timer.duration);

        double distance = (double) pathLength();
        result.putExtra("Distance", distance);
        setResult(MainActivity.ACTION_TRIP, result);

        uiToast("currentLocation " + currentLocation.toString());
    }

    //Calculate the length of the path
    private float pathLength() {
        LatLng lastpoint = path.getPoints().get(0);
        float traveledistance = 0f;
        for (LatLng item : path.getPoints()) {
            float[] distance = {0f};
            Location.distanceBetween(lastpoint.latitude, lastpoint.longitude, item.latitude, item.longitude, distance);
            traveledistance += (double)distance[0];
            lastpoint = item;
        }
        return traveledistance;
    }

}


