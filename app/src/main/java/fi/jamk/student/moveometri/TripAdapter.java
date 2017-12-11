package fi.jamk.student.moveometri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by macodiusmaximus on 12/11/17.
 */

public class TripAdapter extends ArrayAdapter<Trip> {
    public TripAdapter(Context context, List<Trip> trips) {
        super(context,0, trips);
    }

    @Override
    public View getView(int i, View v, ViewGroup p) {
        Trip t = getItem(i);
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.triplayout, p, false);
        }

        TextView cat = (TextView) v.findViewById(R.id.tripview_category);
        TextView date = (TextView) v.findViewById(R.id.tripview_date);
        TextView duration = (TextView) v.findViewById(R.id.tripview_duration);
        TextView distance = (TextView) v.findViewById(R.id.tripview_distance);
        cat.setText(t.category);

        date.setText(getContext().getString(R.string.tripview_date, t.timeStamp));
        duration.setText(getContext().getString(R.string.tripview_duration, new Long((long)(t.duration * 1000))));

        if (t.distance >= 10000)
            distance.setText(getContext().getString(R.string.tripview_distance_km, new Double(t.distance / 1000) ));
        else
            distance.setText(getContext().getString(R.string.tripview_distance_m, new Double(t.distance)));
        return v;
    }
}
