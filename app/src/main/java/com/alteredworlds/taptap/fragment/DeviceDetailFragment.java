package com.alteredworlds.taptap.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alteredworlds.taptap.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceDetailFragment extends Fragment {

    public DeviceDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_detail_fragment, container, false);
    }
}
