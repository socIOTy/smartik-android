package com.socioty.smartik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.socioty.smartik.model.DeviceMap;
import com.socioty.smartik.model.Token;
import com.socioty.smartik.utils.JsonUtils;
import com.socioty.smartik.utils.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.UserEnvelope;

public class LoginActivity extends AppCompatActivity {

    private static final String ARTIK_CLOUD_AUTH_BASE_URL = "https://accounts.artik.cloud";
    private static final String CLIENT_ID = "f68c037951cd422481de491b4a1e153f";// AKA application id
    private static final String REDIRECT_URL = "http://localhost:8080";

    private View mLoginView;
    private WebView mWebView;
    private UsersApi usersApi;
    private Token token;

    private CustomProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new CustomProgressDialog(this);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setVisibility(View.GONE);
        mLoginView = findViewById(R.id.ask_for_login);
        mLoginView.setVisibility(View.VISIBLE);
        Button button = (Button) findViewById(R.id.btn);

        RequestUtils.initialize(this);

        token = Token.get(getApplicationContext());
        if (Token.sToken.getToken() != null) {
            loadToken(Token.sToken.getToken());
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    loadWebView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        mLoginView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String uri) {
                if (uri.startsWith(REDIRECT_URL)) {
                    // Login succeed or back to login after signup

                    if (uri.contains("access_token=")) { //login succeed
                        // Redirect URL has format http://localhost:8000/acdemo/index.php#expires_in=1209600&token_type=bearer&access_token=xxxx
                        // Extract OAuth2 access_token in URL
                        String[] sArray = uri.split("access_token=");
                        String strHavingAccessToken = sArray[1];
                        String accessToken = strHavingAccessToken.split("&")[0];
                        String[] sExpArray = uri.split("expires_in=");
                        String strExp = sExpArray[1];
                        long expIn = Long.parseLong(strExp);
                        long exp = System.currentTimeMillis() / 1000 + expIn;
                        System.out.println("token: " + accessToken);
                        token.setToken(accessToken, exp, getApplicationContext());
                        token = Token.sToken;
                        mLoginView.setVisibility(View.VISIBLE);
                        mWebView.setVisibility(View.GONE);
                        loadToken(accessToken);
                    } else { // No access token available. Signup finishes and user clicks "Back to login"
                        // Example of uri: http://localhost:8000/acdemo/index.php?origin=signup&status=login_request
                        //
                        eraseAuthThenLogin();
                    }
                    return true;
                }
                // Load the web page from URL (login and grant access)
                return super.shouldOverrideUrlLoading(view, uri);
            }
        });

        String url = getAuthorizationRequestUri();
        mWebView.loadUrl(url);
    }

    public String getAuthorizationRequestUri() {
        //https://accounts.artik.cloud/authorize?client=mobile&client_id=xxxx&response_type=token&redirect_uri=http://localhost:8000/acdemo/index.php
        return ARTIK_CLOUD_AUTH_BASE_URL + "/authorize?client=mobile&response_type=token&" +
                "client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URL;
    }

    private void startMessageActivity() {
        Intent msgActivityIntent = new Intent(this, ControlPanelActivity.class);
        startActivity(msgActivityIntent);
    }

    private void eraseAuthThenLogin() {
        CookieManager.getInstance().removeAllCookie();
        mWebView.loadUrl(getAuthorizationRequestUri());
    }


    private void loadToken(final String accessToken) {
        dialog.show();
        initializeApi(accessToken);
        getUser();
    }

    private void getUser() {
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
                    token.setUserId(getApplicationContext(), result.getData().getId());
                    final String email = result.getData().getEmail();
                    token.setEmail(email);
                    final JsonObjectRequest jsObjRequest = new RequestUtils.BaseJsonRequest
                            (Request.Method.GET, String.format(RequestUtils.BACKEND_ACCOUNT_BY_MAIL_RESOURCE_PATTERN, email), null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(final JSONObject response) {
                                    try {
                                        final DeviceMap deviceMap = JsonUtils.GSON.fromJson(response.getJSONObject(RequestUtils.DEVICE_MAP_PROPERTY).toString(), DeviceMap.class);
                                        token.setDeviceMap(deviceMap);
                                        startMessageActivity();
                                        dialog.dismiss();
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
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

    private void initializeApi(final String accessToken) {
        final ApiClient mApiClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: artikcloud_oauth
        final OAuth artikcloud_oauth = (OAuth) mApiClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(accessToken);

        usersApi = new UsersApi(mApiClient);
    }
}
