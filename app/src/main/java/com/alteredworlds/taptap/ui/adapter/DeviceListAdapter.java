package com.alteredworlds.taptap.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alteredworlds.taptap.R;
import com.alteredworlds.taptap.data.converter.BluetoothDeviceConverter;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class DeviceListAdapter extends CursorAdapter {

    private BluetoothDeviceConverter.ColumnIndices mColumnIndeces;

    public DeviceListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.device_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.addressTextView.setText(cursor.getString(mColumnIndeces.colADDRESS));
        if (cursor.isNull(mColumnIndeces.colNAME)) {
            holder.nameTextView.setVisibility(View.GONE);
        } else {
            holder.nameTextView.setVisibility(View.VISIBLE);
            holder.nameTextView.setText(cursor.getString(mColumnIndeces.colNAME));
        }
    }

    @Override
    public void changeCursor(Cursor cursor) {
        updateColumnIndices(cursor);
        super.changeCursor(cursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        updateColumnIndices(newCursor);
        return super.swapCursor(newCursor);
    }

    private void updateColumnIndices(Cursor cursor) {
        if (null != cursor) {
            mColumnIndeces = new BluetoothDeviceConverter.ColumnIndices(cursor);
        } else {
            mColumnIndeces = null;
        }
    }

    protected class ViewHolder {
        public final TextView addressTextView;
        public final TextView nameTextView;

        public ViewHolder(View view) {
            addressTextView = (TextView) view.findViewById(R.id.addressTextView);
            nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        }
    }
}
