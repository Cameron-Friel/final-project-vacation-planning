package cs492.vacationplanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cameron on 3/3/18.
 */

public class LocationContractHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location.db";
    private static int DATABASE_VERSION = 1;

    public LocationContractHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SAVED_REPOS_TABLE =
                "CREATE TABLE " + LocationContract.Locations.TABLE_NAME + "(" +
                        LocationContract.Locations._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        LocationContract.Locations.COLUMN_COUNTRY_NAME + " TEXT NOT NULL, " +
                        LocationContract.Locations.COLUMN_LATITUDE + " TEXT NOT NULL, " +
                        LocationContract.Locations.COLUMN_LONGITUDE + " TEXT NOT NULL, " +
                        LocationContract.Locations.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";
        db.execSQL(SQL_CREATE_SAVED_REPOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationContract.Locations.TABLE_NAME + ";");
        onCreate(db);
    }
}
