package fi.jamk.student.moveometri;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by macodiusmaximus on 12/2/17.
 */

class TripStorageEngine implements TripStorageInterface {
    private static TripStorageEngine instance;

    static TripStorageEngine getInstance(Context context) {
        if (instance != null)
            return instance;
        instance = new TripStorageEngine(context);
        return instance;
    }

    static interface SynchronizationCallback {
        void onSynchronized(boolean success);
    }

    private TripConnection connection;
    private SharedPreferences settings;
    private final String DB_PREFS_NAME = "DB_STATE";

    private long localRevision = 1;
    private long remoteRevision = 0;

    private TripDatabaseProvider db;

    private TripStorageEngine(Context context) {
        connection = TripConnection.getInstance(context);
        db = new TripDatabaseProvider(context);
        settings = context.getSharedPreferences(DB_PREFS_NAME, 0);
        localRevision = settings.getLong("revision", 1);
        if (connection.isLoggedIn())
        {
            synchronize(null);
        }
    }

    public void synchronize(SynchronizationCallback cb) {
        if (synchronizeTask == null) {
            synchronizeTask = new SynchronizeTask();
            synchronizeTask.execute((Void) null);
            synchronizeCallback = cb;
        }
    }

    @Override
    public List<Trip> getTrips() {

        ArrayList<Trip> list = new ArrayList<Trip>();
        final String[] columns = new String[]{"id","category","timestamp","distance","duration", "extId", "synchronized"};
        Cursor cursor = db.getReadableDatabase().query(db.TABLE_TRIPS, columns, null, null, null, null, "timestamp DESC", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Trip trip = new Trip();
                trip.id = cursor.getInt(0);
                trip.category = cursor.getString(1);
                trip.timeStamp = new java.util.Date(cursor.getLong(2));
                trip.distance = cursor.getDouble(3);
                trip.duration = cursor.getDouble(4);
                trip.extId = cursor.getInt(5);
                trip.synchronizedTo = cursor.getInt(6);
                list.add(trip);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

    @Override
    public void addTrip(Trip trip) {
        ContentValues values = new ContentValues();
        values.put("category", trip.category);
        values.put("timestamp", trip.timeStamp.getTime());
        values.put("distance", trip.distance);
        values.put("duration", trip.duration);
        values.put("extId", 0);
        values.put("synchronized", 0);
        db.getWritableDatabase().insert(db.TABLE_TRIPS, null, values);

        localRevision += 1;
        settings.edit().putLong("revision", localRevision).apply();
    }

    @Override
    public void removeTrip(int id) {
        db.getWritableDatabase().delete(db.TABLE_TRIPS, "id = " + id, null);
        localRevision += 1;
        settings.edit().putLong("revision", localRevision).apply();
    }

    @Override
    public long getRevision() {
        return localRevision;
    }

    private SynchronizeTask synchronizeTask;
    private SynchronizationCallback synchronizeCallback = null;

    public class SynchronizeTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            TripConnection.Account account = connection.getAccount();
            SQLiteDatabase writer = db.getWritableDatabase();
            if (account == null)
            {
                writer.delete(db.TABLE_TRIPS, "synchronized != 0", null);
                return false;
            }



            List<Trip> allTrips = getTrips();
            for (Trip t : allTrips)
            {
                if (t.synchronizedTo != 0)
                    continue;
                int ret = connection.addTripSync(t);
                if (ret <= 0)
                {
                    Log.d("TripStorageEngine", "Failed to synchronize trip: "+ connection.getErrorCodeMessage(ret));
                    return null;
                }
                else
                {
                    ContentValues values = new ContentValues();
                    values.put("synchronized", account.id);
                    values.put("extId", ret);
                    writer.update(db.TABLE_TRIPS, values, "id = " + t.id, null);
                }
            }

            writer.delete(db.TABLE_TRIPS, null, null);

            ArrayList<Trip> exTrips = new ArrayList<>();
            int retCode = connection.getTripsSync(exTrips);
            if (retCode <= 0) {
                Log.d("TripStorageEngine", "Failed to synchronize trips: "+connection.getErrorCodeMessage(retCode));
                return null;
            }
            else {
                for (Trip t : exTrips) {
                    if (t.synchronizedTo != account.id) {
                        Log.d("TripStorageEngine", "Account synchronization id mismatch (Trip account id " + t.synchronizedTo + " != current account id " + account.id + ")");
                        continue;
                    }
                    ContentValues values = new ContentValues();
                    values.put("category", t.category);
                    values.put("timestamp", t.timeStamp.getTime());
                    values.put("distance", t.distance);
                    values.put("duration", t.duration);
                    values.put("extId", t.extId);
                    values.put("synchronized", t.synchronizedTo);

                    writer.insert(db.TABLE_TRIPS, null, values);
                }
            }
            localRevision += 1;
            settings.edit().putLong("revision", localRevision).apply();
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean v) {
            Log.d("TripStorageEngine", "Successfully synchronized");
            if (synchronizeCallback != null)
                synchronizeCallback.onSynchronized(v != null ? v : false);
            synchronizeTask = null;
            synchronizeCallback = null;
        }

        @Override
        protected void onCancelled() {
            synchronizeTask = null;
        }
    }

}
