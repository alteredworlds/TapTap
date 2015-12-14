package com.alteredworlds.taptap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract;
import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;
import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapContentProviderTests extends DatabaseTestCase {
    private static final String LOG_TAG = TapTapContentProviderTests.class.getSimpleName();

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    public void setUp() {
        if (!removeDatabaseOnce()) {
            // remove any records in database
            deleteAllRecords();
        }
    }

    public void testGetType() {
        // Device
        String type = mContext.getContentResolver().getType(DeviceEntry.CONTENT_URI);
        assertEquals(DeviceEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(DeviceEntry.buildUri(1L));
        assertEquals(DeviceEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(TemperatureRecordEntry.CONTENT_URI);
        assertEquals(TemperatureRecordEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(TemperatureRecordEntry.buildUri(1L));
        assertEquals(TemperatureRecordEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadDevice() {
        ContentValues contentValues = TapTapSamplesFactory.createDeviceContentValues();
        runInsertReadTest(DeviceEntry.CONTENT_URI, contentValues);
    }

    public void testInsertReadDeviceWithTemperature() {
        ContentValues contentValues = TapTapSamplesFactory.createDeviceContentValues();
        String deviceAddress = contentValues.getAsString(TapTapDataContract.DeviceEntry.COLUMN_ADDRESS);
        runInsertReadTest(DeviceEntry.CONTENT_URI, contentValues);

        ContentValues tempValues = TapTapSamplesFactory.createTemperatureContentValues(deviceAddress);
        runInsertReadTest(TemperatureRecordEntry.CONTENT_URI, tempValues);
    }

    protected long runInsertReadTest(Uri uri, ContentValues testValues) {
        Uri resultUri = mContext.getContentResolver().insert(uri, testValues);
        long retVal = ContentUris.parseId(resultUri);
        Log.i(LOG_TAG, "Inserted for for Uri:" + uri + " row with _ID " + retVal);
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                resultUri,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        validateCursor(cursor, testValues);
        cursor.close();
        return retVal;
    }

    // brings our database to an empty state
    protected void deleteAllRecords() {
        deleteAndTestAllRecordsForURI(TemperatureRecordEntry.CONTENT_URI);
        deleteAndTestAllRecordsForURI(DeviceEntry.CONTENT_URI);
    }

    protected void deleteAndTestAllRecordsForURI(Uri uri) {
        mContext.getContentResolver().delete(
                uri,
                null,
                null);
        Cursor cursor = mContext.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
}
