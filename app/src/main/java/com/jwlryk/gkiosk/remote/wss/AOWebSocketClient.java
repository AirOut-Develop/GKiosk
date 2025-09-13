package com.jwlryk.gkiosk.remote.wss;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AOWebSocketClient extends WebSocketClient {
    private static final String TAG = "AOWSS";
    private final WsEventListener listener;

    public AOWebSocketClient(URI serverUri, WsEventListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "Connected");
        if (listener != null) listener.onOpen();
    }

    @Override public void onMessage(String message) {
        // Some servers send JSON with { data: [int,...] } that encodes another JSON string.
        if (listener != null) listener.onRawMessage(message);
        try {
            JSONObject eventData = new JSONObject(message);
            JSONArray data = eventData.optJSONArray("data");
            if (data != null) {
                byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                String jsonString = StandardCharsets.UTF_8.decode(buffer).toString();
                String[] stringNumbers = jsonString.replace("[", "").replace("]", "").split(",");
                StringBuilder decoded = new StringBuilder();
                for (String s : stringNumbers) {
                    if (s == null || s.trim().isEmpty()) continue;
                    int codePoint = Integer.parseInt(s.trim());
                    decoded.append((char) codePoint);
                }
                JSONObject inner = new JSONObject(new JSONTokener(decoded.toString()));
                String msgValue = inner.optString("msg", null);
                if (msgValue != null) {
                    handleMsg(msgValue);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Message parse error", e);
        }
    }

    private void handleMsg(String msgValue) {
        Log.i(TAG, "WSS msg: " + msgValue);
        try {
            String[] parts = msgValue.split("=");
            if (parts.length == 2) {
                if ("Refresh-Kiosk".equals(parts[1].trim())) {
                    if (listener != null) listener.onRefresh();
                    return;
                }
                if (parts[1].trim().matches("^-?\\d+$")) {
                    int productNumber = Integer.parseInt(parts[1].trim());
                    if (listener != null) listener.onProductMessage(productNumber);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "handleMsg error", e);
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

