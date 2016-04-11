package com.alteredworlds.taptap.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.alteredworlds.taptap.R;
import com.alteredworlds.taptap.data.TapTapDataContract;
import com.alteredworlds.taptap.util.DateHelper;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by twcgilbert on 14/12/2015.
 */
public class TemperatureListAdapter extends CursorAdapter {
    private final DecimalFormat mDecimalFormat = new DecimalFormat("#.00");
    private ColumnIndeces mIndeces;

    public TemperatureListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.temperature_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        long millis = cursor.getLong(mIndeces.colTIMESTAMP);
        Date nightmare = new Date(millis * 1000);

        holder.timeTextView.setText(DateHelper.formattedDateLocal(nightmare));
        setValue(cursor, mIndeces.colVALUE0, holder.value0TextView);
        setValue(cursor, mIndeces.colVALUE1, holder.value1TextView);
        setValue(cursor, mIndeces.colVALUE2, holder.value2TextView);
    }

    protected void setValue(Cursor cursor, int index, TextView view) {
        if (cursor.isNull(index)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);

            int readResult = cursor.getInt(index);
            double temperature = readResult / 100.0;
            view.setText(mDecimalFormat.format(temperature));
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
            mIndeces = new ColumnIndeces(cursor);
        } else {
            mIndeces = null;
        }
    }

    protected static class ColumnIndeces {
        public final int colTIMESTAMP;
        public final int colVALUE0;
        public final int colVALUE1;
        public final int colVALUE2;

        public ColumnIndeces(Cursor cursor) {
            colTIMESTAMP = cursor.getColumnIndex(TapTapDataContract.TemperatureRecordEntry.COLUMN_TIMESTAMP);
            colVALUE0 = cursor.getColumnIndex(TapTapDataContract.TemperatureRecordEntry.COLUMN_VALUE0);
            colVALUE1 = cursor.getColumnIndex(TapTapDataContract.TemperatureRecordEntry.COLUMN_VALUE1);
            colVALUE2 = cursor.getColumnIndex(TapTapDataContract.TemperatureRecordEntry.COLUMN_VALUE2);
        }
    }

    protected static class ViewHolder {
        public final TextView timeTextView;
        public final TextView value0TextView;
        public final TextView value1TextView;
        public final TextView value2TextView;

        public ViewHolder(View view) {
            timeTextView = (TextView) view.findViewById(R.id.timeTextView);
            value0TextView = (TextView) view.findViewById(R.id.valueTextView);
            value1TextView = (TextView) view.findViewById(R.id.value1TextView);
            value2TextView = (TextView) view.findViewById(R.id.value2TextView);
        }
    }
}
