package fi.jamk.student.moveometri;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    protected static final int ACTION_LOGIN = 1;
    protected static final int ACTION_TRIP = 2;

    //Update all views in the activity to reflect internal database state
    protected void refreshViews() {
        Button loginButton = (Button) findViewById(R.id.button);
        TextView loginText = (TextView) findViewById(R.id.textView);

        final TripConnection connection = TripConnection.getInstance(this);

        TripConnection.Account account = connection.getAccount();
        if (account == null)
        {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(i, ACTION_LOGIN);
                }
            });
            loginText.setText(R.string.notloggedin);
            loginButton.setText(getString(R.string.login));
        }
        else
        {
            Date date = new Date(account.lastModification);
            loginText.setText(getString(R.string.loggedin, account.name));
            loginButton.setText(R.string.logout);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connection.logOut();
                    performSync();
                }
            });
        }

        TripAdapter tadapter = new TripAdapter(this, TripStorageEngine.getInstance(this).getTrips());
        ListView lv = (ListView) findViewById(R.id.tripview);
        if (lv.getAdapter() != null)
        {
            tadapter.notifyDataSetInvalidated();
        }
        lv.setAdapter(tadapter);
        tadapter.notifyDataSetChanged();
    }

    //Updates local database asynchronously and calls refreshViews after that
    public void performSync()
    {
        TripStorageEngine.getInstance(this).synchronize(new TripStorageEngine.SynchronizationCallback() {
            @Override
            public void onSynchronized(boolean success) {
                refreshViews();
            }
        });
    }

    @Override
    public void onActivityResult(int code, int result, Intent data) {
        if (code == ACTION_LOGIN) {
            performSync();
        }
        if (code == ACTION_TRIP) {
            Log.i("MainActivity", "Result received from TripActivity");
            Trip trip = new Trip();
            trip.category = data.getStringExtra("Category");
            //trip.category = ((EditText)findViewById(R.id.category)).getText().toString();
            trip.distance = data.getDoubleExtra("Distance", 0);
            trip.duration = data.getDoubleExtra("Duration", 0);
            trip.timeStamp = new Date(data.getLongExtra("Date", 0));
            Log.i("MainActivity", "Intent results\n"
                    + trip.category + "\n"
                    + trip.timeStamp.toString() + "\n"
                    + trip.distance + "\n"
                    + trip.duration);
            //if (TripConnection.getInstance(this).isLoggedIn())
            TripStorageEngine.getInstance(this).addTrip(trip);
            performSync();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        performSync();

        //Test button for adding new trips
        final TripStorageEngine tse = TripStorageEngine.getInstance(this);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Random random = new Random();
                Trip t = new Trip();
                t.category = "testtest";
                t.distance = random.nextDouble() * 1500.0;
                t.duration = random.nextDouble() * 3000.0;
                t.timeStamp = new Date();
                tse.addTrip(t);
                performSync();
            }
        });

        findViewById(R.id.buttonStartTripActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TripActivity.class);
                i.putExtra("TripStartCategory", ((EditText)findViewById(R.id.category)).getText().toString());
                //startActivity(i);
                startActivityForResult(i, ACTION_TRIP);
            }
        });


    }
}
