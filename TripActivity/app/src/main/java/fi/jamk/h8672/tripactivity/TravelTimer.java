package fi.jamk.h8672.tripactivity;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

//Not in use...
import android.widget.Toast;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.location.LocationProvider;

import java.util.ArrayList;

import static android.location.LocationManager.GPS_PROVIDER;

//import com.google.android.gms.location.LocationListener;

/**
 * Created by juha-matti on 2.12.2017.
 * About services...
 * https://developer.android.com/guide/components/services.html
 */

public class TravelTimer extends StopTimer {

    //Location requirements
    private LocationManager manager;
    private LocationListener listener;

    private class LocationListener implements android.location.LocationListener {
        //Location LastLocation;

        public LocationListener(String provider) {
            //LastLocation = new Location(provider);
            //Log.i("LocationListener", "LocationListener created for " + LastLocation.getProvider());
            Log.i("LocationListener", "LocationListener created for " + provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i("LocationListener", location.toString());
            //LastLocation.set(location);
            //times.add(new TripEntry("L", new Location(LastLocation)));
            times.add(new TripEntry("L", new Location(location)));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.i("LocationListener", "StatusChanged: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i("LocationListener", "ProviderEnabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.i("LocationListerner", "ProviderDisabled: " + s);
        }
    }

    @SuppressLint("MissingPermission")
    //Constructor
    public TravelTimer(final Context context) {
        super();
        Log.i("TravelTimer", "Initialized");

        //Start custom location listener
        manager = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        listener = new LocationListener(GPS_PROVIDER);
        manager.requestLocationUpdates(GPS_PROVIDER, 1000, 0.1f, listener);

        //Get latest location
        //Location loc = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        //Log.i("TravelTimer", "Location " + loc.toString());
        //Toast.makeText(context, loc.toString(), Toast.LENGTH_SHORT).show();
    }

    //StopTimer overrides, add new check.
    @Override
    protected void CheckEntry(TripEntry item) {
        if(item.getTag() == "L") {
            Log.i("TravelTimer", ((Location)item.getData()).toString());
        }
    }

    //New methods
    public ArrayList getLocations(){
        //Stop updating location
        manager.removeUpdates(listener);

        //Return all locations
        ArrayList list = new ArrayList();
        for(TripEntry item : times){
            if(item.getTag() == "L") list.add(item.getData());
        }
        return list;
    }


    //manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        /*
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                if (traveling) {
                    //Add new location every time temp is 0 to make this loop to break faster
                    if (temp == 0) {
                        //Get location using location provider if it exists

                        try {
                            //Location location = new Location(manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
                            times.add(new TripEntry("L", listeners[0].LastLocation));
                        } catch (Exception e) {
                            //Toast.makeText(context, "No location provider!", Toast.LENGTH_SHORT).show();
                            Log.i("TravelTimer", "No location provider\n" + e.getMessage());
                        }
                    }
                    temp++;
                    if (temp >= 50) temp = 0;
                    handler.postDelayed(this, 100);
                }
            }
        };
        this.traveling = false;
        this.temp = 0;
        */
    //Toast.makeText(context, "Toest test!", Toast.LENGTH_LONG).show();



/*
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager != null) {
            for(LocationListener item : listeners) {
                try {
                    manager.removeUpdates(item);
                } catch (Exception e) {
                    Log.i("TravelTimer", "onDestroy() " + e.getMessage());
                }
            }
        }
    }
*/
    /*
    //Service requirements
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // Check location provider then check command
            // Maybe send intent to maps service tell the progress like to
            // mark the position and return current duration in real time...
            switch (msg.arg1) {
                case 0: {

                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }
*/
    /*
    //Service creation
    @Override
    public void onCreate() {
        Log.i("TravelTimer", "Creating service");
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        listeners = new LocationListener[]{
                new LocationListener(LocationManager.PASSIVE_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER),
                new LocationListener(GPS_PROVIDER)
        };

        manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    //Listener values to update requirements.
    private static int TIME_TO_UPDATE = 100;
    private static float MIN_DISTANCE = 0.1f;

    //Service start
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();
        Log.i("TravelTimer Service", "Starting");

        //TODO activate listeners, let listeners to send messages to update locations
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Travel service didn't start", Toast.LENGTH_SHORT).show();

            return START_NOT_STICKY;
        }
        try {
            manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                    TIME_TO_UPDATE, MIN_DISTANCE, listeners[0]);
            //Location loc = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (Exception e) {
            Log.i("TravelTimer PASSIVE", e.getMessage());
            throw e;
        }
        try {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    TIME_TO_UPDATE, MIN_DISTANCE, listeners[1]);
        } catch (Exception e) {
            Log.i("TravelTimer NETWORK", e.getMessage());
            throw e;
        }
        try {
            manager.requestLocationUpdates(GPS_PROVIDER,
                    TIME_TO_UPDATE, MIN_DISTANCE, listeners[2]);
        } catch (Exception e) {
            Log.i("TravelTimer GPS", e.getMessage());
            throw e;
        }
        Log.i("TravelTimer", "Created requests to update locations");
        return START_STICKY;
    }
    */
}
