package com.socioty.smartik;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.socioty.smartik.model.DeviceMap;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Device;
import cloud.artik.model.DevicesEnvelope;
import cloud.artik.model.UserEnvelope;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class StatusFragment extends Fragment {

    private UsersApi usersApi;
    private MessagesApi messagesApi;

    private String fullName ="";
    private int devicesCount = 0;

    private TextView greeting;
    private TextView devices;
    private TextView currentTemp;
    public static StatusFragment newInstance() {
        
        Bundle args = new Bundle();
        
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeMessagesApi(Token.sToken.getToken());
        initStatusData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_status, container, false);
        greeting = (TextView)v.findViewById(R.id.user_greeting);
        devices = (TextView)v.findViewById(R.id.active_devices);
        return v;
    }

    private void initializeMessagesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        usersApi = new UsersApi(mApiClient);
        messagesApi = new MessagesApi(mApiClient);
    }
    private void initStatusData() {
        try {
            usersApi.getSelfAsync(new ApiCallback<UserEnvelope>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println(e.getCause());
                }

                @Override
                public void onSuccess(UserEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    fullName = result.getData().getFullName();
                    getDevicesData();
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                }
            });


        } catch (final ApiException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        }

    }

    private void getDevicesData() {
        try {
            usersApi.getUserDevicesAsync(Token.sToken.getUserId(), 0, 100, true, new ApiCallback<DevicesEnvelope>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println(e.getCause());
                }

                @Override
                public void onSuccess(DevicesEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {

                    devicesCount = result.getCount().intValue();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                }
            });
        } catch (final ApiException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        }
    }

    private void updateUI() {
        greeting.setText("Welcome back, "+ fullName + "!");
        devices.setText("You have " + devicesCount + "/" + devicesCount + " active devices");
    }
}
