package com.alteredworlds.taptap;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alteredworlds.taptap.service.BleTapTapService;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 101;

    private BleTapTapService mService;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get BleTapTapService instance
            BleTapTapService.LocalBinder binder = (BleTapTapService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleScanDevices();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mService) {
            mService.stopScanDevices();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BleTapTapService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (null != mService) {
            unbindService(mConnection);
            mService = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_BLUETOOTH:
                if (Activity.RESULT_CANCELED == resultCode) {
                    Log.w(LOG_TAG, "User didn't enable Bluetooth...we're not gonna be doing much");
                } else {
                    Log.i(LOG_TAG, "User enabled Bluetooth");
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void startScanningAfterVerifyingPermission(boolean requestPermissions) {
        if (null == mService) {
            Log.e(LOG_TAG, "Requires BleTapTapService!");
        } else {
            if (!mService.bleIsEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
            } else {
                // we have Bluetooth capability...
                if (!hasLocationPermission()) {
                    // LOCATION permission currently DENIED
                    if (requestPermissions) {
                        // request permission
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION_PERMISSION);
                    }
                } else if (null != mService) {
                    mService.startScanDevices();
                }
            }
        }
    }

    protected boolean hasLocationPermission() {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if ((grantResults.length > 0) && (PackageManager.PERMISSION_GRANTED == grantResults[0])) {
                    // permission was granted, yay!
                    startScanningAfterVerifyingPermission(false);
                } else {
                    // permission denied, boo!
                    Log.w(LOG_TAG, "User denied LOCATION permission!");
                }
                break;
            }

            default:
                break;
        }
    }

    public void toggleScanDevices() {
        if (null != mService) {
            if (!mService.isScanning()) {
                startScanningAfterVerifyingPermission(true);
            } else {
                mService.stopScanDevices();
                // dump set of unique devices found during this scan to log
                mService.listDevices();
            }
        }
    }
}
