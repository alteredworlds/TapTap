package com.alteredworlds.taptap.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alteredworlds.taptap.data.TapTapDataContract;
import com.alteredworlds.taptap.data.converter.BluetoothDeviceConverter;
import com.alteredworlds.taptap.data.converter.TemperatureRecordConverter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by twcgilbert on 05/12/2015.
 */
public class BleTapTapService extends Service {
    public final static UUID TX_CHAR_UUID = UUID.fromString("713d0003-503e-4c75-ba94-3148f18d941e");
    public final static UUID RX_CHAR_UUID = UUID.fromString("713d0002-503e-4c75-ba94-3148f18d941e");
    public final static UUID RX_SERVICE_UUID = UUID.fromString("713d0000-503e-4c75-ba94-3148f18d941e");

    public final static String EXTRA_DATA = "awUART.EXTRA_DATA";

    private static final String LOG_TAG = BleTapTapService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final int SCAN_STOP_MSG_ID = 12345;
    private static final int SCAN_TIMEOUT_MS = 2000;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private final Object mLock = new Object();
    // local handler
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private HashSet<String> mKnownDevices = new HashSet<>();

    private int mConnectionState = STATE_DISCONNECTED;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = TapGattAttributes.ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(LOG_TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(LOG_TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = TapGattAttributes.ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(LOG_TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(LOG_TAG, "mBluetoothGatt = " + mBluetoothGatt);
                broadcastUpdate(TapGattAttributes.ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristicDataAvailable(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            characteristicDataAvailable(gatt, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(TapGattAttributes.ACTION_GATT_RSSI, rssi);
            } else {
                Log.w(LOG_TAG, "onReadRemoteRssi received: " + status);
            }
        }
    };

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (null != result) {
                BluetoothDevice device = result.getDevice();
                if (null == device) {
                    Log.e(LOG_TAG, "Scan result supplied null device...?");
                } else {
                    addDevice(device);
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

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final byte[] data) {
        final Intent intent = new Intent(action);
        if (null != data) {
            intent.putExtra(EXTRA_DATA, data);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void characteristicDataAvailable(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic) {
        final byte[] data;
        if (RX_CHAR_UUID.equals(characteristic.getUuid())) {
            // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
            data = characteristic.getValue();
            // retrieve temperature record information from byte[] buffer
            ContentValues cv = TemperatureRecordConverter.fromByteArray(data);
            if (cv.size() > 0) {
                cv.put(TapTapDataContract.TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS,
                        mBluetoothDeviceAddress);
                // now add to our data store
                getContentResolver().insert(
                        TapTapDataContract.TemperatureRecordEntry.CONTENT_URI,
                        cv);
            }
        } else {
            data = null;
        }
        broadcastUpdate(TapGattAttributes.ACTION_DATA_AVAILABLE, data);
    }


    private boolean addDevice(BluetoothDevice device) {
        boolean retVal = false;
        if (null != device) {
            String key = device.getAddress();
            if (!TextUtils.isEmpty(key) && !mKnownDevices.contains(key)) {
                // add to our KnownDevices (fast lookup) set
                mKnownDevices.add(key);

                // now add to our data store
                getContentResolver().insert(
                        TapTapDataContract.DeviceEntry.CONTENT_URI,
                        BluetoothDeviceConverter.getContentValues(device));

                retVal = true;

                Log.d(LOG_TAG, "Found Device " + key + " " + (TextUtils.isEmpty(device.getName()) ? "" : device.getName()));
            }
        }
        return retVal;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular case, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new BleTapServiceHandler(this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (null == mBluetoothAdapter) {
            Log.w(LOG_TAG, "Bluetooth Adapter cannot be found!");
        }
    }

    public void clearAllDevices() {
        getContentResolver().delete(TapTapDataContract.DeviceEntry.CONTENT_URI, null, null);
        mKnownDevices.clear();
    }

    public void startScanDevices() {
        // clear all recorded devices & start afresh...
        clearAllDevices();
        synchronized (mLock) {
            if (null != mBluetoothAdapter) {
                mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (null == mBleScanner) {
                    Log.w(LOG_TAG, "Failed to getBluetoothLeScanner");
                } else {
                    ScanSettings scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                            .build();
                    List<ScanFilter> filters = new ArrayList<>(1);
//                    ScanFilter scanFilter = new ScanFilter.Builder()
//                            .setServiceUuid(new ParcelUuid(TapGattAttributes.RX_SERVICE_UUID))
//                            .build();
//                    filters.add(scanFilter);

                    mBleScanner.startScan(filters, scanSettings, mLeScanCallback);

                    // notify we have started scanning
                    broadcastUpdate(TapGattAttributes.ACTION_BLE_SCAN_START);

                    // ensure any scan auto-terminated if user hasn't already done so
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(SCAN_STOP_MSG_ID),
                            SCAN_TIMEOUT_MS);
                }
            }
        }
    }

    public void stopScanDevices() {
        synchronized (mLock) {
            if (null != mBleScanner) {
                mBleScanner.stopScan(mLeScanCallback);
                mBleScanner = null;

                // notify we have stopped scanning
                broadcastUpdate(TapGattAttributes.ACTION_BLE_SCAN_STOP);
            }
        }
    }

    public void clearAllResultsForDevice(String address) {
        int num = getContentResolver().delete(
                TapTapDataContract.TemperatureRecordEntry.CONTENT_URI,
                TapTapDataContract.TemperatureRecordEntry.COLUMN_DEVICE_ADDRESS + " = '" + address + "'",
                null);
        Log.i(LOG_TAG, "Deleted " + num + " temperature records for device " + address);
    }

    public boolean isScanning() {
        return null != mBleScanner;
    }

    public boolean bleIsEnabled() {
        return (null != mBluetoothAdapter) && mBluetoothAdapter.isEnabled();
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if ((null == mBluetoothAdapter) || (null == address)) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if ((null != mBluetoothDeviceAddress) && address.equals(mBluetoothDeviceAddress)
                && (null != mBluetoothGatt)) {
            Log.d(LOG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (null == device) {
            Log.w(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if ((null == mBluetoothAdapter) || (null == mBluetoothGatt)) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
        } else {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(LOG_TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public BluetoothGattService getSupportedGattService() {
        BluetoothGattService retVal = null;
        if (null != mBluetoothGatt) {
            retVal = mBluetoothGatt.getService(RX_SERVICE_UUID);
        }
        return retVal;
    }

    public List<BluetoothGattService> getServices() {
        List<BluetoothGattService> retVal = null;
        if (null != mBluetoothGatt) {
            retVal = mBluetoothGatt.getServices();
        }
        return retVal;
    }

    public void sendCommand(char[] data) {
        //writeValue(data);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (RX_CHAR_UUID.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    TapGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
            if (null != descriptor) {
                descriptor.setValue(enabled ?
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        boolean retVal = (null != mBluetoothAdapter) &&
                (null != mBluetoothGatt) &&
                mBluetoothGatt.writeCharacteristic(characteristic);
        if (!retVal) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
        }
        return retVal;
    }

    // static class with weak reference used to prevent memory leak
    static class BleTapServiceHandler extends Handler {
        private final WeakReference<BleTapTapService> mService;

        BleTapServiceHandler(BleTapTapService service) {
            super(service.getMainLooper());
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            if (SCAN_STOP_MSG_ID == msg.what) {
                BleTapTapService service = mService.get();
                if (null != service) {
                    service.stopScanDevices();
                }
            } else {
                super.handleMessage(msg);
            }
        }
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
