package com.alteredworlds.taptap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alteredworlds.taptap.data.TapTapDataContract;
import com.alteredworlds.taptap.data.TapTapDbHelper;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapDbTests extends DatabaseTestCase {
    private static final String LOG_TAG = TapTapDbTests.class.getSimpleName();

    static long runTableInsertReadTest(SQLiteDatabase db, String table, ContentValues contentValues) {
        // TEST: WRITE to table
        long rowId = db.insert(table, null, contentValues);
        assertTrue(rowId != -1);

        // TEST: READ from table
        Cursor cursor = db.query(
                table,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // TEST: MATCH: did we get back what we put in?
        validateCursor(cursor, contentValues);
        return rowId;
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    public void setUp() {
        if (!removeDatabaseOnce()) {
            // remove any records in database
            deleteAllRecords();
        }
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(TapTapDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new TapTapDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        mContext.deleteDatabase(TapTapDbHelper.DATABASE_NAME);
        TapTapDbHelper dbHelper = new TapTapDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = TapTapSamplesFactory.createEntryContentValues();
        runTableInsertReadTest(db, TapTapDataContract.DeviceEntry.TABLE_NAME, contentValues);

        dbHelper.close();
    }

    // brings our database to an empty state
    protected void deleteAllRecords() {
        TapTapDbHelper dbHelper = new TapTapDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TapTapDataContract.DeviceEntry.TABLE_NAME, null, null);
    }
}
