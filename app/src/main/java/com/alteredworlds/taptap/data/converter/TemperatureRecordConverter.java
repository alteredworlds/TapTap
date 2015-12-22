package com.alteredworlds.taptap.data.converter;

import android.content.ContentValues;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract.TemperatureRecordEntry;
import com.alteredworlds.taptap.util.Primitives;

/**
 * Created by twcgilbert on 14/12/2015.
 */
public class TemperatureRecordConverter {
    // these constants will be moved to TapTapDataContract
    private static final String LOG_TAG = TemperatureRecordConverter.class.getSimpleName();

    public static String describe(ContentValues cv) {
        StringBuilder sb = new StringBuilder();
        if ((null != cv) && (cv.size() > 0)) {
            // Timestamp
            sb.append("Timestamp: ");
            sb.append(cv.get(TemperatureRecordEntry.COLUMN_TIMESTAMP));
            appendValueForKey(sb, cv, TemperatureRecordEntry.COLUMN_VALUE0, "Value:");
            appendValueForKey(sb, cv, TemperatureRecordEntry.COLUMN_VALUE1, "Value1:");
            appendValueForKey(sb, cv, TemperatureRecordEntry.COLUMN_VALUE2, "Value2:");
        }
        return sb.toString();
    }

    private static void appendValueForKey(StringBuilder sb, ContentValues cv, String key, String label) {
        if (cv.containsKey(key)) {
            sb.append("  ");
            sb.append(label);
            sb.append(" ");
            sb.append(cv.get(key));
        }
    }

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
                int tmp = Primitives.unsignedBytesToInt(data[0], data[1], data[2], data[3]);
                // so need to keep value in a (64 bit) long
                long dateTime = tmp & 0xffffffffL;
                cv.put(TemperatureRecordEntry.COLUMN_TIMESTAMP, dateTime);

                // OK, now to read one or more 16 bit temp values
                short tmpShort;
                int numTempValues = tempDataLen / 2;
                for (int tempIdx = 0; tempIdx < numTempValues; tempIdx++) {
                    // read each temperature value
                    int offset = 4 + 2 * tempIdx;
                    int reading = Primitives.unsignedBytesToInt(data[offset], data[offset + 1]);

                    String columnName = TemperatureRecordEntry.getColumnNameForValue(tempIdx);
                    if (null != columnName) {
                        cv.put(columnName, reading);
                    }
                }
            }
        }
        return cv;
    }
}
