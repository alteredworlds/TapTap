package com.alteredworlds.taptap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapContentProviderTests extends DatabaseTestCase {
    private static final String LOG_TAG = TapTapContentProviderTests.class.getSimpleName();

    public void testGetType() {
        // Device
        String type = mContext.getContentResolver().getType(DeviceEntry.CONTENT_URI);
        assertEquals(DeviceEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(DeviceEntry.buildUri(1L));
        assertEquals(DeviceEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadDevice() {
        ContentValues contentValues = TapTapSamplesFactory.createEntryContentValues();
        runInsertReadTest(DeviceEntry.CONTENT_URI, contentValues);
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
}
