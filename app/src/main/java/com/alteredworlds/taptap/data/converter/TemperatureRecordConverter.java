package com.alteredworlds.taptap.data.converter;

import android.content.ContentValues;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;

/**
 * Created by twcgilbert on 14/12/2015.
 */
public class TemperatureRecordConverter {
    // these constants will be moved to TapTapDataContract
    private static final String LOG_TAG = TemperatureRecordConverter.class.getSimpleName();

    public static ContentValues fromByteArray(byte[] data) {
        ContentValues cv = new ContentValues(2);
        if (null != data) {
            // we expect a 32 bit unix time value
            // followed by one or more 16 bit temperature values
            int tempDataLen = data.length - 4;
            if ((data.length < 6) || (0 != tempDataLen % 2)) {
                Log.e(LOG_TAG, "Invalid data packet with length: " + data.length);
            } else {
                // read 32 bit unixtime
                // NOTE: Java doesn't have unsigned int (32 bit)
                int tmp = unsignedBytesToInt(data[0], data[1], data[2], data[3]);
                // so need to keep value in a (64 bit) long
                long dateTime = tmp & 0xffffffffL;
                cv.put(TemperatureRecordEntry.COLUMN_TIMESTAMP, dateTime);
                Log.i(LOG_TAG, "Timestamp: " + dateTime);

                // OK, now to read one or more 16 bit temp values
                short tmpShort;
                int numTempValues = tempDataLen / 2;
                for (int tempIdx = 0; tempIdx < numTempValues; tempIdx++) {
                    // read each temperature value
                    int offset = 4 + 2 * tempIdx;
                    int reading = unsignedBytesToInt(data[offset], data[offset + 1]);

                    String columnName = TemperatureRecordEntry.getColumnNameForValue(tempIdx);
                    if (null != columnName) {
                        cv.put(columnName, reading);
                    }
                    Log.i(LOG_TAG, "Index: " + tempIdx + "  Temperature: " + reading);
                }
            }
        }
        return cv;
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }
}
