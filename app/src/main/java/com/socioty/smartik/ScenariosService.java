package com.socioty.smartik;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.socioty.smartik.model.ScenarioAction;
import com.socioty.smartik.model.Token;

import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Action;
import cloud.artik.model.ActionArray;
import cloud.artik.model.Actions;
import cloud.artik.model.Device;
import cloud.artik.model.DevicesEnvelope;
import cloud.artik.model.MessageIDEnvelope;

/**
 * Created by serhiipianykh on 2017-04-17.
 */

public class ScenariosService extends Service {

    private MessagesApi messagesApi;
    private UsersApi usersApi;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeMessagesApi(Token.sToken.getToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle data = intent.getExtras();
            ScenarioAction action = (ScenarioAction) data.getSerializable("action");
            switch (action) {
                case lightsOn: {
                    sendStateAction(true);
                    sendMyBroadcastMessage();
                    break;
                }
                case lightsOff: {
                    sendStateAction(false);
                    sendMyBroadcastMessage();
                    break;
                }
                case stateHome: {
                    sendStateAction(true);
                    sendThermostatStateAction(1);
                    sendMyBroadcastMessage();
                    break;
                }
                case stateAway: {
                    sendStateAction(false);
                    sendThermostatStateAction(0);
                    sendMyBroadcastMessage();
                    break;
                }
                case energySaving: {
                    sendIntensityAction(30);
                    sendThermostatStateAction(1);
                    sendTemperature(20);
                    sendMyBroadcastMessage();
                    break;
                }
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeMessagesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        usersApi = new UsersApi(mApiClient);
        messagesApi = new MessagesApi(mApiClient);
    }

    private void sendStateAction(boolean active) {
        final ActionArray actionArray = new ActionArray();
        actionArray.addActionsItem(new Action().name(active ? "setOn" : "setOff"));
        sendGroupAction(actionArray, DeviceListFragment.LED_SMART_LIGHT_DEVICE_TYPE_ID);


    }

    private void sendIntensityAction(final int intensity) {
        final ActionArray actionArray = new ActionArray();
        actionArray.addActionsItem(new Action()
                .name("setIntensity")
                .putParametersItem("intensity", intensity));

        sendGroupAction(actionArray, DeviceListFragment.LED_SMART_LIGHT_DEVICE_TYPE_ID);

    }

    private void sendThermostatStateAction(int state) {
        final ActionArray actionArray = new ActionArray();
        Action action = new Action();
        switch(state) {
            case 0: {
                action.setName("setOff");
                break;
            }
            case 1: {
                action.setName("setHeatCoolMode");
                break;
            }
            case 2: {
                action.setName("setHeatMode");
                break;
            }
            case 3: {
                action.setName("setCoolMode");
                break;
            }
        }
        actionArray.addActionsItem(action);

        sendGroupAction(actionArray, DeviceListFragment.NEST_THERMOSTAT_DEVICE_TYPE_ID);

    }

    private void commonSendAction(final ActionArray actionArray, String deviceId) {
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

    private void sendTemperature(int temp) {
        final ActionArray actionArray = new ActionArray();
        Action action = new Action();
        action.setName("setTemperature");
        action.putParametersItem("temp", temp);
        actionArray.addActionsItem(action);
        sendGroupAction(actionArray, DeviceListFragment.NEST_THERMOSTAT_DEVICE_TYPE_ID);

    }

    private void sendMyBroadcastMessage() {
        Intent intent = new Intent();
        intent.setAction("scenarioAction");

        Bundle data = new Bundle();
        data.putString("broadcastMessage", "Scenario successfully activated");
        intent.putExtras(data);

        sendBroadcast(intent);
    }

    private void sendGroupAction(final ActionArray actionArray, final String dtid) {
        try {
            usersApi.getUserDevicesAsync(Token.sToken.getUserId(), 0, 100, true, new ApiCallback<DevicesEnvelope>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {

                }

                @Override
                public void onSuccess(DevicesEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {
                    for (final Device device : result.getData().getDevices()) {
                        if (device.getDtid().equals(dtid)) {
                            commonSendAction(actionArray, device.getId());
                        }
                    }
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
}
