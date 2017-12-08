package fi.jamk.student.moveometri;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    protected static final int ACTION_LOGIN = 1;

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
            loginText.setText("Not logged in");
            loginButton.setText("Login");
        }
        else
        {
            Date date = new Date(account.lastModification);
            loginText.setText(account.name+" ("+account.id+") last modified " + date.toString());
            loginButton.setText("Log out");
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connection.logOut();
                    refreshViews();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int code, int result, Intent data) {
        if (code == ACTION_LOGIN) {
            refreshViews();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshViews();

        final TripStorageEngine tse = TripStorageEngine.getInstance(this);
        final Random random = new Random();
        final TextView tv = (TextView) findViewById(R.id.textView2);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Trip t = new Trip();
                t.category = "testtest";
                t.distance = random.nextDouble() * 1500.0;
                t.duration = random.nextDouble() * 3000.0;
                t.timeStamp = new Date();
                tse.addTrip(t);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Trip> trips = tse.getTrips();
                String s = "";
                for (Trip t : trips)
                {
                    s += t.id + " ( " + t.extId + " - " + t.synchronizedTo + " ) : " + t.duration + "\n";
                }
                tv.setText(s);
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tse.synchronize();
            }
        });
    }
}
