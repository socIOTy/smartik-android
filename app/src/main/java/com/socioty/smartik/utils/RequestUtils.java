package com.socioty.smartik.utils;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.socioty.smartik.model.Token;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Willian on 2017-04-18.
 */

public final class RequestUtils {

    public static class BaseJsonRequest extends JsonObjectRequest {

        public BaseJsonRequest(final int method, String url, final JSONObject jsonRequest,
                               final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map getHeaders() throws AuthFailureError {
            final Map headers = new HashMap<>();
            final String email = Token.sToken.getEmail();
            headers.put("Authorization", "Basic " + Base64.encodeToString(email.getBytes(), Base64.NO_WRAP));
            return headers;
        }
    }

    public static final String BACKEND_BASE_URL = "https://smartik.herokuapp.com";
    public static final String BACKEND_ACCOUNT_BY_MAIL_RESOURCE_PATTERN = BACKEND_BASE_URL + "/rest/account/%s";
    public static final String BACKEND_DEVICE_MAP_RESOURCE = BACKEND_BASE_URL + "/rest/deviceMap";
    public static final String BACKEND_DEVICE_RESOURCE = BACKEND_BASE_URL + "/rest/device";
    public static final String BACKEND_DEVICE_RESOURCE_ID_PATTERN = BACKEND_DEVICE_RESOURCE + "/%s";
    public static final String DEVICE_MAP_PROPERTY = "deviceMap";

    private static RequestQueue queue;

    private RequestUtils() {
        throw new AssertionError();
    }


    public static void initialize(final Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public static <T> void addRequest(Request<T> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

}
