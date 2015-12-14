package com.alteredworlds.taptap.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapDataContract {
    public static final String CONTENT_AUTHORITY = "com.alteredworlds.taptap.data";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DEVICE = "device";
    public static final String PATH_TEMPERATURE = "temperature";

    public static final class DeviceEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DEVICE).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_DEVICE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_DEVICE;

        public static final String TABLE_NAME = "device";

        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_NAME = "name";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TemperatureRecordEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEMPERATURE).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TEMPERATURE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TEMPERATURE;

        public static final String TABLE_NAME = "temperature";

        // foreign key into device table
        public static final String COLUMN_DEVICE_ADDRESS = "device_address";

        // when tap measurement was taken
        public static final String COLUMN_TIMESTAMP = "timestamp";

        // sink can have up to 3 sensors (hot, cold, mix)
        public static final String COLUMN_VALUE0 = "value0";
        public static final String COLUMN_VALUE1 = "value1";
        public static final String COLUMN_VALUE2 = "value2";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getColumnNameForValue(int value) {
            final String retVal;
            switch (value) {
                case 0:
                    retVal = COLUMN_VALUE0;
                    break;
                case 1:
                    retVal = COLUMN_VALUE1;
                    break;
                case 2:
                    retVal = COLUMN_VALUE2;
                    break;
                default:
                    retVal = null;
                    break;
            }
            return retVal;
        }
    }
}
