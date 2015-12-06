package com.alteredworlds.taptap.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by twcgilbert on 05/12/2015.
 */
public class BleTapTapService extends Service {
    private static final String LOG_TAG = BleTapTapService.class.getSimpleName();

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBleScanner;
    private HashMap<String, BluetoothDevice> mDevices = new HashMap<>();

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(LOG_TAG, "onScanResult");
            if (null != result) {
                BluetoothDevice device = result.getDevice();
                if (null == device) {
                    Log.e(LOG_TAG, "Scan result supplied null device...?");
                } else {
                    String key = device.getAddress();
                    if (addDevice(key, device)) {
                        // NOTIFY that we have found a new device
                        Log.d(LOG_TAG, "New device " + key + " detected");
                    } else {
                        Log.d(LOG_TAG, "Device " + key + " already scanned");
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(LOG_TAG, "Scan Failed: " + errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(LOG_TAG, "onBatchScanResults not yet supported");
        }
    };

    private boolean addDevice(String key, BluetoothDevice device) {
        boolean retVal = false;
        if ((null != device) && isValidKey(key) && !mDevices.containsKey(key)) {
            mDevices.put(key, device);
            retVal = true;
        }
        return retVal;
    }

    // we may end up insisting key looks like a valid MAC
    private boolean isValidKey(String key) {
        return !TextUtils.isEmpty(key);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();

        if (null == mBtAdapter) {
            Log.w(LOG_TAG, "Bluetooth Adapter cannot be found!");
        }
    }

    public void startScanDevices() {
        if (null != mBtAdapter) {
            mBleScanner = mBtAdapter.getBluetoothLeScanner();
            if (null == mBleScanner) {

            } else {
                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build();
                mBleScanner.startScan(null, scanSettings, mLeScanCallback);
            }
        }
    }

    public void stopScanDevices() {
        if (null != mBleScanner) {
            mBleScanner.stopScan(mLeScanCallback);
            mBleScanner = null;
        }
    }

    public boolean isScanning() {
        return null != mBleScanner;
    }

    public void listDevices() {
        for (BluetoothDevice device : mDevices.values()) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("Device address: ");
            sb.append(device.getAddress());
            sb.append("  name: ");
            sb.append(device.getName());
            Log.i(LOG_TAG, sb.toString());
        }
    }

    public boolean bleIsEnabled() {
        return (null != mBtAdapter) && mBtAdapter.isEnabled();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BleTapTapService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BleTapTapService.this;
        }
    }
}
