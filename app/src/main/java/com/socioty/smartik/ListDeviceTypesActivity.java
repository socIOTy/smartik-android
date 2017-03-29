package com.socioty.smartik;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

    public static final String LED_SMART_LIGHT_DEVICE_TYPE_ID = "dt71c282d4fad94a69b22fa6d1e449fbbb";
    public static final String NEST_THERMOSTAT_DEVICE_TYPE_ID = "dt5247379d38fa4ac78e4723f8e92de681";

    private String accessToken;

    private UsersApi usersApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device_types);


        accessToken = Token.sToken.getToken();
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
                        devices.add(device);
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
                    startSockectListenerService(userEnvelope.getData().getId(), devices);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                Token.clearToken(getApplicationContext());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSockectListenerService(final String userId, final Iterable<Device> devices) {
        if (devices.iterator().hasNext()) {
            final StringBuilder deviceIds = new StringBuilder();
            for (final Device device : devices) {
                deviceIds.append(device.getId()).append(",");
            }
            deviceIds.delete(deviceIds.length() - 1, deviceIds.length());

            final Intent intent = new Intent(getApplicationContext(), FirehoseWebSocketListenerService.class);
            final Bundle bundle = new Bundle();
            bundle.putString(FirehoseWebSocketListenerService.ACCESS_TOKEN_KEY, accessToken);
            bundle.putString(FirehoseWebSocketListenerService.USER_ID_KEY, userId);
            bundle.putString(FirehoseWebSocketListenerService.DEVICE_IDS_KEY, deviceIds.toString());
            intent.putExtras(bundle);
            startService(intent) ;
        }

    }
}