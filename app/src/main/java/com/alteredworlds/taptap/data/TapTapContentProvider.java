package com.alteredworlds.taptap.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;
import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;
import com.alteredworlds.taptap.data.converter.TemperatureRecordConverter;

/**
 * Created by twcgilbert on 09/12/2015.
 */
@SuppressWarnings("ConstantConditions")
// gets rid of getContext().getContentResolver() null warning
public class TapTapContentProvider extends ContentProvider {
    private static final String LOG_TAG = TapTapContentProvider.class.getSimpleName();

    private static final int DEVICE = 100;
    private static final int DEVICE_ID = 101;

    private static final int TEMPERATURE = 200;
    private static final int TEMPERATURE_ID = 201;
    private static final int TEMPERATURE_ADDRESS = 202;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TapTapDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher retVal = new UriMatcher(UriMatcher.NO_MATCH);

        // all Devices
        retVal.addURI(
                TapTapDataContract.CONTENT_AUTHORITY,
                TapTapDataContract.PATH_DEVICE,
                DEVICE);

        // a specific Device (by ID)
        retVal.addURI(
                TapTapDataContract.CONTENT_AUTHORITY,
                TapTapDataContract.PATH_DEVICE + "/#",
                DEVICE_ID);

        // all Temperatures
        retVal.addURI(
                TapTapDataContract.CONTENT_AUTHORITY,
                TapTapDataContract.PATH_TEMPERATURE,
                TEMPERATURE);

        // a specific Temperature (by ID)
        retVal.addURI(
                TapTapDataContract.CONTENT_AUTHORITY,
                TapTapDataContract.PATH_TEMPERATURE + "/#",
                TEMPERATURE_ID);

        // set of Temperatures for Device Address
        retVal.addURI(
                TapTapDataContract.CONTENT_AUTHORITY,
                TapTapDataContract.PATH_TEMPERATURE + "/*",
                TEMPERATURE_ADDRESS);

        return retVal;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new TapTapDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // "device"
            case DEVICE: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DeviceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "device/#"
            case DEVICE_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);

                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(DeviceEntry.TABLE_NAME);
                queryBuilder.appendWhere(DeviceEntry._ID + " = " + id);

                retCursor = queryBuilder.query(
                        mDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "temperature"
            case TEMPERATURE: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        TemperatureRecordEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "temperature/#"
            case TEMPERATURE_ID: {
                // extract ID and add a where clause
                long id = ContentUris.parseId(uri);

                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(TemperatureRecordEntry.TABLE_NAME);
                queryBuilder.appendWhere(TemperatureRecordEntry._ID + " = " + id);

                retCursor = queryBuilder.query(
                        mDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "temperature/*"
            case TEMPERATURE_ADDRESS: {
                // extract Address and add a where clause
                String address = TemperatureRecordEntry.getAddress(uri);

                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables(TemperatureRecordEntry.TABLE_NAME);
                queryBuilder.appendWhere(TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS + " = '" + address + "'");

                retCursor = queryBuilder.query(
                        mDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        if (null != retCursor) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri retVal;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DEVICE: {
                long _id = db.insert(DeviceEntry.TABLE_NAME, null, values);
                if (_id >= 0) {
                    retVal = DeviceEntry.buildUri(_id);
                    Log.d(LOG_TAG, "Inserted new device");
                } else {
                    throw new SQLException("Failed to insert row into " + DeviceEntry.TABLE_NAME);
                }
            }
            break;

            case TEMPERATURE: {
                long _id = db.insert(TemperatureRecordEntry.TABLE_NAME, null, values);
                if (_id >= 0) {
                    retVal = TemperatureRecordEntry.buildUri(_id);
                    Log.d(LOG_TAG, "Inserted new temperature " + TemperatureRecordConverter.describe(values));
                } else {
                    throw new SQLException("Failed to insert row into " + TemperatureRecordEntry.TABLE_NAME);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRows;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DEVICE:
                numRows = db.delete(DeviceEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TEMPERATURE:
                numRows = db.delete(TemperatureRecordEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if ((null == selection) || (numRows > 0)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRows;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DEVICE:
                numRows = db.update(DeviceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TEMPERATURE:
                numRows = db.update(TemperatureRecordEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if ((null == selection) || (0 != numRows)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRows;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        String retVal;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DEVICE:
                retVal = DeviceEntry.CONTENT_TYPE;
                break;

            case DEVICE_ID:
                retVal = DeviceEntry.CONTENT_ITEM_TYPE;
                break;

            case TEMPERATURE:
                retVal = TemperatureRecordEntry.CONTENT_TYPE;
                break;

            case TEMPERATURE_ID:
                retVal = TemperatureRecordEntry.CONTENT_ITEM_TYPE;
                break;

            case TEMPERATURE_ADDRESS:
                retVal = TemperatureRecordEntry.CONTENT_TYPE;
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        return retVal;
    }
}
