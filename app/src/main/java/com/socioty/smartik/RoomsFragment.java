package com.socioty.smartik;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.socioty.smartik.model.DeviceMap;
import com.socioty.smartik.model.Floor;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class RoomsFragment extends Fragment {

    public static String KEY_FLOOR_NUMBER = "floorNumber";
    public static String KEY_ROOM_NAME = "roomName";

    public static RoomsFragment newInstance() {

        Bundle args = new Bundle();

        RoomsFragment fragment = new RoomsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private RecyclerView recyclerView;
    private ManageRoomFragment manageRoomFragment;
    private boolean invalidated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rooms, container, false);

        this.manageRoomFragment = ManageRoomFragment.newInstance();
        manageRoomFragment.setTargetFragment(this, 0);

        this.recyclerView = (RecyclerView) v.findViewById(R.id.rooms_list);

        initializeAddRoomButton(v);

        return v;
    }

    private void initializeAddRoomButton(View v) {
        final FloatingActionButton addRoomButton = (FloatingActionButton) v.findViewById(R.id.add_room_fab);
        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageRoomFragment.setArguments(new Bundle());
                manageRoomFragment.show(getChildFragmentManager(), "BLA");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        this.invalidated = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                (Request.Method.GET, String.format(RequestUtils.BACKEND_ACCOUNT_BY_MAIL_RESOURCE_PATTERN, Token.sToken.getEmail()), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        final DeviceMap deviceMap = JsonUtils.extractDeviceMapFromResponse(response);
                        reloadRooms(deviceMap);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        throw new RuntimeException(error);
                    }
                });
        RequestUtils.addRequest(jsObjRequest);

        this.invalidated = false;
    }

    private void reloadRooms(final DeviceMap deviceMap) {
        final SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
        int floorNumber = 1;
        for (final Floor floor : deviceMap.getFloors()) {
            sectionAdapter.addSection(new FloorSection(manageRoomFragment, getChildFragmentManager(), floorNumber++, floor.getRoomsList()));
        }
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(sectionAdapter.getSectionItemViewType(position)) {
                    case VIEW_TYPE_HEADER:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        RoomsFragment.this.recyclerView.setLayoutManager(gridLayoutManager);
        RoomsFragment.this.recyclerView.setAdapter(sectionAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 10) {
            int floorNumber = data.getExtras().getInt(KEY_FLOOR_NUMBER);
            String name = data.getExtras().getString(KEY_ROOM_NAME);
            addRoom(floorNumber, name);

        }
    }

    private void addRoom(final int floorNumber, final String name) {
        final DeviceMap deviceMap = Token.sToken.getDeviceMap();
        deviceMap.addRoom(floorNumber, name);
        try {
            final String deviceMapJsonString = JsonUtils.GSON.toJson(deviceMap);
            final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                    (Request.Method.POST, RequestUtils.BACKEND_DEVICE_MAP_RESOURCE, new JSONObject(deviceMapJsonString), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            final DeviceMap deviceMap = JsonUtils.extractDeviceMapFromResponse(response);
                            reloadRooms(deviceMap);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(final VolleyError error) {
                            throw new RuntimeException(error);
                        }
                    });
            RequestUtils.addRequest(jsObjRequest);
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }


}
