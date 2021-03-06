package com.alteredworlds.taptap.data.helper;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import com.alteredworlds.taptap.data.TapTapDataContract;
import com.alteredworlds.taptap.util.DateHelper;

import java.util.Date;

/**
 * Created by twcgilbert on 08/04/2016.
 */
public class TapTapDataHelper {
    public static Date getMaxRecordedDateToday(Context context, String deviceAddress) {
        Date retVal = getMaxRecordedDate(context, deviceAddress);
        if ((null != retVal) && !DateUtils.isToday(retVal.getTime())) {
            // if not today, dump it so we calculate start of day below
            retVal = null;
        }
        if (null == retVal) {
            // OK, we don't have a valid datetime for today, so create one at start of day
            retVal = DateHelper.getStartOfToday();
        }
        return retVal;
    }

    public static Date getMaxRecordedDate(Context context, String deviceAddress) {
        Date retVal = null;
        Cursor cursor = context.getContentResolver().query(
                TapTapDataContract.TemperatureRecordEntry.buildUriForAddress(deviceAddress),
                new String[]{"MAX(" + TapTapDataContract.TemperatureRecordEntry.COLUMN_TIMESTAMP + ") AS max_timestamp"},
                null,  // selection
                null,  // selectionArgs
                null); // sortOrder
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                // we only expect one record with one column
                long millis = cursor.getLong(0) * 1000L;
                retVal = new Date(millis);
            }
            cursor.close();
        }
        return retVal;
    }
}
