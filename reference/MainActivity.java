package com.jwlryk.ogkiosk;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jwlryk.ogkiosk.API.AOWebSocketClient;
import com.jwlryk.ogkiosk.API.ApiHelper;
import com.jwlryk.ogkiosk.API.ProductSale;
import com.jwlryk.ogkiosk.API.Sale;
import com.jwlryk.ogkiosk.API.Store;
import com.jwlryk.ogkiosk.Util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;


public class MainActivity extends AppCompatActivity {

    /* DEV MODE SETTING */
    private ToggleButton devSettingToggleButton_MASTER;
    public static final String DEVMODE_MASTER_SETTING_KEY = "DEVMODE_MASTER_SETTING";

    private ToggleButton devSettingToggleButton_Payment;
    public static final String DEVMODE_PAYMENT_CARD_KEY = "DEVMODE_PAYMENT_CARD";


    /** WSS **/

    public static AOWebSocketClient mWebSocketClient;

    private Handler wssHandler = new Handler(Looper.getMainLooper());
    private Runnable runnable; // 10초 타이머를 위한 Runnable

    Button wss_message_01;
    Button wss_message_02;
    EditText wss_receive_msg;

    static int MOVE_TIEM = 3*1000;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences settingPreferences;

    private static final String TAG = "MainActivity";

    private TextView texView_StoreInfo;
    private EditText edit_companyNumber;
    private Button button_get_store, button_KioskModeControl;


    private boolean isActivated = false; // 활성화 상태 플래그
    private Handler handler = new Handler(); // Handler를 사용해 10초 후 이동
    private Runnable navigateToProductListRunnable;
    private static NiceVCATClass niceVCAT;

    /* 자판기 스피너 */
    Spinner spinnerVendNumber;
    int selectedVendNumber = 1;  // 기본값을 설정 (1번 자판기)

    /* 시리얼 연결 */
    private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private TextView textView_ven_isConnect, textView_bill_isConnect;

    Boolean VEN_ACTIVE= false, BILL_ACTIVE = false;

    // 요청 데이터 및 응답 데이터를 표시할 EditText 변수 (자판기 관련)
    private static EditText editVenRequestData;
    private EditText editVenResultData;
    // 요청 데이터 및 응답 데이터를 표시할 EditText 변수 (지폐 투입기 관련)
    private EditText editBillRequestData, editBillResultData;

    EditText Send_Message_Ven;
    Button Setting_button_Ven,Send_button_Ven_All, Setting_button_VenNumber, Send_button_Ven, Setting_button_Bill;

    public static EditText edit_bill_price;
    public static TextView textView_bill_input;
    public static TextView textView_bill_output;
    public static TextView textView_bill_sum;
    Button button_bill_init;
    Button button_bill_check;
    Button button_bill_stop;
    Button button_bill_set;
    Button button_bill_reset;

    public static int getBill_1000 = 0;
    public static int getBill_5000 = 0;
    public static int getBill_10000 = 0;
    public static int getBill_input = 0;
    public static int getBill_output = 0;
    public static int getBill_out_counter = 0;

    public static int getBill_Sum = 0;

    public static int setBill_price = 0;

    public static boolean setBill_test_active = false;
    public static boolean setBill_test_over_flow_trigger = false;  // 오버플로우 트리거 추가
    public static boolean setBill_test_cash_overflow_complete = false;  // 결제 완료 플래그 추가



    Spinner Ven_Port_spinner, Bill_Port_spinner;
    protected SerialPort serialPortVen, serialPortBill;
    static String[] SERIAL_PORT_NAME_Ven, SERIAL_PORT_NAME_Bill;
    static String SERIAL_PORT_NAME_Ven_String, SERIAL_PORT_NAME_Bill_String, Port_split_Ven = "", Port_split_Bill = "";
    static int SERIAL_BAUDRATE_Ven = 9600, SERIAL_BAUDRATE_Bill = 9600;
    public static InputStream inputStreamVen, inputStreamBill;
    public static OutputStream outputStreamVen, outputStreamBill;
    SerialThread serialThreadVen, serialThreadBill;
    private final SerialPortFinder Port_Finder = new SerialPortFinder();
    ArrayAdapter<String> arrayAdapter;
    String coverted, result, laststring;
    int countVen = 0, countBill = 0;
    boolean checkVen = true, checkBill = true;

    /* 카드 데이터 처리 */
    EditText edit_card_testPrice, edit_card_requestData, edit_card_resultData;
    EditText edit_card_transactionSerial, edit_card_approvalNumber, edit_card_approvalDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sharedPreferences = getSharedPreferences("OGAM", MODE_PRIVATE);
        settingPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        /** DEV MODE SETTING :: START */
        // ToggleButton 초기화
        devSettingToggleButton_MASTER = findViewById(R.id.devSettingToggleButton_MASTER);
        devSettingToggleButton_Payment = findViewById(R.id.devSettingToggleButton_Payment);

        // ToggleButton 상태가 변경될 때마다 SharedPreferences에 값 저장
        devSettingToggleButton_MASTER.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingPreferences.edit();
            editor.putBoolean(DEVMODE_MASTER_SETTING_KEY, isChecked);
            editor.apply(); // 변경 사항 저장
            Toast.makeText(MainActivity.this, "개발자 모드 : " + isChecked, Toast.LENGTH_SHORT).show();
        });

        devSettingToggleButton_Payment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingPreferences.edit();
            editor.putBoolean(DEVMODE_PAYMENT_CARD_KEY, isChecked);
            editor.apply(); // 변경 사항 저장
            Toast.makeText(MainActivity.this, "테스트 결재 모드: " + isChecked, Toast.LENGTH_SHORT).show();
        });

        /** DEV MODE SETTING :: END */

        /** WSS **/
        wss_Init(this);
        WSSCheckTimer();
        wss_sendMessage();

        edit_card_requestData = findViewById(R.id.edit_card_requestData);
        edit_card_resultData = findViewById(R.id.edit_card_resultData);

        edit_card_testPrice = findViewById(R.id.edit_card_testPrice);
        edit_card_transactionSerial = findViewById(R.id.edit_card_transactionSerial);
        edit_card_approvalNumber = findViewById(R.id.edit_card_approvalNumber);
        edit_card_approvalDate = findViewById(R.id.edit_card_approvalDate);

        // NiceVCATClass의 콜백 설정
        niceVCAT = new NiceVCATClass(this);

        // UI 요소 초기화
        texView_StoreInfo = findViewById(R.id.texView_StoreInfo);
        edit_companyNumber = findViewById(R.id.edit_companyNumber);
        button_get_store = findViewById(R.id.button_get_store);
        button_KioskModeControl = findViewById(R.id.button_KioskModeControl);

        // 세팅 자판기 번호 스피너
        // Spinner 초기화
        spinnerVendNumber = findViewById(R.id.spinner_vendNumber);

        // Spinner의 값이 선택될 때 호출되는 리스너 설정
        spinnerVendNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 선택된 항목의 index 값을 저장 (1번, 2번, ...으로 대응됨)
                selectedVendNumber = position + 1; // Spinner는 0부터 시작하므로 +1
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택된 값이 없을 때 처리 (필요 시 추가)
            }
        });


        // Setting Serial Port UI & Event
        SettingProtInterface();

        // '불러오기' 버튼 클릭 시 동작
        button_get_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyNumber = edit_companyNumber.getText().toString();
                if (!companyNumber.isEmpty()) {
                    // API를 통해 데이터를 불러옴
                    loadStoreData(companyNumber);
                } else {
                    Toast.makeText(MainActivity.this, "사업자 번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 활성화 버튼 클릭 시 상태 토글
        button_KioskModeControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActivationState();
            }
        });


        // 무조건 데브모드는 false 되게
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean(DEVMODE_MASTER_SETTING_KEY, false);
        editor.apply(); // 변경 사항 저장

        // 데이터가 있으면 값을 로드하고 UI에 반영
        boolean isDevMode_Master_Enabled = settingPreferences.getBoolean(DEVMODE_MASTER_SETTING_KEY, false);
        Dlog.i("isDevMode_Master_Enabled :: " + isDevMode_Master_Enabled);
        devSettingToggleButton_MASTER.setChecked(isDevMode_Master_Enabled);
        if(!isDevMode_Master_Enabled){
            LoadedCompanyInfo();
        }

        // 저장된 DEVMODE_PAYMENT_CARD 값을 불러와 초기화
        boolean isDevMode_Payment_Enabled = settingPreferences.getBoolean(DEVMODE_PAYMENT_CARD_KEY, false);
        devSettingToggleButton_Payment.setChecked(isDevMode_Payment_Enabled);

        /**
         *
         * NVCat체크
         *
         * */
//        checkNVCAT();

        Handler handler = new Handler();  // Handler 생성
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNVCAT();  // 30초 후에 실행될 메서드
            }
        }, 10 * 1000);  // 30초 = 30000ms

        /**
         *
         * 자판기 시리얼 체크
         *
         * */

        // 저장된 자판기 포트 설정 불러오기
        String savedVendingPortName = sharedPreferences.getString("VendingPortName", null);
        int savedVendingBaudRate = sharedPreferences.getInt("VendingBaudRate", SERIAL_BAUDRATE_Ven);
        Dlog.i("savedVendingPortName : " + savedVendingPortName);
        // 자판기 포트 설정 확인 및 연결
        if (savedVendingPortName != null) {
            // Spinner에서 저장된 포트 이름으로 설정
            String PortName = setSpinnerSelection(Ven_Port_spinner, savedVendingPortName);
            Port_split_Ven = PortName;
            SERIAL_PORT_NAME_Ven = Port_split_Ven.split(" ");
            SERIAL_PORT_NAME_Ven_String = SERIAL_PORT_NAME_Ven[0];
            SetVenSerialPort(SERIAL_PORT_NAME_Ven_String);
        }


        // 저장된 지폐 투입기 포트 설정 불러오기
        String savedBillPortName = sharedPreferences.getString("BillPortName", null);
        int savedBillBaudRate = sharedPreferences.getInt("BillBaudRate", SERIAL_BAUDRATE_Bill);
        Dlog.i("savedBillPortName : " + savedBillPortName);
        if (savedBillPortName != null) {
            // Spinner에서 저장된 포트 이름으로 설정
            String PortName = setSpinnerSelection(Bill_Port_spinner, savedBillPortName);
            Port_split_Bill = PortName;
            SERIAL_PORT_NAME_Bill = Port_split_Bill.split(" ");
            SERIAL_PORT_NAME_Bill_String = SERIAL_PORT_NAME_Bill[0];
            SetBillSerialPort(SERIAL_PORT_NAME_Bill_String);

        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        boolean DEVMODE_MASTER  = settingPreferences.getBoolean(DEVMODE_MASTER_SETTING_KEY, false);

        if(DEVMODE_MASTER){
            isActivated = false;
            button_KioskModeControl.setText("비활성화");
            button_KioskModeControl.setBackgroundColor(getResources().getColor(R.color.colorDeactivated));
        } else {
            button_KioskModeControl.setText("활성화됨");
            button_KioskModeControl.setBackgroundColor(getResources().getColor(R.color.colorActivated));
//            LoadedCompanyInfo();
        }


        /** WSS **/
        // 액티비티가 다시 시작될 때 타이머 재시작
        WSSTimerReset();
        if(MainActivity.mWebSocketClient != null && MainActivity.mWebSocketClient.isClosed()) {
            MainActivity.wss_Init(this);
            MainActivity.sendWebSocketMessage("PROD=MainActivityNIT");
        }else{
            MainActivity.sendWebSocketMessage("PROD=MainActivityNIT");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    /**
     *
     * NVCAT 체크
     *
     */

    private void checkNVCAT() {
        if (niceVCAT != null) {
            try {

                String sendData = niceVCAT.createSendDataForNVCatRestart();
                niceVCAT.send(sendData);

//                String sendData = niceVCAT.createSendDataForCardReaderInfo();
//                niceVCAT.send(sendData);
            } catch (Exception ex) {
                Dlog.e("NVCat Check Error: " + ex.toString());
            }
        } else {
            Dlog.e("NVCat is not initialized");
        }
    }



    /**
     *
     *
     * 카드 결제 호출
     *
     *
     */


    // 승인 데이터를 전송할 때 호출되는 메서드 예시
    public void onApprovalButtonClick(View view) {
        sendCardApprovalData(); // 승인 데이터를 전송하는 메서드 호출
    }
    // 카드 승인 데이터를 생성하여 전송하는 메서드
    public void sendCardApprovalData() {
        // 카드 승인에 필요한 데이터를 설정
        String money = edit_card_testPrice.getText().toString(); // 거래금액
        String tax = "0"; // 세금
        String bongsa = "0"; // 봉사료
        String halbu = "00"; // 할부
        String apprtid = ""; // 승인 CAT ID
        String myunse = ""; // 면세 여부
        String txtnum = ""; // 거래번호
        String filler = ""; // 여유 필드
        String txt = ""; // 거래 텍스트
        String devicegb = ""; // 장치 구분

        // 승인 데이터를 생성
        String sendData = niceVCAT.createSendDataForCardApproval(money, tax, bongsa, halbu, apprtid, myunse, txtnum, filler, txt, devicegb);

        edit_card_requestData.setText(sendData);
        // 승인 데이터를 전송
        niceVCAT.send(sendData); // requestCode를 설정하여 전송
    }

    // 카드 취소 데이터를 생성하여 전송하는 메서드
    public void onCancelButtonClick(View view){
        // 결제 취소 전 알럿을 표시
        new AlertDialog.Builder(this)
                .setTitle("결제 취소 확인")
                .setMessage("결제를 취소하시겠습니까?")
                .setPositiveButton("승인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 "승인"을 선택한 경우
                        sendCardCancelData_TestMode(edit_card_testPrice.getText().toString(), edit_card_approvalNumber.getText().toString()
                        ,edit_card_approvalDate.getText().toString(), edit_card_transactionSerial.getText().toString());  // 결제 취소 함수 호출
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 "취소"를 선택한 경우 아무런 동작을 하지 않음
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)  // 알럿 아이콘 설정
                .show();
    };
    public void sendCardCancelData_TestMode(String price, String _approvalNumber, String _approvalDate, String _transactionSerial) {
        // 카드 승인에 필요한 데이터를 설정
        String money = price; // 거래금액
        String tax = "0"; // 세금
        String bongsa = "0"; // 봉사료
        String halbu = "00"; // 할부
        String approvalNumber = _approvalNumber; // 승인번호
        String approvalDate = _approvalDate;
        String apprtid = ""; // 승인 CAT ID
        String transactionSerial = _transactionSerial;
        String myunse = ""; // 면세 여부
        String txtnum = ""; // 거래번호
        String filler = ""; // 여유 필드
        String txt = ""; // 거래 텍스트
        String devicegb = ""; // 장치 구분

        // 승인 데이터를 생성
        String sendData = niceVCAT.createSendDataForNoCardCancel(money, tax, bongsa, halbu, approvalNumber, approvalDate, apprtid,transactionSerial, myunse, txtnum, filler, txt, devicegb);

        edit_card_requestData.setText(sendData);
        // 승인 데이터를 전송
        niceVCAT.send(sendData); // requestCode를 설정하여 전송

    }

    public static Boolean PaymentCancel_RealMode = false;
    public static String GlobalCancelPrice = "0";
    public static String GlobalCancelIndex = "0";
    public static String GlobalCancelProductCode = "0";
    public static void sendCardCancelData_RealMode(String price, String _approvalNumber, String _approvalDate, String _transactionSerial, String productIndex, String productCode) {
        // 카드 승인에 필요한 데이터를 설정
        String money = price; // 거래금액
        String tax = "0"; // 세금
        String bongsa = "0"; // 봉사료
        String halbu = "00"; // 할부
        String approvalNumber = _approvalNumber; // 승인번호
        String approvalDate = _approvalDate;
        String apprtid = ""; // 승인 CAT ID
        String transactionSerial = _transactionSerial;
        String myunse = ""; // 면세 여부
        String txtnum = ""; // 거래번호
        String filler = ""; // 여유 필드
        String txt = ""; // 거래 텍스트
        String devicegb = ""; // 장치 구분
        PaymentCancel_RealMode = true;

        GlobalCancelPrice = price; // 거래금액
        GlobalCancelIndex = productIndex;
        GlobalCancelProductCode = productCode;

        // 승인 데이터를 생성
        String sendData = niceVCAT.createSendDataForNoCardCancel(money, tax, bongsa, halbu, approvalNumber, approvalDate, apprtid,transactionSerial, myunse, txtnum, filler, txt, devicegb);

        // 승인 데이터를 전송
        niceVCAT.send(sendData); // requestCode를 설정하여 전송

    }

    private void onSomeEvent_Complete(String productIndex, String productCode, String GlobalCancelProductCode, NiceTransactionData transactionData, String transactionSerial, String approvalNumber, String approvalDate) {

        Dlog.i("onSomeEvent_Complete");
        // 정수를 문자열로 변환
        String numStr =  productIndex;

        // 첫 번째 숫자 추출
        int venNumber = Integer.parseInt(numStr.substring(0, 1));
        // 나머지 숫자 추출
        int productNumber = Integer.parseInt(productIndex);

        int getPrice = Integer.parseInt(GlobalCancelProductCode) * -1;
        /* 서버 데이터 전송 */
        String storeCode = MainActivity.sharedPreferences.getString("StoreCode", null);
        String deviceCode = MainActivity.sharedPreferences.getString("DeviceCode", null);

        ApiHelper apiHelper = new ApiHelper();

        List<ProductSale> productList = new ArrayList<>();
        productList.add(new ProductSale(productNumber, productCode, 1, getPrice, null, null));

        Sale sale = new Sale();
        sale.setStoreCode(storeCode);
        sale.setDeviceCode(deviceCode);
        sale.setPayMethod("CANCEL");
        sale.setRealPrice(getPrice);
        sale.setDiscountPrice(0);
        sale.setTotalPrice(getPrice);
        sale.setPaymentData(transactionData);
        sale.setProducts(productList);
        sale.setOpt00(transactionSerial);
        sale.setOpt01(approvalNumber);
        sale.setOpt02(approvalDate);

        apiHelper.createSale(sale, new ApiHelper.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Dlog.i("CANCEL onSuccess");

            }

            @Override
            public void onFailure(String errorMessage) {
                // 실패 처리
//                ShowAlertDialogAndClose(mainActivity,"결제 후 서버 오류", "상품 결제 정보 등록에 문제가 있습니다.");
                Dlog.i("CANCEL onFailure");
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            String revData = data.getStringExtra("NVCATRECVDATA");

            // 결과값 파싱
            String[] parsedData = niceVCAT.RecvFS(revData);

            // 배열의 모든 필드를 순차적으로 출력
            // NiceTransactionData 객체로 변환
            NiceTransactionData transactionData = new NiceTransactionData(parsedData);

            // 데이터를 JSON으로 변환
            String jsonData = transactionData.toJson();

            // JSON 데이터 출력 (디버깅용)
            Dlog.i("Transaction Data (JSON): \n" + transactionData.toString());


            /* 결과 Message 처리 */
            niceVCAT.handleResult(requestCode, resultCode, data);
            edit_card_resultData.setText(revData);

            // 결과 처리
            if (resultCode == RESULT_OK) {
                // 승인 성공 처리
                if(parsedData[0] == null){
                    // 데이터가 null인 경우 처리
//                MainActivity.ShowAlertDialogConfirm(this,"결제 실패", revData);패
                } else {
                    if (parsedData[2].equals("0000")) {
                        /* 결과값 파싱*/
                        edit_card_transactionSerial.setText(parsedData[20]);
                        edit_card_approvalNumber.setText(parsedData[7]);
                        edit_card_approvalDate.setText(parsedData[8]);


                        if(PaymentCancel_RealMode){
                            onSomeEvent_Complete(GlobalCancelIndex, GlobalCancelProductCode, GlobalCancelPrice, transactionData, parsedData[20], parsedData[7], parsedData[8]);

                            GlobalCancelProductCode = "0";
                            GlobalCancelIndex = "0";
                            GlobalCancelPrice = "0";
                            PaymentCancel_RealMode = false;

                            Intent intent = new Intent(MainActivity.this, ProductList.class);
                            startActivity(intent);
                        }

                    }else{
                        String responseMessage = parsedData[16];
                        MainActivity.ShowAlertDialogConfirm(this,"결제 실패", responseMessage);

                        Intent intent = new Intent(MainActivity.this, ProductList.class);
                        startActivity(intent);
                    }
                }


            } else if (resultCode == RESULT_CANCELED) {

                Dlog.i(revData.contains("NVCAT 재시작") + "");
                if (revData.contains("NVCAT 재시작")) {
                    String sendData = niceVCAT.createSendDataForNVCatRestart();
                    niceVCAT.send(sendData);
                } else if(revData.contains("리더기 연결 상태 체크")){
                    /* 카드단말기 체크 */
                    String sendData = niceVCAT.createSendDataForCardReaderInfo();
                    niceVCAT.send(sendData);
                }

                else if (revData.contains("TITENG 에러코드 : CD")) {
                    // 결제 취소된 경우 처리
                    MainActivity.ShowAlertDialogConfirm(this,"결제 취소", "결제가 취소되었습니다.");
                } else if(parsedData[0] == null){
                    // 데이터가 null인 경우 처리
                    MainActivity.ShowAlertDialogConfirm(this,"결제 실패", revData);
                }
                else if(!parsedData[16].isEmpty()){
                    String responseMessage = parsedData[16];
                    MainActivity.ShowAlertDialogConfirm(this,"결제 실패", responseMessage);
                }
            }
        }


    }


    /**
     *
     *
     * 기본 데이터 호출
     *
     * @param companyNumber
     */


    // Store 데이터를 불러오는 함수
    private void loadStoreData(String companyNumber) {
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.fetchStoreByCompanyNumber(companyNumber, new ApiHelper.ApiCallback<Store>() {
            @Override
            public void onSuccess(Store storeData) {
                // SharedPreferences에 Store 데이터 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("StoreName", storeData.getName());
                editor.putString("StoreCode", storeData.getCode());
                editor.putString("CompanyNumber", storeData.getCompanyNumber());
                editor.putString("CardVanCATID", storeData.getCardVanCATID());
                editor.putInt("StoreStatus", storeData.getStatus());
                editor.apply();

                // UI에 데이터 반영
                texView_StoreInfo.setText(storeData.getName());

                // 활성화 버튼 설정
                checkActivationCondition();

                // 디버깅 로그
                Dlog.d("Store Data 저장 완료: " + storeData.toString());
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "데이터 불러오기 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }



    // 저장된 데이터 불러와서 UI에 표시
    private void LoadedCompanyInfo() {
        // 조건에 따라 '활성화' 버튼 설정
        checkActivationCondition();
    }

    // 조건을 확인하고 활성화 버튼을 설정하는 함수
    private void checkActivationCondition() {
        String storeName = sharedPreferences.getString("StoreName", "");
        texView_StoreInfo.setText(storeName);
        String storeCode = sharedPreferences.getString("StoreCode", null);
        String companyNumber = sharedPreferences.getString("CompanyNumber", null);
        edit_companyNumber.setText(companyNumber);
        int storeStatus = sharedPreferences.getInt("StoreStatus", 0);
        String cardVanCATID = sharedPreferences.getString("CardVanCATID", null);

        // 조건: StoreCode와 CompanyNumber 존재, status == 3, cardVanCATID != null
        if (storeCode != null && companyNumber != null && storeStatus == 3 && cardVanCATID != null) {

            texView_StoreInfo.setText(storeName);

            isActivated = true;
            // 활성화된 경우 10초 후에 ProductList로 이동
            navigateToProductListRunnable = new Runnable() {
                @Override
                public void run() {
                    // ProductList로 이동
                    Intent intent = new Intent(MainActivity.this, ProductList.class);
                    startActivity(intent);
//                    finish();
                }
            };

            button_KioskModeControl.setEnabled(true);
            button_KioskModeControl.setText("활성화됨");
            button_KioskModeControl.setBackgroundColor(getResources().getColor(R.color.colorActivated));
            Toast.makeText(MainActivity.this, "활성화됨", Toast.LENGTH_SHORT).show();
            // 10초 후 자동 이동 설정
            handler.postDelayed(navigateToProductListRunnable, MOVE_TIEM);

        } else {
            isActivated = false;
            button_KioskModeControl.setEnabled(false);
            button_KioskModeControl.setText("비활성화");
            button_KioskModeControl.setBackgroundColor(getResources().getColor(R.color.colorDeactivated));

            // 비활성화 상태일 때 자동 이동을 취소
            if (navigateToProductListRunnable != null) {
                handler.removeCallbacks(navigateToProductListRunnable);
            }

            Toast.makeText(MainActivity.this, "비활성화됨 - 스토어 상태확인", Toast.LENGTH_SHORT).show();
        }
    }

    // 활성화 상태를 토글하는 함수
    private void toggleActivationState() {
        if (isActivated) {
            // 자동 이동을 방지

            // 버튼을 비활성화 시킴
            isActivated = false;
            button_KioskModeControl.setText("비활성화");
            button_KioskModeControl.setBackgroundColor(getResources().getColor(R.color.colorDeactivated));

            if (navigateToProductListRunnable != null) {
                handler.removeCallbacks(navigateToProductListRunnable);  // 자동 이동 취소
                Toast.makeText(MainActivity.this, "비활성화됨 - 자동 이동 취소됨", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "비활성화됨", Toast.LENGTH_SHORT).show();
            }
        }else{
            checkActivationCondition();
        }
    }

    @Override
    public void onBackPressed() {
        // 자원 정리 작업 실행
        // Handler에서 예정된 모든 작업 취소
        if (navigateToProductListRunnable != null) {
            handler.removeCallbacks(navigateToProductListRunnable);
        }
        // 애플리케이션 종료
        finishAffinity(); // 현재 애플리케이션의 모든 액티비티를 종료하고 앱을 종료
        System.exit(0);  // 앱 프로세스 종료
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Activity 종료 시 Handler 콜백 제거
        if (navigateToProductListRunnable != null) {
            handler.removeCallbacks(navigateToProductListRunnable);
        }
        // 자판기 및 지폐 투입기 스레드 종료
        stopThreads();
    }



    private void stopThreads() {
        // 자판기 스레드 종료
        stopThread(serialThreadVen);
        // 지폐 투입기 스레드 종료
        stopThread(serialThreadBill);
    }

    private void stopThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태 복원
            }
        }
    }
    /**
     *
     *
     * Serial Communication
     *
     *
     */
    void SettingProtInterface(){

        textView_ven_isConnect = findViewById(R.id.textView_ven_isConnect);
        textView_bill_isConnect = findViewById(R.id.textView_bill_isConnect);
        // 버튼 및 스피너 초기화
        Setting_button_Ven = findViewById(R.id.button_ven_port_apply);
        Setting_button_VenNumber = findViewById(R.id.button_vendNumberSetting);
        Send_button_Ven = findViewById(R.id.button_vendOpen_each);
        Send_button_Ven_All = findViewById(R.id.button_vendOpen_all);
        Setting_button_Bill = findViewById(R.id.button_bill_port_apply);


        button_bill_init = findViewById(R.id.button_bill_init);
        button_bill_check = findViewById(R.id.button_bill_check);
        button_bill_stop = findViewById(R.id.button_bill_stop);
        button_bill_set = findViewById(R.id.button_bill_set);


        edit_bill_price = findViewById(R.id.edit_bill_price);
        textView_bill_input = findViewById(R.id.textView_bill_input);
        textView_bill_output = findViewById(R.id.textView_bill_output);

        textView_bill_sum = findViewById(R.id.textView_bill_sum);

        button_bill_reset = findViewById(R.id.button_bill_reset);



        Ven_Port_spinner = findViewById(R.id.spinner_ven_ports);
        Bill_Port_spinner = findViewById(R.id.spinner_bill_ports);

        // 요청 데이터 및 응답 데이터를 표시할 EditText를 초기화
        editVenRequestData = findViewById(R.id.edit_ven_requestData);
        editVenResultData = findViewById(R.id.edit_ven_resultData);
        // 요청 데이터 및 응답 데이터를 표시할 EditText를 초기화 (지폐 투입기 관련)
        editBillRequestData = findViewById(R.id.edit_bill_requestData);
        editBillResultData = findViewById(R.id.edit_bill_resultData);

        Send_Message_Ven = findViewById(R.id.edit_vendNumber);

        /* BIll Setting */

//        Receive_Message_Bill.setMovementMethod(new ScrollingMovementMethod());

        // 자판기 포트 스피너 설정
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Port_Finder.getAllDevices());
        Ven_Port_spinner.setAdapter(arrayAdapter);
        Ven_Port_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Port_split_Ven = String.valueOf(adapterView.getItemAtPosition(i));
                SERIAL_PORT_NAME_Ven = Port_split_Ven.split(" ");
                SERIAL_PORT_NAME_Ven_String = SERIAL_PORT_NAME_Ven[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 지폐 투입기 포트 스피너 설정
        Bill_Port_spinner.setAdapter(arrayAdapter);
        Bill_Port_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Port_split_Bill = String.valueOf(adapterView.getItemAtPosition(i));
                SERIAL_PORT_NAME_Bill = Port_split_Bill.split(" ");
                SERIAL_PORT_NAME_Bill_String = SERIAL_PORT_NAME_Bill[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 자판기 시리얼 포트 설정 버튼제
        Setting_button_Ven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (VEN_ACTIVE) {
//                    closeVenConnection();
//                } else {
//
//                }

                SetVenSerialPort(SERIAL_PORT_NAME_Ven_String);
            }
        });


        // 자판기 순서 설정
        Setting_button_VenNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Setting Ven", Toast.LENGTH_SHORT).show();

                byte[] hexData = new byte[]{
                        (byte) 0x02, (byte) 0x06, (byte) 0x06, (byte) 0xDA,
                        (byte) 0x01, (byte) 0x24, (byte) 0x71, (byte) 0x03};

                //0x02 0x06 0x06 0xDA 0x01 0x24 0x71 0x03
                SendDataVen(hexData);

                // 전송할 데이터를 헥사 문자열로 변환하여 표시
                String hexDataToSend = SerialCommunication.byteArrayToHex(hexData);
                // EditText에 보낸 데이터를 표시
                editVenRequestData.setText(hexDataToSend);

                Dlog.d(" SendVenSettingVenNumber Buffer (Hex) = " + hexDataToSend);  // 헥사 형식 로그 출력
            }
        });

        Send_button_Ven_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedVendNumber == 1|| selectedVendNumber == 5){
                    SendVenAll(selectedVendNumber, 42);
                }else if(selectedVendNumber == 2 || selectedVendNumber == 3 || selectedVendNumber == 7){
                    SendVenAll(selectedVendNumber, 36);
                }else if(selectedVendNumber == 4 || selectedVendNumber == 6){
                    SendVenAll(selectedVendNumber, 20);
                }

            }
        });


        // 자판기 데이터 전송 버튼
        Send_button_Ven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!VEN_ACTIVE){
                    Toast.makeText(MainActivity.this, "자판기 Port 설정을 먼저 해주세요.", Toast.LENGTH_SHORT).show();
                } else {

                    // 문자열 길이가 3인지 확인
                    if (Send_Message_Ven.getText().toString().length() == 3) {
                        try {
                            // 문자열을 정수로 변환
                            int productNumber = Integer.parseInt(Send_Message_Ven.getText().toString());

                            int venNumber = Integer.parseInt(Send_Message_Ven.getText().toString().substring(0, 1));

                            if(venNumber == 1 || venNumber ==5){
                                SendVenEachData(productNumber, 42);
                            } else if(venNumber == 2 || venNumber ==3 || venNumber ==7){
                                SendVenEachData(productNumber, 36);

                            } else if(venNumber == 4 || venNumber ==6){
                                SendVenEachData(productNumber, 20);

                            }
                            Toast.makeText(MainActivity.this, "자판기 메세지 보내기 성공", Toast.LENGTH_SHORT).show();


                        } catch (NumberFormatException e) {
                            // 숫자로 변환할 수 없는 문자열인 경우
                            System.out.println("입력된 값은 숫자가 아닙니다.");
                        }
                    } else {
                        // 문자열 길이가 3이 아닌 경우
                        System.out.println("입력된 값은 3자리 숫자가 아닙니다.");
                    }


                }
            }
        });


        // 지폐 투입기 시리얼 포트 설정 버튼
        Setting_button_Bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetBillSerialOnlyPort(SERIAL_PORT_NAME_Bill_String);
            }
        });



        /* 지폐투입기 초기설정 */
        LoadedBillInfo();
    }

    private void LoadedBillInfo(){
        button_bill_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dlog.i("button_bill_init :: " +  "send data");
                byte[] hexData = new byte[]{
                        (byte) 0xfe, (byte) 0x0d, (byte) 0x10, (byte) 0x07,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe4};
                try {
                    SendDataBill(hexData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        button_bill_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dlog.i("button_bill_check :: " +  "send data");
                byte[] hexData = new byte[]{
                        (byte) 0xfe, (byte) 0x0d, (byte) 0x11, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe2};
                try {
                    SendDataBill(hexData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        button_bill_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dlog.i("button_bill_stop :: " +  "send data");
                byte[] hexData = new byte[]{
                        (byte) 0xfe, (byte) 0x0d, (byte) 0x10, (byte) 0x18,
                        (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xeb};
                try {
                    SendDataBill(hexData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        button_bill_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setBill_test_active = false;
                edit_bill_price.setEnabled(true);

                Dlog.i("button_bill_reset :: " +  "send data");
                byte[] hexData = new byte[]{
                        (byte) 0xfe, (byte) 0x0d, (byte) 0x10, (byte) 0x1b,
                        (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe8};
                try {
                    SendDataBill(hexData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        button_bill_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_bill_price.setEnabled(false);
                setBill_price = Integer.parseInt(edit_bill_price.getText().toString());
                setBill_test_active = true;
            }
        });
    }

    // 자판기 시리얼 포트 설정
    void SetVenSerialPort(String name) {
        if (!VEN_ACTIVE) {
            VEN_ACTIVE = true;
            SetSerial(name, SERIAL_BAUDRATE_Ven, true);

            // 스레드 상태 확인 및 필요에 따라 재시작
            if (serialThreadVen == null || !serialThreadVen.isAlive()) {
                startVenThread();
            }
            textView_ven_isConnect.setText(("Connected"));
        } else {
            closeVenConnection();
        }
    }

    void SetVenSerialOnlyPort(String name) {
        savePortSettings(name, SERIAL_BAUDRATE_Ven, true);
//        RestartApp();
    }

    void closeVenConnection() {
        if (VEN_ACTIVE) {
            VEN_ACTIVE = false;
            Setting_button_Ven.setText("선택 포트 적용");
            stopVenThreadAsync();
        }
    }

    void stopVenThreadAsync() {
        if (serialThreadVen != null && serialThreadVen.isAlive()) {
            threadExecutor.submit(() -> {
                serialThreadVen.interrupt();
                try {
                    serialThreadVen.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Restore interrupt status
                } finally {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "자판기 연결 해제 완료", Toast.LENGTH_SHORT).show();
                    });
                    serialThreadVen = null;
                }
            });
        }
    }


    // 지폐 투입기 시리얼 포트 설정
    void SetBillSerialPort(String name) {
        if (!BILL_ACTIVE) {
            BILL_ACTIVE = true;
            SetSerial(name, SERIAL_BAUDRATE_Bill, false);


            // 스레드 상태 확인 및 필요에 따라 재시작
            if (serialThreadBill == null || !serialThreadBill.isAlive()) {
                startBillThread();
            }

            textView_bill_isConnect.setText(("Connected"));
        } else {
            closeBillConnection();
        }
    }

    void SetBillSerialOnlyPort(String name) {
        savePortSettings(name, SERIAL_BAUDRATE_Bill, false);
//        RestartApp();
    }


    void closeBillConnection() {
        if (BILL_ACTIVE) {
            BILL_ACTIVE = false;
            Setting_button_Ven.setText("선택 포트 적용");
            stopBillThreadAsync();
        }
    }

    void stopBillThreadAsync() {
        if (serialThreadBill != null && serialThreadBill.isAlive()) {
            threadExecutor.submit(() -> {
                serialThreadBill.interrupt();
                try {
                    serialThreadBill.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Restore interrupt status
                } finally {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "지폐 투입기 연결 해제 완료", Toast.LENGTH_SHORT).show();
                    });
                    serialThreadBill = null;
                }
            });
        }
    }


    // 공통 시리얼 포트 설정 함수
    void SetSerial(String name, int baudRate, boolean isVendingMachine) {
        try{
            Dlog.d("SetSerial : " + name + " / " + "MODE : " + (isVendingMachine ? "VEN" : "BILL" ));
            SerialPortFinder serialPortFinder = new SerialPortFinder();
            String[] devices = serialPortFinder.getAllDevices();
            String[] devicesPath = serialPortFinder.getAllDevicesPath();

            for (int i = 0; i < devices.length; i++) {
                String device = devices[i];
                if (device.contains(name)) {
                    try {
                        if (isVendingMachine) {
                            serialPortVen = new SerialPort(new File(devicesPath[i]), baudRate, 0);
                        } else {
                            serialPortBill = new SerialPort(new File(devicesPath[i]), baudRate, 0);
                        }
                        // 포트 설정을 SharedPreferences에 저장
                        savePortSettings(name, baudRate, isVendingMachine);

                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (isVendingMachine && serialPortVen != null) {
                inputStreamVen = serialPortVen.getInputStream();
                outputStreamVen = serialPortVen.getOutputStream();
            } else if (!isVendingMachine && serialPortBill != null) {
                inputStreamBill = serialPortBill.getInputStream();
                outputStreamBill = serialPortBill.getOutputStream();
            }
        }catch (Exception e){
            Dlog.e(e.toString());
        }

    }
    // 포트 설정을 SharedPreferences에 저장하는 함수
    private void savePortSettings(String portName, int baudRate, boolean isVendingMachine) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isVendingMachine) {
            editor.putString("VendingPortName", portName);
            editor.putInt("VendingBaudRate", baudRate);
            Toast.makeText(MainActivity.this, "자판기 포트 설정되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            editor.putString("BillPortName", portName);
            editor.putInt("BillBaudRate", baudRate);
            Toast.makeText(MainActivity.this, "투입기 포트 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }
        editor.apply();
    }


    // Spinner에서 저장된 포트 이름과 일치하는 값을 선택하도록 설정하는 함수
    private String setSpinnerSelection(Spinner spinner, String portName) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        String getItemName = "ttysWK1";
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).contains(portName)) {
                spinner.setSelection(i);
                getItemName = adapter.getItem(i);
                break;
            }
        }
        return getItemName;
    }

    // 자판기 데이터 수신 스레드 시작
    void startVenThread() {
        if (inputStreamVen == null) {
            Dlog.d("자판기 inputstream open x");
            return;
        }
        checkVen = true;
        serialThreadVen = new SerialThread(true);
        serialThreadVen.start();
    }

    // 지폐 투입기 데이터 수신 스레드 시작
    void startBillThread() {
        if (inputStreamBill == null) {
            Dlog.d("지폐 투입기 inputstream open x");
            return;
        }
        checkBill = true;
        serialThreadBill = new SerialThread(false);
        serialThreadBill.start();
    }

    // 자판기 데이터 전송

    public static void SendVenSettingVenNumber(int vendingNumber){
        byte[] sendData = SerialCommunication.SettingVenNubmer(vendingNumber);
        SendDataVen(sendData);

        // 전송할 데이터를 헥사 문자열로 변환하여 표시
        String hexDataToSend = SerialCommunication.byteArrayToHex(sendData);
        // EditText에 보낸 데이터를 표시
        editVenRequestData.setText(hexDataToSend);

        Dlog.d(" SendVenSettingVenNumber Buffer (Hex) = " + hexDataToSend);  // 헥사 형식 로그 출력
    }

    public static void SendVenEachData(int productDisplayNumber, int totalProductNumber) {
        Dlog.d(" SendVenEachData productDisplayNumber = " + productDisplayNumber);  // 헥사 형식 로그 출력
        Dlog.d(" SendVenEachData totalProductNumber = " + totalProductNumber);  // 헥사 형식 로그 출력

        int mode = productDisplayNumber >= 700 ? 1 : 0;

        byte vendingNumber = (byte) Character.getNumericValue(String.valueOf(productDisplayNumber).charAt(0));  // 첫 번째 숫자 추출
        int firstZonePerLineCount = 0;
        int firstZoneEnd = 0;
        int secondZonePerLineCount=0;
        int secondZoneEnd=0;
        int thirdZonePerLineCount=0;
        int thirdZoneEnd=0;
        if(totalProductNumber == 42){
            firstZonePerLineCount = 8;
            firstZoneEnd = 32;
            secondZonePerLineCount = 5;
            secondZoneEnd = 42;
        }else if(totalProductNumber == 36){
            firstZonePerLineCount = 4;
            firstZoneEnd = 36;
            secondZonePerLineCount = 0;
            secondZoneEnd = 0;
        }else if(totalProductNumber == 20){
            firstZonePerLineCount = 3;
            firstZoneEnd = 12;
            secondZonePerLineCount = 4;
            secondZoneEnd = 20;
        }
        if(mode == 1) {
            firstZonePerLineCount = 8;
            firstZoneEnd = 16;
            secondZonePerLineCount = 6;
            secondZoneEnd = 36;
        }


        int sendNumber = SerialCommunication.calculateVendingNumberAsString(
                productDisplayNumber, firstZonePerLineCount,firstZoneEnd,secondZonePerLineCount,secondZoneEnd);



        if(productDisplayNumber >= 729) {
            if(productDisplayNumber == 729){
                sendNumber = 40;
            }
            if(productDisplayNumber == 730){
                sendNumber = 42;
            }
            if(productDisplayNumber == 731){
                sendNumber = 44;
            }
            if(productDisplayNumber == 732){
                sendNumber = 46;
            }

            if(productDisplayNumber == 733){
                sendNumber = 50;
            }
            if(productDisplayNumber == 734){
                sendNumber = 52;
            }
            if(productDisplayNumber == 735){
                sendNumber = 54;
            }
            if(productDisplayNumber == 736){
                sendNumber = 56;
            }
        }

        Dlog.d(" SendVenEachData Matching sendNumber = " + sendNumber);  // 헥사 형식 로그 출력
        Dlog.d(" SendVenEachData Matching mode = " + mode);  // 헥사 형식 로그 출력

        byte[] sendData = SerialCommunication.SettingEachProductControl(mode, vendingNumber, (byte) sendNumber);
        SendDataVen(sendData);

        // 전송할 데이터를 헥사 문자열로 변환하여 표시
        String hexDataToSend = SerialCommunication.byteArrayToHex(sendData);
        // EditText에 보낸 데이터를 표시
        editVenRequestData.setText(hexDataToSend);

        Dlog.d(" SendVenEachData Buffer (Hex) = " + hexDataToSend);  // 헥사 형식 로그 출력
    }

    public static void SendVenAll(int vendNumber, int totalProductNumber) {
        // 자판기 번호에 맞는 첫 번째 상품 번호 설정
        int startProductNumber = vendNumber * 100 + 1; // 예: 1번 자판기는 101부터 시작

        // 마지막 상품 번호 설정
        int endProductNumber = startProductNumber + totalProductNumber - 1;

        for (int productDisplayNumber = startProductNumber; productDisplayNumber <= endProductNumber; productDisplayNumber++) {
            // 각 상품 번호에 대해 SendVenEachData 호출
            SendVenEachData(productDisplayNumber, totalProductNumber);

            // 500ms 간격으로 호출
            try {
                Thread.sleep(500);  // 500ms 대기
            } catch (InterruptedException e) {
                e.printStackTrace();  // 에러 로그 출력
            }
        }
    }


    // 자판기 데이터 전송
    public static void SendDataVen(byte[] bSendVal) {
//        byte[] bSendVal = sendString.getBytes(StandardCharsets.UTF_8);

        if (outputStreamVen == null) {
            Dlog.d("outputstream open x");
            return;
        }
        try {
            outputStreamVen.write(bSendVal);
            String send = new String(bSendVal, 0, bSendVal.length, "UTF-8");
            Dlog.d(" Send Message = " + send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 지폐 투입기 데이터 전송
    public void SendDataBill(byte[] bSendVal) {

        // 전송할 데이터를 헥사 문자열로 변환하여 표시
        String hexDataToSend = SerialCommunication.byteArrayToHex(bSendVal);

        // EditText에 보낸 데이터를 표시
        editBillRequestData.setText(hexDataToSend);

        Dlog.d(" SendDataBill Buffer (Hex) = " + hexDataToSend);  // 헥사 형식 로그 출력

        if (outputStreamBill == null) {
            Dlog.d("outputstream open x");
            return;
        }
        try {
            outputStreamBill.write(bSendVal);
            String send = new String(bSendVal, 0, bSendVal.length, "UTF-8");
            Dlog.d(" Send Message = " + send);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void SendDataBillOnlyData(byte[] bSendVal) {

        // 전송할 데이터를 헥사 문자열로 변환하여 표시
        String hexDataToSend = SerialCommunication.byteArrayToHex(bSendVal);


        Dlog.d(" SendDataBill Buffer (Hex) = " + hexDataToSend);  // 헥사 형식 로그 출력

        if (outputStreamBill == null) {
            Dlog.d("outputstream open x");
            return;
        }
        try {
            outputStreamBill.write(bSendVal);
            String send = new String(bSendVal, 0, bSendVal.length, "UTF-8");
            Dlog.d(" Send Message = " + send);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class SerialThread extends Thread {
        private final boolean isVendingMachine;
        private static final int READ_INTERVAL = 100;
        private final byte[] buffer = new byte[63];

        SerialThread(boolean isVendingMachine) {
            this.isVendingMachine = isVendingMachine;
        }

        @Override
        public void run() {
            Dlog.d("SerialThread started : " + (isVendingMachine ? "VEN" : "BILL"));
            while (isVendingMachine ? checkVen : checkBill) {
                try {
                    InputStream stream = isVendingMachine ? inputStreamVen : inputStreamBill;
                    int bytesRead = stream.read(buffer);
                    if (bytesRead > 0) {
                        byte[] receivedData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, receivedData, 0, bytesRead);
                        Dlog.d(" Buffer size = " + String.valueOf(receivedData.length));
                        OnReceiveData(receivedData, bytesRead, isVendingMachine);
                    }
                    Thread.sleep(READ_INTERVAL);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 데이터 수신 처리 함수
    void OnReceiveData(byte[] buffer, int size, boolean isVendingMachine) {
        try {
            coverted = new String(buffer, 0, size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 수신한 데이터를 헥사 문자열로 변환
        String hexReceivedData = SerialCommunication.byteArrayToHex(buffer);

        laststring = coverted;
        Dlog.d(" Receive Buffer (Hex) = " + hexReceivedData);  // 헥사 형식 로그 출력
        Dlog.d(" Receive Message (UTF-8) = " + laststring);  // UTF-8로 변환된 문자열 로그 출력


        runOnUiThread(() -> {
            if (isVendingMachine) {
                // 수신된 데이터를 EditText에 표시 (자판기)
                editVenResultData.setText(hexReceivedData);
            } else {
                // 수신된 데이터를 EditText에 표시 (지폐 투입기)
                editBillResultData.setText(hexReceivedData);
                getReceiveBillData(buffer);
            }
        });
    }
    public static DecimalFormat priceFormatter = new DecimalFormat("###,###");
    void getReceiveBillData(byte[] data){
        /**
         * For Bill Control
         */

        // ((Activity) context).runOnUiThread(() -> {});

        //BILL CHECK
        if(String.format("%02X",data[0]).equals("FE") && String.format("%02X",data[1]).equals("12") ) {
            // Initialize

            //FE 12 00 00 00 00 00 01 00 00 00 00 00 01 01 01
            //FE1200000000000100000000010101
            try {
                MainActivity.getBill_1000 = (int)data[7] * 1000;
                MainActivity.getBill_5000 = (int)data[8] * 5000;
                MainActivity.getBill_10000 = (int)data[9] * 10000;

//                Dlog.i("data[13] = " + data[13]);
//                if((int)data[13] == 1){
//                    MainActivity.getBill_out_counter++;
//                    MainActivity.getBill_output = MainActivity.getBill_out_counter * 1000;
//                }

                MainActivity.getBill_input = getBill_1000 + getBill_5000 + getBill_10000;
                MainActivity.getBill_Sum = MainActivity.getBill_input - MainActivity.getBill_output;
                Dlog.i("data 7 =  1000 :: " + String.format("%02X",data[7]) + " / " + MainActivity.getBill_1000);
                Dlog.i("data 8 =  5000 :: " + String.format("%02X",data[8]) +" / " + MainActivity.getBill_5000);
                Dlog.i("data 9 = 10000 :: " + String.format("%02X",data[9]) +" / " + MainActivity.getBill_10000);
                Dlog.i("data input =  " + MainActivity.getBill_input);
                Dlog.i("data out = " + MainActivity.getBill_output);
                Dlog.i("data sum = " +  MainActivity.getBill_Sum);;

                ((Activity) this).runOnUiThread(() -> {
                    if(MainActivity.textView_bill_input != null){
                        MainActivity.textView_bill_input.setText(String.format(priceFormatter.format(MainActivity.getBill_Sum)));
                    }
                    if(MainActivity.textView_bill_input != null){
                        MainActivity.textView_bill_input.setText(String.format(priceFormatter.format(MainActivity.getBill_Sum)));
                    }

                    if(MainActivity.textView_bill_sum != null){
                        MainActivity.textView_bill_sum.setText(String.format(priceFormatter.format(MainActivity.getBill_Sum)));
                    }
                });



                if(MainActivity.setBill_test_active && !MainActivity.setBill_test_over_flow_trigger && MainActivity.getBill_Sum > MainActivity.setBill_price){

                    MainActivity.setBill_test_over_flow_trigger = true;

                    Dlog.i("MainActivity.getBill_Sum " + MainActivity.getBill_Sum );
                    Dlog.i("MainActivity.setBill_price" + MainActivity.setBill_price );
                    int overflowAmount = (MainActivity.getBill_Sum - MainActivity.setBill_price) / 1000;
                    Dlog.i("overflowAmount :: " +  overflowAmount);
                    byte[] hexData = new byte[]{
                            (byte) 0xfe, (byte) 0x0d, (byte) 0x12, (byte) 0x00,
                            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe0};
                    try {
                        for(int i =0; i < overflowAmount ; i++){
                            SendDataBill(hexData);
                            MainActivity.getBill_out_counter++;
                            MainActivity.getBill_output = MainActivity.getBill_out_counter * 1000;
                            // 500ms(0.5초) 딜레이
                            Thread.sleep(3000);
                        }
                        MainActivity.setBill_test_cash_overflow_complete = true;

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                if(MainActivity.setBill_test_active && (MainActivity.getBill_Sum == MainActivity.setBill_price || MainActivity.setBill_test_cash_overflow_complete)){
                    MainActivity.setBill_test_active = false;
                    MainActivity.setBill_test_over_flow_trigger = false;
                    MainActivity.setBill_test_cash_overflow_complete = false;

                    MainActivity.edit_bill_price.setEnabled(true);
                    MainActivity.getBill_output = 0;
                    MainActivity.getBill_out_counter = 0;

                    Dlog.i("button_bill_reset :: " +  "send data");
                    byte[] hexData = new byte[]{
                            (byte) 0xfe, (byte) 0x0d, (byte) 0x10, (byte) 0x1b,
                            (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe8};
                    try {
                        SendDataBill(hexData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }


                /**
                 *
                 *
                 * Detail Page
                 *
                 *
                 */
                if(ProductDetailActivity.dialog_cash_input_price != null){
                    ((Activity) this).runOnUiThread(() -> {
                        ProductDetailActivity.dialog_cash_input_price.setText(String.format(priceFormatter.format(MainActivity.getBill_Sum)));
                    });

                }

                if(ProductDetailActivity.cash_init_active && !MainActivity.setBill_test_over_flow_trigger &&  MainActivity.getBill_Sum > ProductDetailActivity.selectedProduct_price){

                    MainActivity.setBill_test_over_flow_trigger = true;

                    int overflowAmount = (MainActivity.getBill_Sum - ProductDetailActivity.selectedProduct_price) / 1000;
//                    Dlog.i("overflowAmount :: " +  overflowAmount);
                    byte[] hexData = new byte[]{
                            (byte) 0xfe, (byte) 0x0d, (byte) 0x12, (byte) 0x00,
                            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xe0};
                    try {
                        for(int i =0; i < overflowAmount ; i++){
                            SendDataBill(hexData);
                            MainActivity.getBill_out_counter++;
                            MainActivity.getBill_output = MainActivity.getBill_out_counter * 1000;
                            // 500ms(0.5초) 딜레이
                            Thread.sleep(3000);
                        }
                        MainActivity.setBill_test_cash_overflow_complete = true;

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
                if(ProductDetailActivity.dialog_cash_input_price != null){
                    ((Activity) this).runOnUiThread(() -> {
                        ProductDetailActivity.dialog_cash_input_price.setText(String.format(priceFormatter.format(MainActivity.getBill_Sum)));
                    });

                }

                if(ProductDetailActivity.cash_init_active && (MainActivity.getBill_Sum == ProductDetailActivity.selectedProduct_price|| MainActivity.setBill_test_cash_overflow_complete)){
                    ProductDetailActivity.cash_init_active = false;
                    MainActivity.setBill_test_over_flow_trigger = false;
                    MainActivity.setBill_test_cash_overflow_complete = false;

                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 이 부분에 UI 업데이트 코드를 넣습니다.
                            if(ProductDetailActivity.dialog_cash_progress_text != null && ProductDetailActivity.dialog_cash_progress_bg != null) {
                                ProductDetailActivity.dialog_cash_progress_bg.setBackground(new ColorDrawable(Color.parseColor("#00AA00")));
                                ProductDetailActivity.dialog_cash_progress_text.setText("결재 완료");
                            }
                        }
                    });

                    MainActivity.getBill_output = 0;
                    MainActivity.getBill_out_counter = 0;

                    // 브로드캐스트 인텐트에 Product 객체 추가
                    Intent intent = new Intent("com.jwlryk.ogkiosk.ACTION_PAYMENT_CASH_RESULT");
                    intent.putExtra("product", ProductDetailActivity.productData);  // productData는 Product 객체
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


                    Dlog.i("button_bill_stop :: " +  "send data");
                    byte[] hexData = new byte[]{
                            (byte) 0xfe, (byte) 0x0d, (byte) 0x10, (byte) 0x18,
                            (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xeb};
                    try {
                        SendDataBill(hexData);
//                        Thread.sleep(1000);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }



    // 응답 메시지를 AlertDialog로 표시하는 메소드
    public static void ShowAlertDialogConfirm(Activity activity, String title, String message) {
        if (!activity.isFinishing()) {  // 액티비티가 종료 중이 아닐 때만 대화상자를 표시
            AlertDialog dialog = new AlertDialog.Builder(activity)  // AlertDialog 생성
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("확인", (dialogInterface, which) -> dialogInterface.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .create();

            dialog.show();  // 다이얼로그 표시

            // 5초 후 자동으로 다이얼로그 닫기
            new Handler().postDelayed(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();  // 다이얼로그 닫기
                }
            }, 3000);
        }
    }




    public static void ShowAlertDialogAndClose(Activity activity, String title, String message) {
        if (!activity.isFinishing()) {  // 액티비티가 종료 중이 아닐 때만 대화상자를 표시
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("확인", (dialog, which) -> {
                        dialog.dismiss();
                        activity.finish();  // 창을 닫음
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }


    /** WSS **/
    public static void wss_Init(Context context){
        Dlog.i("WebSocket :: Initialized");
        String storeCode = sharedPreferences.getString("StoreCode", "");; // 서버 코드 설정
        String serverAddress = "wss://hub.airout.kr:8800/wss/";; // 서버 주소 설정
        String connectionURL = serverAddress + storeCode;

        Dlog.i("WebSocket connectionURL :: " + connectionURL);
        if(storeCode.length() > 0 ){
            try {
                // WebSocket 연결 생성
                mWebSocketClient = new AOWebSocketClient(new URI(connectionURL), context);
                mWebSocketClient.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }


    void wss_sendMessage(){
        wss_message_01 = findViewById(R.id.wss_message_01);
        wss_message_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWebSocketMessage("PROD-TEST");
            }
        });

    }

    public static void sendWebSocketMessage(String message) {
        if (mWebSocketClient != null) {
            try {
                // JSON 객체 생성
                JSONObject sendMessage = new JSONObject();
                sendMessage.put("msg", message);  // "msg" 필드에 메시지 추가

                // JSON 객체를 문자열로 변환하여 WebSocket을 통해 전송
                mWebSocketClient.sendMessageToServer(sendMessage.toString());
            } catch (JSONException e) {
                Log.e("WebSocket", "Error creating JSON message", e);
            }
        } else {
            Log.e("WebSocket", "WebSocket Client is not initialized");
        }
    }


    public void WSSCheckTimer(){
        // Runnable을 생성하여 뒤로가기 이벤트를 예약
        runnable = new Runnable() {
            @Override
            public void run() {
                // 웹소켓 연결 상태 확인
                if (MainActivity.mWebSocketClient != null) {
                    if (MainActivity.mWebSocketClient.isClosed()) {
                        MainActivity.wss_Init(getApplicationContext()); // 연결이 닫혔으면 초기화
                        MainActivity.sendWebSocketMessage("PROD=Product-Reconnect");
                    } else {
                        // 연결이 여전히 열려 있으면 재연결 메시지를 보낼 필요는 없습니다.
                        // 필요에 따라 상태 체크 로직 추가
                        MainActivity.sendWebSocketMessage("PROD=Product-AlreadyConnect");
                    }
                } else {
                    // WebSocket 클라이언트가 null인 경우 초기화
                    MainActivity.wss_Init(getApplicationContext());
                    MainActivity.sendWebSocketMessage("PROD=Product-Reconnect");
                }

                // 다음 상태 확인을 위해 타이머 재설정
                handler.postDelayed(this, 60 * 1000); // 60초 후 다시 실행
            }
        };

        // 10초 타이머 시작
        WSSTimerStart();
    }

    private void WSSTimerStart() {
        // 10초 후에 뒤로가기 이벤트 발생시키기
        wssHandler.postDelayed(runnable, 60*1000); // 3*60초후에 실행
    }

    public void WSSTimerReset() {
        Dlog.i("resetTimer");
        // 이전에 예약된 콜백 제거하고 타이머 초기화
        wssHandler.removeCallbacksAndMessages(null);
        WSSTimerStart(); // 새로운 타이머 시작
    }






}
