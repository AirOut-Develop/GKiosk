package com.jwlryk.ogkiosk.Support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.jwlryk.ogkiosk.MainActivity;
import com.jwlryk.ogkiosk.Util.Dlog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class StoreWebSocketClient extends WebSocketClient {

    private static final String TAG = "StoreWebSocketClient";

    private Context context; // Context는 이제 null일 수 있음

    public StoreWebSocketClient(URI serverUri, Context context) {
        super(serverUri);
        this.context = context; // Context 초기화, null 가능
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Dlog.i("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        Dlog.i("Received message: " + message);

        boolean DevMode = MainActivity.sharedPreferences.getBoolean("DevMode", false);
        // 메시지 파싱 로직
        try {
            // 메시지 형식: "[채널]채널=상품번호"
            String[] parts = message.split("=");
            if (parts.length == 2) {
                if (parts[1].trim().matches("^-?\\d+$")) {
                    // 정수 형식에 맞는 경우
                    int productNumber = Integer.parseInt(parts[1].trim());
                    if(!DevMode){

                        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
                        editor.putString("SelectedItemNumber", String.format("%d",productNumber));
                        editor.apply();

//                        ProductList.vendingControl(productNumber);
                    }else{
                    }


                } else if(parts[1].trim().equals("KioskRefresh")){
//                    if(context != null){
//                        Intent intent = new Intent(context, ProductList.class);
//                        intent.putExtra("button_id", "btn1");  // 버튼 구별자 전달
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        context.startActivity(intent);
//                    }
                }
            }
        } catch (NumberFormatException e) {
            Dlog.e("Error parsing product number from message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Dlog.i("Disconnected from server");
    }

    @Override
    public void onError(Exception ex) {
        Dlog.i("WebSocket error: " + ex.getMessage());
    }
    /**
     * 서버로 메시지를 보내는 메소드
     * @param message 서버로 보낼 메시지
     */
    public void sendMessageToServer(String message) {
        if (this.isOpen()) { // WebSocket이 열려있는지 확인
            this.send(message);
        } else {
            Log.e(TAG, "WebSocket is not open");
        }
    }

}