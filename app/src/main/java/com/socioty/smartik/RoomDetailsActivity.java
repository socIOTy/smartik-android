package com.socioty.smartik;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.socioty.smartik.model.DeviceMap;
import com.socioty.smartik.model.Room;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import static com.socioty.smartik.R.id.imageView;

public class RoomDetailsActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;

    private UsersApi usersApi;
    private Room room;

    private ManageDeviceFragment manageDeviceFragment;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        progressDialog = new CustomProgressDialog(this);

        final String accessToken = Token.getToken();
        final String userId = Token.sToken.getUserId();


        initializeApi(accessToken);
        this.room = Token.sToken.getDeviceMap().getRoom(getIntent().getExtras().getString("roomName"));


        initializeTextName(room);
        initializeDeleteButton();
        initializeImage(room);
        initializeDeviceList(accessToken, userId, room);
    }

    private void initializeTextName(final Room room) {
        final TextView textView = (TextView) findViewById(R.id.room_name);
        textView.setText(room.getName());
    }

    private void initializeDeleteButton() {
        final ImageButton deleteRoomBtn = (ImageButton) findViewById(R.id.room_delete_btn);
        deleteRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.getDeviceIds().isEmpty()) {
                    new AlertDialog.Builder(RoomDetailsActivity.this).setTitle(R.string.delete_room_title)
                            .setMessage(R.string.delete_room_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final DeviceMap deviceMap = Token.sToken.getDeviceMap();
                                    deviceMap.removeRoom(room);
                                    try {
                                        final String deviceMapJsonString = JsonUtils.GSON.toJson(Token.sToken.getDeviceMap());
                                        final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                                                (Request.Method.POST, RequestUtils.BACKEND_DEVICE_MAP_RESOURCE, new JSONObject(deviceMapJsonString), new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(final JSONObject response) {
                                                        RoomDetailsActivity.this.finish();
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
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                } else {
                    new AlertDialog.Builder(RoomDetailsActivity.this).setTitle(R.string.delete_room_title)
                            .setMessage(R.string.cannot_delete_room_with_devices)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Nothing to do
                                }}).show();

                }

            }
        });
    }

    private void initializeApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        usersApi = new UsersApi(mApiClient);
    }

    private void initializeImage(Room room) {
        final ImageView imageView = (ImageView) findViewById(R.id.room_image);
        final byte[] imageBytes = room.getImageBytes();
        if (imageBytes != null) {
            final Bitmap bitMap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(bitMap);
        }
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                return true;
            }
        });
    }

    private void initializeDeviceList(final String accessToken, final String userId, final Room room) {
        progressDialog.show();
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
                        if (room.getDeviceIds().contains(device.getId())) {
                            devices.add(device);
                        }
                    }

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(RoomDetailsActivity.this.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.room_device_list);
                            recyclerView.setHasFixedSize(true);
                            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(RoomDetailsActivity.this);
                            recyclerView.setLayoutManager(mLayoutManager);
                            final DeviceListAdapter mAdapter = new DeviceListAdapter(devices, accessToken);
                            recyclerView.setAdapter(mAdapter);
                            progressDialog.dismiss();
                        }
                    };
                    mainHandler.post(myRunnable);
                    startSockectListenerService(accessToken, userId, devices);
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {


                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {


                }
            });
        } catch (final ApiException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        progressDialog.show();
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        room.setImageBytes(byteArray);
                        try {
                            final String deviceMapJsonString = JsonUtils.GSON.toJson(Token.sToken.getDeviceMap());
                            final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                                    (Request.Method.POST, RequestUtils.BACKEND_DEVICE_MAP_RESOURCE, new JSONObject(deviceMapJsonString), new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(final JSONObject response) {

                                            final ImageView imageView = (ImageView) findViewById(R.id.room_image);
                                            imageView.setImageBitmap(selectedImage);
                                            progressDialog.dismiss();
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
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                }
        }
    }


    private void startSockectListenerService(final String accessToken, final String userId, final Iterable<Device> devices) {
        if (devices.iterator().hasNext()) {
            final StringBuilder deviceIds = new StringBuilder();
            for (final Device device : devices) {
                deviceIds.append(device.getId()).append(",");
            }
            deviceIds.delete(deviceIds.length() - 1, deviceIds.length());

            final Intent intent = new Intent(this, FirehoseWebSocketListenerService.class);
            final Bundle bundle = new Bundle();
            bundle.putString(FirehoseWebSocketListenerService.ACCESS_TOKEN_KEY, accessToken);
            bundle.putString(FirehoseWebSocketListenerService.USER_ID_KEY, userId);
            bundle.putString(FirehoseWebSocketListenerService.DEVICE_IDS_KEY, deviceIds.toString());
            intent.putExtras(bundle);
            startService(intent) ;
        }
    }


}
