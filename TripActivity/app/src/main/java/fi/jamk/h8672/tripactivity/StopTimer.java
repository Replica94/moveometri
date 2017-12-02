package fi.jamk.h8672.tripactivity;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by H8672 on 15.11.2017.
 */

public class StopTimer extends Timer {
    protected ArrayList<TripEntry> times;
    boolean stopped;
    Date end;

    public StopTimer() {
        this.times = new ArrayList<TripEntry>();
        this.stopped = false;
    }

    @Override
    public void StartTimer() {
        if(recordDate != null){
            Log.i("StopTimer", "Restarting timer");
            times.clear();
            stopped = false;
            duration = 0;
        }
        recordDate = new Date(System.currentTimeMillis());
        Log.i("StopTimer", "Date using system..." + recordDate.getTime());
        times.add(new TripEntry("D", new Date(System.currentTimeMillis())));
    }

    @Override
    public void PauseTimer() {
        times.add(new TripEntry("P", new Date(System.currentTimeMillis())));
    }

    @Override
    public void StopTimer() {
        //If timer is on pause when timer is stopped, modulus from list size will be 0 and there's no need to add another time.
        if(times.size() % 2 == 1) {
            times.add(new TripEntry("D", new Date(System.currentTimeMillis())));
        }
        CalculateDuration();
        Log.i("StopTimer", "Timer stopped and duration calculated");
    }

    protected void CheckEntry(TripEntry item) {}

    @Override
    protected void CalculateDuration() {
        //Required attributes to get total time
        float time = 0;
        Date lastDate = new Date(System.currentTimeMillis());
        boolean paused = false;

        //Go through list of times
        for (TripEntry item : times) {
            //If item is a pause
            if(item.getTag() == "P") {
                Log.i("StopTimer", "Pause was found");
                paused = !paused;
                if(paused) {
                    time += (float) (((Date) item.getData()).getTime()/100 - lastDate.getTime()/100);
                }
                lastDate = ((Date) item.getData());
                continue;
            }

            //Timer on pause
            if(paused) {
                Log.w("StopTimer", "Timer was on pause and there was a time between pauses");
                continue;
            }
            else {
                //If item is a Date
                if (item.getTag() == "D") {
                    Log.i("StopTimer", "Date was found");
                    //Ignore lastTime in first run.
                    if (lastDate.before(((Date) item.getData()))) {
                        time += (float) (((Date) item.getData()).getTime()/100 - lastDate.getTime()/100);
                    }
                    lastDate = ((Date) item.getData());
                }
                else {
                    CheckEntry(item);
                }
            }
        }
        time = time / 10;
        Log.i("StopTimer", "Duration was " + time + " and there is " + times.size() + " records");
        end = new Date(System.currentTimeMillis());
        float xtime = (((float)(end.getTime()/100 - recordDate.getTime()/100))/10);
        Log.i("StopTimer", "XTime value was " + xtime);
        duration = time;
    }
}
