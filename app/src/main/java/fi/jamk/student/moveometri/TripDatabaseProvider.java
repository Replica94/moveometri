package fi.jamk.student.moveometri;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by macodiusmaximus on 12/2/17.
 */

public class TripDatabaseProvider extends SQLiteOpenHelper {

    static public final String TABLE_TRIPS = "trips";

    TripDatabaseProvider(Context context)
    {
        super(context, "TRIPDB", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_TRIPS +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " category TEXT," +
                " timestamp INTEGER," +
                " distance REAL," +
                " duration REAL," +
                " synchronized INTEGER," +
                " extId INTEGER" +
                " );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i != 1 || i1 != i)
            throw new UnsupportedOperationException();
    }
}