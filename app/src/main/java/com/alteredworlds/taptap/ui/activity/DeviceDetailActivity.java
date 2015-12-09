package com.alteredworlds.taptap.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.alteredworlds.taptap.R;
import com.alteredworlds.taptap.data.converter.BluetoothDeviceConverter;
import com.alteredworlds.taptap.service.BleTapTapService;
import com.alteredworlds.taptap.service.TapGattAttributes;

public class DeviceDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_DEVICE_URI = "DEVICE_URI";
    private static final String LOG_TAG = DeviceDetailActivity.class.getSimpleName();
    private static final int DEVICE_INFO_LOADER_ID = 2;

    private Uri mDeviceUri;
    private String mDeviceAddress;
    private TextView mAddressTextView;
    private TextView mNameTextView;
    private TextView mStatusTextView;


    // move away from bound service ASAP
    private BleTapTapService mService;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(LOG_TAG, action);
            if (TapGattAttributes.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (TapGattAttributes.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (null == mService.getSupportedGattService()) {
                    mStatusTextView.setText("");
                } else {
                    mStatusTextView.setText("THIS IS OURS, BABY!");
                }
            } else if (TapGattAttributes.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
            }
        }
    };
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(TapGattAttributes.ACTION_GATT_CONNECTED);
        intentFilter.addAction(TapGattAttributes.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(TapGattAttributes.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(TapGattAttributes.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Device Uri from Intent
        mDeviceUri = getIntent().getParcelableExtra(EXTRA_DEVICE_URI);

        mAddressTextView = (TextView) findViewById(R.id.addressTextView);
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
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
            mService.disconnect();
            unbindService(mConnection);
            mService = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGattUpdateReceiver,
                makeGattUpdateIntentFilter());

        getSupportLoaderManager().initLoader(DEVICE_INFO_LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Loader<Cursor> retVal;
        switch (id) {
            case DEVICE_INFO_LOADER_ID:
                retVal = new CursorLoader(
                        this,
                        mDeviceUri,
                        null,   // projection
                        null,   // selection
                        null,   // selectionArgs
                        null);  // sort order
                break;

            default:
                throw new UnsupportedOperationException("Unknown Loader ID: " + id);
        }
        return retVal;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // use the Cursor to populate our fields...
        switch (loader.getId()) {
            case DEVICE_INFO_LOADER_ID:
                setDeviceInfo(data);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Loader ID: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setDeviceInfo(Cursor data) {
        if ((null != data) && data.moveToFirst()) {
            BluetoothDeviceConverter.ColumnIndices columnIndices = new BluetoothDeviceConverter.ColumnIndices(data);
            // required field
            mDeviceAddress = data.getString(columnIndices.colADDRESS);
            mAddressTextView.setText(mDeviceAddress);
            // optional
            String name = data.isNull(columnIndices.colNAME) ? "" : data.getString(columnIndices.colNAME);
            mNameTextView.setText(name);

            // try connecting to the device
            mService.connect(mDeviceAddress);
        }
    }
}
