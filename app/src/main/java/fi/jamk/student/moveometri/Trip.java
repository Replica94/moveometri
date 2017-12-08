package fi.jamk.student.moveometri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by macodiusmaximus on 12/2/17.
 */

public class Trip {
    public int id = 0;

    public String category;
    public Date timeStamp;
    public double distance = 0;
    public double duration = 0;

    public int synchronizedTo = 0;
    public int extId = 0;

    public long synchronizationTime = 0;

    public  Trip() {
    }

    //Constructor from REST api JSON
    public Trip(JSONObject j) throws JSONException {

        category = j.getString("category");
        timeStamp = new Date(j.getLong("timeStamp"));
        duration = j.getDouble("duration");
        distance = j.getDouble("distance");

        synchronizedTo = j.getInt("accountId");
        extId = j.getInt("id");

        synchronizationTime = j.getLong("synchronizationTime");
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("category", category);
        json.put("timeStamp", timeStamp.getTime());
        json.put("distance", distance);
        json.put("duration", duration);
        return json;

    }
}