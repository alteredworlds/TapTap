package com.alteredworlds.taptap.data.converter;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class BluetoothDeviceConverter {
    public static ContentValues getContentValues(BluetoothDevice device) {
        ContentValues retVal = new ContentValues(2);
        if (null != device) {
            // ADDRESS is a required field
            retVal.put(DeviceEntry.COLUMN_ADDRESS, device.getAddress());
            // NAME is optional
            String name = device.getName();
            if (!TextUtils.isEmpty(name)) {
                retVal.put(DeviceEntry.COLUMN_NAME, name);
            }
        }
        return retVal;
    }

    public static class ColumnIndices {
        public final int colADDRESS;
        public final int colNAME;

        public ColumnIndices(Cursor c) {
            colADDRESS = c.getColumnIndex(DeviceEntry.COLUMN_ADDRESS);
            colNAME = c.getColumnIndex(DeviceEntry.COLUMN_NAME);
        }
    }
}
