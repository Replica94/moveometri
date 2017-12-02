package fi.jamk.h8672.tripactivity;

/**
 * Created by juha-matti on 2.12.2017.
 */

public class TripEntry {
    String tag;
    Object data;

    public TripEntry(String tag, Object data) {
        this.tag = tag;
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public Object getData() {
        return data;
    }
}
