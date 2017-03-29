package com.socioty.smartik;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class StatusFragment extends Fragment {

    public static StatusFragment newInstance() {
        
        Bundle args = new Bundle();
        
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status,container,false);
    }
}
