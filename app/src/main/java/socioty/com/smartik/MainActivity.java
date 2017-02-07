package socioty.com.smartik;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

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
import cloud.artik.model.Message;
import cloud.artik.model.MessageIDEnvelope;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void sendAction(boolean active) {

//        ApiClient mApiClient = new ApiClient();
//        mApiClient.setAccessToken("a9fda4d52acc4b5c8efda8169c5a33b9");


        ApiClient mApiClient = Configuration.getDefaultApiClient();

// Configure OAuth2 access token for authorization: artikcloud_oauth
        OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken("a9fda4d52acc4b5c8efda8169c5a33b9");

        MessagesApi apiInstance = new MessagesApi(mApiClient);
        Message data = new Message(); // Actions | Actions that are passed in the body
        data.setSdid("61c976c37c604467a1e6d7d963723545");
//        data.getData().addActionsItem(new Action().name(active ? "setOn" : "setOff"));
        data.getData().put("intensity", active ? 100 : 0);
        try {
            apiInstance.sendMessageAsync(data, new ApiCallback<MessageIDEnvelope>() {

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
