package com.alteredworlds.taptap;

import android.content.ContentValues;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapSamplesFactory {
    public static ContentValues createEntryContentValues() {
        ContentValues retVal = new ContentValues();
        retVal.put(DeviceEntry._ID, "1");
        retVal.put(DeviceEntry.COLUMN_ADDRESS, "12:34:56:78");
        retVal.put(DeviceEntry.COLUMN_NAME, "AW_TAP_01");
        return retVal;
    }
}
