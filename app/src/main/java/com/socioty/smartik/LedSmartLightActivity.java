package com.socioty.smartik;

import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enrico.colorpicker.colorDialog;
import com.socioty.smartik.Model.Token;
import com.triggertrap.seekarc.SeekArc;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Action;
import cloud.artik.model.ActionArray;
import cloud.artik.model.Actions;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.NormalizedMessagesEnvelope;

public class LedSmartLightActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener, DeviceMessageBroadcastReceiver.Delegate {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String KEY_DEVICE_ID = "DEVICE_ID";

    private final DeviceMessageBroadcastReceiver broadcastReceiver = new DeviceMessageBroadcastReceiver(this);

    private String deviceId;

    private MessagesApi messagesApi;

    private ImageButton imageButton;
    private TextView state;

    private boolean isOn = false;
    private int intensity = 0;
    private int rColor = 0;
    private int gColor = 0;
    private int bColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_smart_light_main);

        initializeMessagesApi(Token.sToken.getToken());
        this.deviceId = getIntent().getStringExtra(KEY_DEVICE_ID);
        imageButton = (ImageButton) findViewById(R.id.switcher);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOn = !isOn;
                changeImage(imageButton, isOn);
                sendStateAction(isOn);
                enableComponentsBasedOnState(isOn);
            }
        });


        getLatestMsg();

        state = (TextView) findViewById(R.id.lightIndicatorText);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(String.format(FirehoseWebSocketListenerService.DEVICE_MESSAGE_BROADCAST_ACTION_PATTERN, deviceId));
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void enableComponentsBasedOnState(final boolean state) {
        final View colorView = (View) findViewById(R.id.ledColorView);
        final SeekArc intensitySeekArc = (SeekArc) findViewById(R.id.ledIntensitySeekArc);
        colorView.setEnabled(state);
        changeImage(imageButton, isOn);
        intensitySeekArc.setEnabled(state);
    }

    private void configureColorButton() {
        final View colorView = (View) findViewById(R.id.ledColorView);
        int rgb = rColor;
        rgb = (rgb << 8) + gColor;
        rgb = (rgb << 8) + bColor;
        colorDialog.setPickerColor(LedSmartLightActivity.this, 1, rgb);
        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                colorDialog.showColorPicker(LedSmartLightActivity.this, 1);
            }
        });
    }

    private void configureIntensityPicker() {
        final SeekArc intensitySeekArc = (SeekArc) findViewById(R.id.ledIntensitySeekArc);
        final TextView seekArcProgress = (TextView) findViewById(R.id.seekArcProgress);
        intensitySeekArc.setProgress(intensity);
        seekArcProgress.setText(String.valueOf(intensity));
        intensitySeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            int latestProgress = 0;
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                seekArcProgress.setText(String.valueOf(progress));
                latestProgress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                sendIntensityAction(latestProgress);


            }
        });

    }


    @Override
    public void onColorSelection(final DialogFragment dialogFragment, @ColorInt final int selectedColor) {
        colorDialog.setPickerColor(LedSmartLightActivity.this, 1, selectedColor);
        final View colorView = (View) findViewById(R.id.ledColorView);
        colorView.setBackgroundColor(selectedColor);
        sendColorAction(selectedColor, getSelectedIntensity());
    }

    private int getSelectedIntensity() {
        //return ((SeekBar) findViewById(R.id.ledIntensitySlider)).getProgress();
        return Integer.parseInt(((TextView) findViewById(R.id.seekArcProgress)).getText().toString());


    }

    private void initializeMessagesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        messagesApi = new MessagesApi(mApiClient);
    }

    private void sendStateAction(boolean active) {
        final ActionArray actionArray = new ActionArray();
        actionArray.addActionsItem(new Action().name(active ? "setOn" : "setOff"));

        commonSendAction(actionArray);
    }

    private void sendColorAction(final int color, final int intensity) {
        final ActionArray actionArray = new ActionArray();
        final Map<String,Integer> colorRgbMap = new HashMap<>();
        colorRgbMap.put("r", Color.red(color));
        colorRgbMap.put("g", Color.green(color));
        colorRgbMap.put("b", Color.blue(color));
        actionArray.addActionsItem(new Action()
                .name("setColorAsRGB")
                .putParametersItem("colorRGB", colorRgbMap)
                .putParametersItem("intensity", intensity));

        commonSendAction(actionArray);
    }

    private void sendIntensityAction(final int intensity) {
        final ActionArray actionArray = new ActionArray();
        actionArray.addActionsItem(new Action()
                .name("setIntensity")
                .putParametersItem("intensity", intensity));

        commonSendAction(actionArray);
    }

    private void commonSendAction(final ActionArray actionArray) {
        final Actions actions = new Actions(); // Actions | Actions that are passed in the body
        actions.setDdid(deviceId);

        actions.setData(actionArray);

        try {
            messagesApi.sendActionsAsync(actions, new ApiCallback<MessageIDEnvelope>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    System.out.println("FALHOU!!");
                    e.printStackTrace();
                }

                @Override
                public void onSuccess(MessageIDEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    broadcastReceiver.ignoreNext();
                    System.out.println(result);
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                }
            });
        } catch (ApiException e) {
            System.err.println("Exception when calling MessagesApi#sendActions");
            e.printStackTrace();
        }
    }
    private void changeImage(ImageButton imageButton, boolean isOn) {
        if (isOn) {
            imageButton.setImageResource(R.mipmap.on_button);
            state.setText(getString(R.string.light_on));

        } else {
            imageButton.setImageResource(R.mipmap.off_button);
            state.setText(getString(R.string.light_off));
        }
    }

    private void getLatestMsg() {
        final String tag = "Bulb getLastNormalizedMessagesAsync";
        try {
            int messageCount = 1;
            messagesApi.getLastNormalizedMessagesAsync(messageCount, deviceId, null,
                    new ApiCallback<NormalizedMessagesEnvelope>() {
                        @Override
                        public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(NormalizedMessagesEnvelope result, int i, Map<String, List<String>> stringListMap) {
                            Log.v(tag, " onSuccess latestMessage = " + result.getData().toString());
                            String mid = "";
                            String data = "";
                            if (!result.getData().isEmpty()) {
                                mid = result.getData().get(0).getMid();
                                data = result.getData().get(0).getData().toString();
                            }
                            updateState(data);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initUI();
                                }
                            });

                        }

                        @Override
                        public void onUploadProgress(long bytes, long contentLen, boolean done) {
                        }

                        @Override
                        public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                        }
                    });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }

    private void processFailure(final String context, ApiException exc) {
        String errorDetail = " onFailure with exception" + exc;
        Log.w(context, errorDetail);
        exc.printStackTrace();
    }

    private void updateState(final String jsonString) {
        if (jsonString != "" && jsonString != null) {
            try {
                updateState(new JSONObject(jsonString));
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateState(final JSONObject json) {
        if (json != null) {
            try {
                isOn = json.getBoolean("state");
                intensity = json.getInt("intensity");
                JSONObject colors = json.getJSONObject("colorRGB");
                rColor = colors.getInt("r");
                gColor = colors.getInt("g");
                bColor = colors.getInt("b");
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initUI() {
        configureColorButton();
        configureIntensityPicker();
        enableComponentsBasedOnState(isOn);
    }

    @Override
    public View getSnackbarView() {
        return findViewById(R.id.switcher);
    }

    @Override
    public void delegate(final JSONObject json) {
        updateState(json);
        initUI();
    }
}
