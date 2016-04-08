package com.alteredworlds.taptap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;
import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "taptap.db";
    private static final String LOG_TAG = TapTapDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;

    public TapTapDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // profile table
        final String SQL_CREATE_DEVICE_TABLE = "CREATE TABLE " + DeviceEntry.TABLE_NAME + " (" +
                DeviceEntry._ID + " INTEGER NOT NULL PRIMARY KEY," +
                DeviceEntry.COLUMN_ADDRESS + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                DeviceEntry.COLUMN_NAME + " TEXT );";

        final String SQL_CREATE_TEMPERATURE_TABLE = "CREATE TABLE " + TemperatureRecordEntry.TABLE_NAME + " (" +
                TemperatureRecordEntry._ID + " INTEGER NOT NULL PRIMARY KEY," +
                TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS + " TEXT NOT NULL, " +
                TemperatureRecordEntry.COLUMN_TIMESTAMP + " INTEGER(4) NOT NULL, " +
                TemperatureRecordEntry.COLUMN_VALUE0 + " INTEGER(4), " +
                TemperatureRecordEntry.COLUMN_VALUE1 + " INTEGER(4), " +
                TemperatureRecordEntry.COLUMN_VALUE2 + " INTEGER(4), " +

                // Set up the COLUMN_DEVICE_ADDRESS as a foreign key to device table.
                " FOREIGN KEY (" + TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS + ") REFERENCES " +
                DeviceEntry.TABLE_NAME + " (" + DeviceEntry.COLUMN_ADDRESS + ")" +
                // not really sure if this is necessary but don't want multiple
                // repetitions of the same relationship
                // e.g.: profile '1' linked with attribute '27'
                " UNIQUE (" + TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS + ", "
                + TemperatureRecordEntry.COLUMN_TIMESTAMP + ") ON CONFLICT REPLACE" +
                ");";

        db.execSQL(SQL_CREATE_DEVICE_TABLE);
        db.execSQL(SQL_CREATE_TEMPERATURE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeviceEntry.TABLE_NAME);
        onCreate(db);
    }
}
