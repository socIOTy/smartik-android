package com.socioty.smartik;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Willian on 2017-03-29.
 */

public class DeviceMessageBroadcastReceiver extends BroadcastReceiver {

    public interface Delegate {

        View getSnackbarView();

        void delegate(final JSONObject json);
    }

    private static final String TAG = "DeviceMessageBroadRec";

    private final Delegate delegate;
    private boolean ignoreNext;

    public DeviceMessageBroadcastReceiver(final Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (ignoreNext) {
            ignoreNext = false;
            return;
        }

        final String action = intent.getAction();
        Log.v(TAG,"received action:" + action);

        try {
            delegate.delegate(new JSONObject(intent.getExtras().getString(FirehoseWebSocketListenerService.BROADCAST_MESSAGE_KEY)));
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }

        Snackbar.make(delegate.getSnackbarView(), "Device status was updated!", Snackbar.LENGTH_LONG).show();
    }

    public void ignoreNext() {
        this.ignoreNext = true;
    }
}
