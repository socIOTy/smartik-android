package com.socioty.smartik;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

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

public class NestThermostatActivity extends AppCompatActivity {

    Spinner stateSpinner;
    SeekBar tempBar;
    TextView temperature;
    Button cButton;
    Button fButton;

    private static final int minC = 10;
    private static final int maxC = 40;
    private static final int minF = 50;
    private static final int maxF = 104;

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
        initializeMessagesApi(Token.sToken.getToken());
        this.deviceId = getIntent().getStringExtra(LedSmartLightActivity.KEY_DEVICE_ID);

        tempBar = (SeekBar) findViewById(R.id.temp_bar);

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

        temperature = (TextView) findViewById(R.id.tempView);
        cButton = (Button) findViewById(R.id.c_button);
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
        fButton = (Button) findViewById(R.id.f_button);
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


}
