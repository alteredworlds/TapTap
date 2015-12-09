package com.alteredworlds.taptap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;

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

        db.execSQL(SQL_CREATE_DEVICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeviceEntry.TABLE_NAME);
        onCreate(db);
    }
}
