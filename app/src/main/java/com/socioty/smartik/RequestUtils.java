package com.socioty.smartik;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Willian on 2017-04-18.
 */

public final class RequestUtils {

    private static RequestQueue queue;

    private RequestUtils() {
        throw new AssertionError();
    }


    public static void initialize(final Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public static <T> void addRequest(Request<T> request) {
        queue.add(request);
    }

}
