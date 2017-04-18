package com.socioty.smartik;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socioty.smartik.model.DeviceMap;
import com.socioty.smartik.model.Scenario;
import com.socioty.smartik.model.ScenarioAction;
import com.socioty.smartik.model.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class RoomsFragment extends Fragment {

    public static RoomsFragment newInstance() {

        Bundle args = new Bundle();

        RoomsFragment fragment = new RoomsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rooms, container, false);


        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.rooms_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        final DeviceMap deviceMap = Token.sToken.getDeviceMap();
        RoomListAdapter adapter = new RoomListAdapter(deviceMap.getAllRooms());
        recyclerView.setAdapter(adapter);

        return v;
    }
}
