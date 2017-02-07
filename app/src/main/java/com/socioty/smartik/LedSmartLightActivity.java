package com.socioty.smartik;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

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
import cloud.artik.model.Message;
import cloud.artik.model.MessageIDEnvelope;

public class LedSmartLightActivity extends AppCompatActivity {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    private static final String LED_SMART_LIGHT_DEVICE_ID = "61c976c37c604467a1e6d7d963723545";

    private MessagesApi messagesApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_smart_light_main);

        initializeMessagesApi(getIntent().getStringExtra(KEY_ACCESS_TOKEN));

        final Switch switchLights = (Switch) findViewById(R.id.switchLights);

        switchLights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean active) {
                sendAction(active);
                if (active) {
                    System.out.println("Lights on");
                } else {
                    System.out.println("Lights off");
                }
            }
        });
    }

    private void initializeMessagesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        messagesApi = new MessagesApi(mApiClient);
    }

    private void sendAction(boolean active) {
        final Actions actions = new Actions(); // Actions | Actions that are passed in the body
        actions.setDdid(LED_SMART_LIGHT_DEVICE_ID);

        final ActionArray actionArray = new ActionArray();
        actionArray.addActionsItem(new Action().name(active ? "setOn" : "setOff"));
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
}
