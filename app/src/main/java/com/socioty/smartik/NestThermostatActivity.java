package com.socioty.smartik;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.socioty.smartik.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

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

public class NestThermostatActivity extends AppCompatActivity implements DeviceMessageBroadcastReceiver.Delegate{

    private enum Mode {
        OFF(false, false),
        AUTO(true, true),
        HEAT(true, false),
        COOL(false, true);

        private final boolean canHeat;
        private final boolean canCool;

        private Mode(final boolean canHeat, final boolean canCool) {
            this.canHeat = canHeat;
            this.canCool = canCool;
        }

        public boolean isCanHeat() {
            return canHeat;
        }

        public boolean isCanCool() {
            return canCool;
        }

        public static Mode parseMode(final boolean canHeat, final boolean canCool) {
            for (final Mode mode : values()) {
                if (canHeat == mode.canHeat && canCool == mode.canCool) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Unknown mode.");
        }

    }

    Spinner stateSpinner;
    SeekBar tempBar;
    TextView temperature;
    Button cButton;
    Button fButton;

    private static final int minC = 10;
    private static final int maxC = 40;
    private static final int minF = 50;
    private static final int maxF = 104;

    private final DeviceMessageBroadcastReceiver broadcastReceiver = new DeviceMessageBroadcastReceiver(this);

    private String deviceId;

    private MessagesApi messagesApi;

    private int state = 2;
    char currentDegree = 'C';
    double currentTemp = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nest_thermostat);

        stateSpinner = (Spinner) findViewById(R.id.state_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.state_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);
        tempBar = (SeekBar) findViewById(R.id.temp_bar);
        temperature = (TextView) findViewById(R.id.tempView);
        cButton = (Button) findViewById(R.id.c_button);
        fButton = (Button) findViewById(R.id.f_button);

        initializeMessagesApi(Token.sToken.getToken());
        this.deviceId = getIntent().getStringExtra(LedSmartLightActivity.KEY_DEVICE_ID);
        getLatestMsg();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(String.format(FirehoseWebSocketListenerService.DEVICE_MESSAGE_BROADCAST_ACTION_PATTERN, deviceId));
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void sendStateAction(int state) {
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
        commonSendAction(actionArray);
    }

    private void sendTemperature() {
        final ActionArray actionArray = new ActionArray();
        Action action = new Action();
        if (currentDegree == 'C') {
            action.setName("setTemperature");
        } else {
            action.setName("setTemperatureInFahrenheit");
        }
        action.putParametersItem("temp", currentTemp);
        actionArray.addActionsItem(action);
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

    private void initializeMessagesApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        messagesApi = new MessagesApi(mApiClient);
    }

    private void configureTemp() {
        double temp = currentTemp;
        if (currentDegree == 'C') {
            cButton.setSelected(true);
            fButton.setSelected(false);
            tempBar.setMax(maxC-minC);
            temp -= minC;
        } else {
            cButton.setSelected(false);
            fButton.setSelected(true);
            tempBar.setMax(maxF-minF);
            temp -= minF;
        }
        tempBar.setProgress((int) temp);
        temperature.setText(String.valueOf((int)currentTemp) + "°" + currentDegree);
    }

    private void cToF() {
        currentDegree = 'F';
        currentTemp = currentTemp * 1.8 + 32;
        configureTemp();
    }

    private void fToC() {
        currentDegree = 'C';
        currentTemp = (currentTemp - 32) / 1.8;
        configureTemp();
    }

    private void getLatestMsg() {
        final String tag = "Thermostat getLastNormalizedMessagesAsync";
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
        if (jsonString != null && jsonString != "") {
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
                final int temperatureValue = (int) Math.round(json.getDouble("target_temperature_c"));
                currentTemp = temperatureValue;

                final boolean canHeat = json.getBoolean("can_heat");
                final boolean canCool = json.getBoolean("can_cool");
                state = Mode.parseMode(canHeat, canCool).ordinal();
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initUI() {
        stateSpinner.setSelection(state);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != state) {
                    sendStateAction(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (currentDegree == 'C') {
                        currentTemp = progress + minC;
                    } else {
                        currentTemp = progress + minF;
                    }
                    configureTemp();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendTemperature();
                configureTemp();
            }
        });
        ;
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cButton.isSelected()) {
                    fToC();
                    cButton.setSelected(true);
                    fButton.setSelected(false);
                }
            }
        });
        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fButton.isSelected()) {
                    cToF();
                    fButton.setSelected(true);
                    cButton.setSelected(false);
                }

            }
        });

        configureTemp();
    }

    @Override
    public View getSnackbarView() {
        return findViewById(R.id.state_spinner);
    }

    @Override
    public void delegate(final JSONObject json) {
        updateState(json);
        initUI();
    }
}
