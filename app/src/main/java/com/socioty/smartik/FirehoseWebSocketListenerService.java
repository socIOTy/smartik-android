package com.socioty.smartik;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import cloud.artik.model.Acknowledgement;
import cloud.artik.model.ActionOut;
import cloud.artik.model.MessageOut;
import cloud.artik.model.WebSocketError;
import cloud.artik.websocket.ArtikCloudWebSocketCallback;
import cloud.artik.websocket.FirehoseWebSocket;

public class FirehoseWebSocketListenerService extends Service {

    public static final String DEVICE_MESSAGE_BROADCAST_ACTION_PATTERN = "DEVICE_MESSAGE_%s";

    public static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";
    public static final String USER_ID_KEY = "USER_ID";
    public static final String DEVICE_IDS_KEY = "DEVICE_IDS";

    public static final String BROADCAST_MESSAGE_KEY = "MESSAGE";

    private FirehoseWebSocket socket;

    public FirehoseWebSocketListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            final Bundle data = intent.getExtras();
            final String accessToken = data.getString(ACCESS_TOKEN_KEY);
            final String deviceIds = data.getString(DEVICE_IDS_KEY);
            final String userId = data.getString(USER_ID_KEY);
            connectSocket(accessToken, deviceIds, userId);
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSocket();
    }

    private void connectSocket(final String accessToken, final String deviceIds, final String userId) {
        try {
            socket = new FirehoseWebSocket(accessToken, null, deviceIds, null, userId,
                    new ArtikCloudWebSocketCallback() {

                        @Override
                        public void onAck(Acknowledgement ack) {
                            //Nothing to do
                        }

                        @Override
                        public void onAction(ActionOut action) {
                            //Nothing to do
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            System.out.printf("onClose: %d %s %s\n", code, reason,
                                    remote);
                        }

                        @Override
                        public void onError(WebSocketError error) {
                            System.err.println("onError: " + error);
                        }

                        @Override
                        public void onMessage(MessageOut message) {
                            sendBroadcastMessage(message);
                        }

                        @Override
                        public void onOpen(int httpStatus, String httpStatusMessage) {
                            System.out.printf("onOpen: %d %s\n", httpStatus,
                                    httpStatusMessage);
                        }

                        @Override
                        public void onPing(long timestamp) {
                            System.out.printf("ping: %d\n", timestamp);
                        }

                    });


            socket.connect();
        } catch (final IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendBroadcastMessage(final MessageOut message) {
        final Intent intent = new Intent();
        intent.setAction(String.format(DEVICE_MESSAGE_BROADCAST_ACTION_PATTERN, message.getSdid()));

        final Bundle bundle = new Bundle();
        bundle.putString(BROADCAST_MESSAGE_KEY, new JSONObject(message.getData()).toString());
        intent.putExtras(bundle);
        System.out.printf("Broadcasting: %s\n", intent.getAction());

        sendBroadcast(intent);
    }

}
