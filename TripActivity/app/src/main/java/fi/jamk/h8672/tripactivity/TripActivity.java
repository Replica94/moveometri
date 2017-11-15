package fi.jamk.h8672.tripactivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TripActivity extends AppCompatActivity {
    Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        timer = new StopTimer();

        addClickListeners();

    }

    private void addClickListeners(){
        Button button = (Button)findViewById(R.id.buttonTimerStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TripActivity", "Timer started");
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
        button
                = (Button)findViewById(R.id.buttonTimerStop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TripActivity", "Timer stopped");
                timer.StopTimer();
                Log.i("TripActivity", "Time duration: " + timer.GetDuration());
            }
        });
    }


}
