package com.socioty.smartik;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Device;
import cloud.artik.model.DevicesEnvelope;
import cloud.artik.model.UserEnvelope;


public class ListDeviceTypesActivity extends AppCompatActivity {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    private static final String LED_SMART_LIGHT_DEVICE_TYPE_ID = "dt71c282d4fad94a69b22fa6d1e449fbbb";

    private String accessToken;

    private UsersApi usersApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device_types);

        accessToken = getIntent().getStringExtra(KEY_ACCESS_TOKEN);
        initializeDevicesApi(accessToken);

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
                    invokeListDevices(result);
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

    private void invokeListDevices(final UserEnvelope userEnvelope) {
        try {
            usersApi.getUserDevicesAsync(userEnvelope.getData().getId(), 0, 100, true, new ApiCallback<DevicesEnvelope>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println(e.getCause());
                }

                @Override
                public void onSuccess(DevicesEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {

                    final List<Device> devices = new ArrayList<>();

                    for (final Device device : result.getData().getDevices()) {
                        if (device.getDtid().equals(LED_SMART_LIGHT_DEVICE_TYPE_ID)) {
                            devices.add(device);
                        }
                    }

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(ListDeviceTypesActivity.this.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.device_list);

                            // use this setting to improve performance if you know that changes
                            // in content do not change the layout size of the RecyclerView
                            mRecyclerView.setHasFixedSize(true);

                            // use a linear layout manager
                            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(ListDeviceTypesActivity.this);
                            mRecyclerView.setLayoutManager(mLayoutManager);


                            // specify an adapter (see also next example)
                            final DeviceListAdapter mAdapter = new DeviceListAdapter(devices, accessToken);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    };
                    mainHandler.post(myRunnable);
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

    private void initializeDevicesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        usersApi = new UsersApi(mApiClient);
    }
}