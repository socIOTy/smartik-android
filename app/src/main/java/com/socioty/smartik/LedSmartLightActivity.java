package com.socioty.smartik;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.enrico.colorpicker.colorDialog;
import com.triggertrap.seekarc.SeekArc;

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

public class LedSmartLightActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String KEY_DEVICE_ID = "DEVICE_ID";

    private String deviceId;

    private MessagesApi messagesApi;

    private ImageButton imageButton;
    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_smart_light_main);

        //Set up Bottom Bar
        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        bottomNavigationView.getMenu().getItem(1).setChecked(false);
        bottomNavigationView.getMenu().getItem(2).setChecked(false);

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

        bottomNavigationView.setOnNavigationItemSelectedListener(

                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.action_appointments:

                                break;
                            case R.id.action_patients:

                                break;
                        }

                        return false;
                    }
                });



        enableComponentsBasedOnState(false);
        this.deviceId = getIntent().getStringExtra(KEY_DEVICE_ID);
        initializeMessagesApi(getIntent().getStringExtra(KEY_ACCESS_TOKEN));


        configureColorButton();
        configureIntensityPicker();
    }

    private void enableComponentsBasedOnState(final boolean state) {
        final View colorView = (View) findViewById(R.id.ledColorView);
        final SeekArc intensitySeekArc = (SeekArc) findViewById(R.id.ledIntensitySeekArc);

        colorView.setEnabled(state);
        intensitySeekArc.setEnabled(state);
    }

    private void configureColorButton() {
        final View colorView = (View) findViewById(R.id.ledColorView);
        colorDialog.setPickerColor(LedSmartLightActivity.this, 1, Color.YELLOW);

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
        } else {
            imageButton.setImageResource(R.mipmap.off_button);
        }
    }
}
