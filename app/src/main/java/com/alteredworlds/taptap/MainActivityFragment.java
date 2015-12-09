package com.alteredworlds.taptap;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alteredworlds.taptap.adapter.DeviceListAdapter;
import com.alteredworlds.taptap.data.TapTapDataContract.DeviceEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DEVICES_LOADER_ID = 1;
    private DeviceListAdapter mAdapter;
    private View mProgressBar;
    private View mListContainer;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        mAdapter = new DeviceListAdapter(getContext(), null, 0);
        listView.setAdapter(mAdapter);

        View emptyView = view.findViewById(R.id.emptyTextView);
        listView.setEmptyView(emptyView);

        mProgressBar = view.findViewById(R.id.progressBar);
        mListContainer = view.findViewById(R.id.listContainer);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().initLoader(DEVICES_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        //getActivity().getSupportLoaderManager().destroyLoader(DEVICES_LOADER_ID);
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Loader<Cursor> retVal;
        switch (id) {
            case DEVICES_LOADER_ID:
                retVal = new CursorLoader(
                        getContext(),
                        DeviceEntry.CONTENT_URI,
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
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void showScanningStatus(boolean scanning) {
        mProgressBar.setVisibility(scanning ? View.VISIBLE : View.GONE);
        mListContainer.setVisibility(scanning ? View.GONE : View.VISIBLE);
    }
}
