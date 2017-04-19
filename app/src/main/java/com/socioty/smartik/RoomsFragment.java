package com.socioty.smartik;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.socioty.smartik.model.Scenario;
import com.socioty.smartik.model.ScenarioAction;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

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


    private boolean invalidated = false;

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

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        System.out.println("ok");
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
                        try {
                            final DeviceMap deviceMap = JsonUtils.GSON.fromJson(response.getJSONObject(RequestUtils.DEVICE_MAP_PROPERTY).toString(), DeviceMap.class);
                            Token.sToken.setDeviceMap(deviceMap);
                            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.rooms_list);
                            final RoomListAdapter adapter = new RoomListAdapter(deviceMap.getAllRooms());
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
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
}
