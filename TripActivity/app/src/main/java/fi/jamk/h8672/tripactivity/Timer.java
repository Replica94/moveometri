package fi.jamk.h8672.tripactivity;

import android.app.Activity;
import android.app.Service;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by H8672 on 15.11.2017.
 */

public abstract class Timer {
    protected String category;
    protected Date recordDate;
    protected float duration;

    public float GetDuration(){
        Log.i("Timer", "Returning duration as float");
        //if(duration == 0) return (((float)(System.currentTimeMillis()/100 - recordDate.getTime()/100))/10);
        return duration;
    }
    public String GetDate(){
        Log.i("Timer", "Returning date as string");
        //Transform Date to string format.
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd GG:mm:ss z");
        return df.format(recordDate);
    }

    public abstract void StartTimer();
    public abstract void PauseTimer();
    public abstract void StopTimer();
    protected abstract void CalculateDuration();
}
