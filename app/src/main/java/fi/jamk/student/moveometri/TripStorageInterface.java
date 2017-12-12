package fi.jamk.student.moveometri;

import java.util.List;

/**
 * Created by macodiusmaximus on 12/2/17.
 */

public interface TripStorageInterface {
    List<Trip> getTrips();
    void addTrip(Trip trip);
    void removeTrip(int id);
    long getRevision();
}
