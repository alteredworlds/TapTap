package com.alteredworlds.taptap;

import android.content.ContentValues;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;
import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;

import java.util.Date;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapSamplesFactory {
    public static ContentValues createDeviceContentValues() {
        ContentValues retVal = new ContentValues();
        retVal.put(DeviceEntry._ID, "1");
        retVal.put(DeviceEntry.COLUMN_ADDRESS, "12:34:56:78");
        retVal.put(DeviceEntry.COLUMN_NAME, "AW_TAP_01");
        return retVal;
    }

    public static ContentValues createTemperatureContentValues(long id) {
        ContentValues retVal = new ContentValues();
        retVal.put(TemperatureRecordEntry._ID, "1");
        retVal.put(TemperatureRecordEntry.COLUMN_DEVICE_ID, id);
        retVal.put(TemperatureRecordEntry.COLUMN_TIMESTAMP, new Date().getTime());
        retVal.put(TemperatureRecordEntry.COLUMN_VALUE0, 720);
        retVal.put(TemperatureRecordEntry.COLUMN_VALUE1, 128);
        retVal.put(TemperatureRecordEntry.COLUMN_VALUE2, 1023);
        return retVal;
    }
}
