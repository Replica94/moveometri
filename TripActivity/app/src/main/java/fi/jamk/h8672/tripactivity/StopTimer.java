package fi.jamk.h8672.tripactivity;

import android.util.Log;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by H8672 on 15.11.2017.
 */

public final class StopTimer extends Timer {
    private Map<String, Object> times;
    boolean stopped;
    long start, end;

    public StopTimer() {
        this.times = new HashMap<String, Object>();
        this.stopped = false;
    }

    @Override
    public void StartTimer() {
        if(recordDate != null){
            Log.i("StopTimer", "Restarting timer");
            times.clear();
            stopped = false;
            duration = 0;
            start = System.currentTimeMillis();
        }
        //Getting time using GregorianCalender did not get current time.
        //recordDate = (Date) GregorianCalendar.getInstance().getTime().clone();
        recordDate = new Date(System.currentTimeMillis());
        Log.i("StopTimer", "Date using system..." + recordDate.getTime());
        times.put("D", new Date(System.currentTimeMillis()));
    }

    @Override
    public void PauseTimer() {
        times.put("P", new Date(System.currentTimeMillis()));
    }

    @Override
    public void StopTimer() {
        //If timer is on pause when timer is stopped, modulus from list size will be 0 and there's no need to add another time.
        if(times.size() % 2 == 1) {
            times.put("D", new Date(System.currentTimeMillis()));
        }
        CalculateDuration();
        Log.i("StopTimer", "Timer stopped and duration calculated");
    }

    @Override
    protected void CalculateDuration() {
        //Required attributes to get total time
        float time = 0;
        Date lastDate = new Date(System.currentTimeMillis());
        boolean paused = false;

        //Go through list of times
        for (Entry<String, Object> item : times.entrySet()) {
            //If item is a pause
            if(item.getKey() == "P") {
                Log.i("StopTimer", "Pause was found");
                paused = !paused;
                lastDate = ((Date) item.getValue());
                continue;
            }

            //Timer on pause
            if(paused) {
                Log.w("StopTimer", "Timer was on pause and there was a time between pauses");
                continue;
            }

            //If item is a Date
            if(item.getKey() == "D") {
                Log.i("StopTimer", "Date was found");
                //Ignore lastTime in first run.
                if (lastDate.before(((Date) item.getValue()))) {
                    time += (float)(((Date) item.getValue()).getTime() - lastDate.getTime());
                }
                lastDate = ((Date) item.getValue());
            }
        }
        Log.i("StopTimer", "Duration was " + time);
        end = System.currentTimeMillis();
        float xtime = (((float)end - start) / 1000);
        Log.i("StopTimer", "XTime value was " + xtime);
        duration = time;
    }
}
