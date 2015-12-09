package com.alteredworlds.taptap;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.alteredworlds.taptap.data.TapTapDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class DatabaseTestCase extends AndroidTestCase {
    private static boolean sDatabaseDeleted;

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            String actualValue;
            if (Cursor.FIELD_TYPE_FLOAT == valueCursor.getType(idx)) {
                // WORKAROUND for Android 'issues' e.g.:
                // https://code.google.com/p/android/issues/detail?id=22219
                // so getString truncates a value that is returned OK by getDouble!
                actualValue = String.valueOf(valueCursor.getDouble(idx));
            } else {
                actualValue = valueCursor.getString(idx);
            }
            assertEquals(expectedValue, actualValue);
        }
        valueCursor.close();
    }

    public boolean removeDatabaseOnce() {
        boolean retVal = false;
        if (!sDatabaseDeleted) {
            // now delete the entire database...
            mContext.deleteDatabase(TapTapDbHelper.DATABASE_NAME);
            sDatabaseDeleted = true;
            retVal = true;
        }
        return retVal;
    }
}
