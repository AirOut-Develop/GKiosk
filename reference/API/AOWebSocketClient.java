package com.jwlryk.ogkiosk.API;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jwlryk.ogkiosk.MainActivity;
import com.jwlryk.ogkiosk.ProductList;
import com.jwlryk.ogkiosk.Util.Dlog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AOWebSocketClient extends WebSocketClient {

    private static final String TAG = "AOWebSocketClient";

    private Context context; // Context는 이제 null일 수 있음

    public AOWebSocketClient(URI serverUri, Context context) {
        super(serverUri);
        this.context = context; // Context 초기화, null 가능
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Dlog.i("Connected to server");
    }

    @Override
    public void onMessage(String message) {
//        Dlog.i("WSS Received message: " + message);

        /* JavsScript 코드 */
        try {
            // 메시지를 JSON으로 파싱
            JSONObject eventData = new JSONObject(message);

//            Dlog.i("WSS eventData " + eventData);
            // data 필드를 byte 배열로 추출
            byte[] data = eventData.getJSONArray("data").toString().getBytes(StandardCharsets.UTF_8);

            // ByteBuffer를 사용하여 byte 배열을 처리
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // ByteBuffer의 내용을 문자열로 변환 (TextDecoder와 유사)
            String jsonString = StandardCharsets.UTF_8.decode(buffer).toString();

            // 대괄호 제거 후, 쉼표 기준으로 분할하여 문자열 배열로 변환
            String[] stringNumbers = jsonString.replace("[", "").replace("]", "").split(",");

            // 문자열 배열을 int 배열로 변환
            int[] intArray = new int[stringNumbers.length];
            for (int i = 0; i < stringNumbers.length; i++) {
                intArray[i] = Integer.parseInt(stringNumbers[i].trim()); // 문자열을 int로 변환
            }

            StringBuilder decodedString = new StringBuilder();
            for (int codePoint : intArray) {
                decodedString.append((char) codePoint);  // 아스키/UTF-8 코드 포인트를 문자로 변환
            }

            // 변환된 JSON 문자열 출력
//            Dlog.i("Decoded JSON String: " + decodedString);

            String getReceivedData = decodedString.toString();

            try {
                // JSON 문자열을 다시 JSON 객체로 파싱
                JSONObject jsonData = new JSONObject(new JSONTokener(getReceivedData));

                // 유효한 JSON이므로 처리
//                Dlog.i("WSS Received JSON Data ALL: " + jsonData.toString());

                String msgValue = jsonData.getString("msg");
                Dlog.i("WSS Received JSON Data msg: " + msgValue);

                // 메시지 형식: "[채널]채널=상품번호"
                String[] parts = msgValue.split("=");
                if(parts[0].equals("PROD")){
                    if (parts.length == 2) {
                        if (parts[1].trim().matches("^-?\\d+$")) {
                            //PROD=101

                            // 정수 형식에 맞는 경우
                            int productNumber = Integer.parseInt(parts[1].trim());

                            // 정수를 문자열로 변환
                            String numStr = Integer.toString(productNumber);

                            // 첫 번째 숫자 추출
                            int venNumber = Integer.parseInt(numStr.substring(0, 1));

                            /* 자판기 제어 관련 */
                            Dlog.i("자판기 제어 " + productNumber);
                            if(venNumber == 1 || venNumber == 5){
                                MainActivity.SendVenEachData(productNumber, 42);
                            } else if(venNumber == 2 || venNumber ==3 || venNumber ==7){
                                MainActivity.SendVenEachData(productNumber, 36);

                            } else if(venNumber == 4 || venNumber ==6){
                                MainActivity.SendVenEachData(productNumber, 20);
                            }

                        } else if(parts[1].trim().equals("Refresh-Kiosk")){
                            //PROD=Refresh-Kiosk
                            if(context != null){

                                Dlog.i("자판기 화면 갱신");
                                Intent intent = new Intent("com.jwlryk.ogkiosk.UPDATE_DEVICE");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        }


                    }
                } else if(parts[0].trim().equals("PAY") && parts[1].trim().equals("CANCEL") ){
                    //PAY=CANCEL=price,_approvalNumber,_approvalDate,_transactionSerial
                    Dlog.i("PAY=CANCEL Check :: " + parts[2]);
                    //PAY=CANCEL=price,approvalNumber,approvalDate,transactionSerial

                    // ProductList 실행 중이면 MainActivity로 이동
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);

                    String[] paymentDataArray = parts[2].trim().split(",");
                    if(paymentDataArray[0].matches("^-?\\d+$")){
                        MainActivity.PaymentCancel_RealMode = true;
                        MainActivity.sendCardCancelData_RealMode(paymentDataArray[0], paymentDataArray[1], paymentDataArray[2],paymentDataArray[3],paymentDataArray[4],paymentDataArray[5]);
                    }

                } else if(parts[0].trim().equals("CONTROL") && parts[1].trim().equals("RESTART")){

                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    context.startActivity(mainIntent);
                    System.exit(0);

                }


            } catch (JSONException e) {
                // 유효하지 않은 JSON 문자열일 경우 로그를 남김
                Dlog.e("WSS Received Normal Data: " + getReceivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Dlog.e("Error processing message: " + e.getMessage());
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