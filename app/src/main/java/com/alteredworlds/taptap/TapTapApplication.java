package com.alteredworlds.taptap;

import android.app.Application;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class TapTapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // we should clear devices cache on startup
        //getContentResolver().delete(TapTapDataContract.DeviceEntry.CONTENT_URI, null, null);
    }
}
