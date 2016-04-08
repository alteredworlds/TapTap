package com.alteredworlds.taptap.data.helper;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import com.alteredworlds.taptap.data.TapTapDataContract;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
            retVal = getStartOfToday();
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

    public static Date getStartOfToday() {
        GregorianCalendar cal = new GregorianCalendar();
        return getStartOfDay(cal);
    }

    public static Date getStartOfYesterday() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);  // set to yesterday
        return getStartOfDay(cal);
    }

    protected static Date getStartOfDay(GregorianCalendar cal) {
        // set to first millisecond of day
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return cal.getTime();
    }
}
