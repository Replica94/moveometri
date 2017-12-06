package fi.jamk.h8672.tripactivity;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by juha-matti on 6.12.2017.
 */

public final class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        pos = new LatLng(62.2307, 25.762);
        mMap.addMarker(new MarkerOptions().position(pos).title("JKL"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    public void addPositionMarker(Location loc, String title){
        mMap.clear();
        pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(pos).title(title));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }
}
