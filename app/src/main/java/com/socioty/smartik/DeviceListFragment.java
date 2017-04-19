package com.socioty.smartik;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cloud.artik.api.DevicesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Device;
import cloud.artik.model.DeviceEnvelope;
import cloud.artik.model.DevicesEnvelope;

/**
 * Created by serhiipianykh on 2017-03-23.
 */

public class DeviceListFragment extends Fragment {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    public static final String LED_SMART_LIGHT_DEVICE_TYPE_ID = "dt71c282d4fad94a69b22fa6d1e449fbbb";
    public static final String NEST_THERMOSTAT_DEVICE_TYPE_ID = "dt5247379d38fa4ac78e4723f8e92de681";

    public static final String KEY_DEVICE_ID = "DEVICE_ID";
    public static final String KEY_FLOOR_NUMBER = "FLOOR_NUMBER";
    public static final String KEY_ROOM_NAME = "ROOM_NAME";
    public static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String KEY_DEVICE_TYPE = "DEVICE_TYPE";

    private String accessToken;

    private UsersApi usersApi;
    private DevicesApi devicesApi;

    private ManageDeviceFragment manageDeviceFragment;
    private FloatingActionButton addDeviceBtn;

    private String userId;

    public static DeviceListFragment newInstance() {

        Bundle args = new Bundle();

        DeviceListFragment fragment = new DeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = Token.sToken.getToken();
        userId = Token.sToken.getUserId();
        initializeDevicesApi(accessToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_list_device_types, container, false);

        manageDeviceFragment = ManageDeviceFragment.newInstance();
        manageDeviceFragment.setTargetFragment(this, 0);

        addDeviceBtn = (FloatingActionButton) v.findViewById(R.id.add_device_fab);
        addDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getChildFragmentManager();
                manageDeviceFragment.setArguments(new Bundle());
                manageDeviceFragment.show(fm, "BLA");
            }
        });

        invokeListDevices();

        return v;
    }

    private void invokeListDevices() {
        try {
            usersApi.getUserDevicesAsync(userId, 0, 100, true, new ApiCallback<DevicesEnvelope>() {

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
                    Handler mainHandler = new Handler(getActivity().getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            final RecyclerView mRecyclerView = (RecyclerView) getView().findViewById(R.id.device_list);

                            // use this setting to improve performance if you know that changes
                            // in content do not change the layout size of the RecyclerView
                            mRecyclerView.setHasFixedSize(true);

                            // use a linear layout manager
                            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                            mRecyclerView.setLayoutManager(mLayoutManager);


                            // specify an adapter (see also next example)
                            final DeviceListAdapter mAdapter = new DeviceListAdapter(manageDeviceFragment, getChildFragmentManager(), devices, accessToken);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    };
                    mainHandler.post(myRunnable);
                    startSockectListenerService(userId, devices);
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
        devicesApi = new DevicesApi(mApiClient);
    }

    private void startSockectListenerService(final String userId, final Iterable<Device> devices) {
        if (devices.iterator().hasNext()) {
            final StringBuilder deviceIds = new StringBuilder();
            for (final Device device : devices) {
                deviceIds.append(device.getId()).append(",");
            }
            deviceIds.delete(deviceIds.length() - 1, deviceIds.length());

            final Intent intent = new Intent(getContext(), FirehoseWebSocketListenerService.class);
            final Bundle bundle = new Bundle();
            bundle.putString(FirehoseWebSocketListenerService.ACCESS_TOKEN_KEY, accessToken);
            bundle.putString(FirehoseWebSocketListenerService.USER_ID_KEY, userId);
            bundle.putString(FirehoseWebSocketListenerService.DEVICE_IDS_KEY, deviceIds.toString());
            intent.putExtras(bundle);
            getActivity().startService(intent);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 10: {
                int floorNumber = data.getExtras().getInt(KEY_FLOOR_NUMBER);
                String roomName = data.getExtras().getString(KEY_ROOM_NAME);
                String name = data.getExtras().getString(KEY_DEVICE_NAME);
                int type = data.getExtras().getInt(KEY_DEVICE_TYPE);
                String dtid = "";
                if (type == 0) {
                    dtid = LED_SMART_LIGHT_DEVICE_TYPE_ID;
                } else {
                    dtid = NEST_THERMOSTAT_DEVICE_TYPE_ID;
                }
                addDevice(floorNumber, roomName, name, dtid);
                break;
            }
            case 20: {
                String deviceId = data.getExtras().getString(KEY_DEVICE_ID);
                int floorNumber = data.getExtras().getInt(KEY_FLOOR_NUMBER);
                String roomName = data.getExtras().getString(KEY_ROOM_NAME);
                String name = data.getExtras().getString(KEY_DEVICE_NAME);
                updateDevice(deviceId, floorNumber, roomName, name);
                break;
            }
            case 30: {
                String deviceId = data.getExtras().getString(KEY_DEVICE_ID);
                deleteDevice(deviceId);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknow resultCode");
            }
        }
    }

    private void addDevice(final int floorNumber, final String roomName, String name, String dtid) {
        try {
            devicesApi.addDeviceAsync(new Device().name(name).uid(userId).dtid(dtid), new ApiCallback<DeviceEnvelope>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    //TODO: TREAT!!
                    e.printStackTrace();
                }

                @Override
                public void onSuccess(DeviceEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    final JSONObject object = new JSONObject();
                    try {

                        object.put("deviceId", result.getData().getId());
                        object.put("floorNumber", floorNumber);
                        object.put("roomName", roomName);

                        final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                                (Request.Method.POST, RequestUtils.BACKEND_DEVICE_RESOURCE, object, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(final JSONObject response) {
                                        JsonUtils.extractDeviceMapFromResponse(response);
                                        invokeListDevices();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(final VolleyError error) {
                                        throw new RuntimeException(error);
                                    }
                                });
                        RequestUtils.addRequest(jsObjRequest);
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    //NOTHING TO DO
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    //NOTHING TO DO
                }
            });
        } catch (final ApiException e) {
            e.printStackTrace();
        }
    }

    private void updateDevice(final String deviceId, final int floorNumber, final String roomName, String name) {
        try {
            devicesApi.updateDeviceAsync(deviceId, new Device().name(name).uid(userId),  new ApiCallback<DeviceEnvelope>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    //TODO: TREAT!!
                    e.printStackTrace();
                }

                @Override
                public void onSuccess(DeviceEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    try {
                        final JSONObject object = new JSONObject();
                        object.put("deviceId", deviceId);
                        object.put("floorNumber", floorNumber);
                        object.put("roomName", roomName);

                        final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                                (Request.Method.POST, RequestUtils.BACKEND_DEVICE_RESOURCE, object, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(final JSONObject response) {
                                        JsonUtils.extractDeviceMapFromResponse(response);
                                        invokeListDevices();
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

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    //NOTHING TO DO
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    //NOTHING TO DO
                }
            });
        } catch (final ApiException e) {
            e.printStackTrace();
        }
    }

    private void deleteDevice(final String deviceId) {
        try {
            devicesApi.deleteDeviceAsync(deviceId, new ApiCallback<DeviceEnvelope>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    //TODO: TREAT!!
                    e.printStackTrace();
                }

                @Override
                public void onSuccess(DeviceEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                            (Request.Method.DELETE, String.format(RequestUtils.BACKEND_DEVICE_RESOURCE_ID_PATTERN, deviceId), null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(final JSONObject response) {
                                    JsonUtils.extractDeviceMapFromResponse(response);
                                    invokeListDevices();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(final VolleyError error) {
                                    throw new RuntimeException(error);
                                }
                            });
                    RequestUtils.addRequest(jsObjRequest);
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    //NOTHING TO DO
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    //NOTHING TO DO
                }
            });
        } catch (final ApiException e) {
            e.printStackTrace();
        }
    }
}
