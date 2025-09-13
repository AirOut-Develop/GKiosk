package com.jwlryk.gkiosk.remote.wss;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class StoreWebSocketClient extends WebSocketClient {
    private static final String TAG = "StoreWSS";
    private final WsEventListener listener;

    public StoreWebSocketClient(URI serverUri, WsEventListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "Connected");
        if (listener != null) listener.onOpen();
    }

    @Override public void onMessage(String message) {
        Log.i(TAG, "Received: " + message);
        if (listener != null) listener.onRawMessage(message);
        try {
            String[] parts = message.split("=");
            if (parts.length == 2) {
                if ("KioskRefresh".equals(parts[1].trim())) {
                    if (listener != null) listener.onRefresh();
                    return;
                }
                if (parts[1].trim().matches("^-?\\d+$")) {
                    int productNumber = Integer.parseInt(parts[1].trim());
                    if (listener != null) listener.onProductMessage(productNumber);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
        }
    }

    @Override public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "Disconnected: " + reason);
        if (listener != null) listener.onClose(code, reason, remote);
    }

    @Override public void onError(Exception ex) {
        Log.e(TAG, "Error", ex);
        if (listener != null) listener.onError(ex);
    }
}

