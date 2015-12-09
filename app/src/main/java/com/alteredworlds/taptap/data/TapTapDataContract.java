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
}
