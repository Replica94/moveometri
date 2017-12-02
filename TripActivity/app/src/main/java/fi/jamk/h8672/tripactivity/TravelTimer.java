package fi.jamk.h8672.tripactivity;


import android.os.Handler;
import android.util.Log;

/**
 * Created by juha-matti on 2.12.2017.
 */

public class TravelTimer extends StopTimer {
    Runnable runnable;

    @Override
    public void StartTimer() {
        super.StartTimer();
        final Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                times.add(new TripEntry("L", "Location..."));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void CheckEntry(TripEntry item) {
        if(item.getTag() == "L") {
            Log.i("TravelTimer", "Found location: " + item.getData());
        }
    }
}
