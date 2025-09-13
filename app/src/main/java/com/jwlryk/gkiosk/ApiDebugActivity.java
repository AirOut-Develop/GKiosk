package com.jwlryk.gkiosk;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jwlryk.gkiosk.data.KioskInfo;
import com.jwlryk.gkiosk.data.ProductCartList;
import com.jwlryk.gkiosk.data.ProductItemList;
import com.jwlryk.gkiosk.model.ProductItem;
import com.jwlryk.gkiosk.remote.api.ApiClient;
import com.jwlryk.gkiosk.remote.api.ApiResponse;
import com.jwlryk.gkiosk.remote.api.ApiService;
import com.jwlryk.gkiosk.remote.api.Device;
import com.jwlryk.gkiosk.remote.api.Product;
import com.jwlryk.gkiosk.remote.api.Store;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiDebugActivity extends AppCompatActivity {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private TextView tvSingletonState;
    private TextView tvRequestJson;
    private TextView tvApiJson;
    private TextView tvAssignStatus;
    private EditText etBaseUrl;
    private EditText etStoreCode;
    private EditText etDeviceCode;
    private EditText etCompanyNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_debug);

        tvSingletonState = findViewById(R.id.tv_singleton_state);
        tvRequestJson = findViewById(R.id.tv_request_json);
        tvApiJson = findViewById(R.id.tv_api_json);
        tvAssignStatus = findViewById(R.id.tv_assign_status);
        etBaseUrl = findViewById(R.id.et_base_url);
        etStoreCode = findViewById(R.id.et_store_code);
        etDeviceCode = findViewById(R.id.et_device_code);
        tvRequestJson.setMovementMethod(new ScrollingMovementMethod());
        tvApiJson.setMovementMethod(new ScrollingMovementMethod());

        // Load from SharedPreferences, then prefill inputs from KioskInfo
        com.jwlryk.gkiosk.data.KioskPrefs.loadIntoSingleton(this);
        // Prefill from KioskInfo
        KioskInfo ki = KioskInfo.getInstance();
        if (ki.getApiBaseUrl() != null) etBaseUrl.setText(ki.getApiBaseUrl());
        if (ki.getStoreCode() != null) etStoreCode.setText(ki.getStoreCode());
        if (ki.getKioskCode() != null) etDeviceCode.setText(ki.getKioskCode());

        Button btnRefreshSingleton = findViewById(R.id.btn_refresh_singleton);
        Button btnClearCart = findViewById(R.id.btn_clear_cart_singleton);
        Button btnCallStore = findViewById(R.id.btn_call_store);
        Button btnCallDevices = findViewById(R.id.btn_call_devices);
        Button btnCallProducts = findViewById(R.id.btn_call_products);
        Button btnApplyConfig = findViewById(R.id.btn_apply_config);
        Button btnCallStoreByCompany = findViewById(R.id.btn_call_store_by_company);
        Button btnChain = findViewById(R.id.btn_chain_company_devices_products);
        etCompanyNumber = findViewById(R.id.et_company_number);

        btnRefreshSingleton.setOnClickListener(v -> renderSingletonState());
        btnClearCart.setOnClickListener(v -> { ProductCartList.getInstance().clear(); renderSingletonState(); });
        btnApplyConfig.setOnClickListener(v -> applyConfigToSingleton());
        btnCallStore.setOnClickListener(v -> callStore());
        btnCallStoreByCompany.setOnClickListener(v -> callStoreByCompany());
        btnCallDevices.setOnClickListener(v -> callDevices());
        btnCallProducts.setOnClickListener(v -> callProducts());
        btnChain.setOnClickListener(v -> runChainFromCompany());

        renderSingletonState();
    }

    private void renderSingletonState() {
        KioskInfo k = KioskInfo.getInstance();
        ProductItemList catalog = ProductItemList.getInstance();
        ProductCartList cart = ProductCartList.getInstance();

        StringBuilder sb = new StringBuilder();
        sb.append("KioskInfo\n")
          .append("  apiBaseUrl: ").append(n(k.getApiBaseUrl())).append('\n')
          .append("  wsEndpoint: ").append(n(k.getWsEndpoint())).append('\n')
          .append("  storeCode: ").append(n(k.getStoreCode())).append(" / name: ").append(n(k.getStoreName())).append('\n')
          .append("  kioskCode: ").append(n(k.getKioskCode())).append(" / name: ").append(n(k.getKioskName())).append('\n')
          .append("  flags: maintenance=").append(k.isMaintenanceMode())
          .append(", refund=").append(k.isRefundEnable())
          .append(", partialCancel=").append(k.isPartialCancelEnable())
          .append(", offline=").append(k.isOfflineModeEnable()).append("\n\n");

        sb.append("ProductItemList\n")
          .append("  size: ").append(catalog.size()).append('\n');

        sb.append("ProductCartList\n")
          .append("  distinct: ").append(cart.getDistinctCount())
          .append(", units: ").append(cart.getTotalUnits())
          .append(", subtotal: ").append(cart.getSubtotal()).append('\n');

        tvSingletonState.setText(sb.toString());
    }

    private void callStore() {
        applyConfigToSingleton();
        String code = etStoreCode.getText().toString().trim();
        if (code.isEmpty()) {
            String fromSingleton = KioskInfo.getInstance().getStoreCode();
            if (fromSingleton != null && !fromSingleton.isEmpty()) {
                code = fromSingleton;
                etStoreCode.setText(code);
            }
        }
        if (code.isEmpty()) {
            promptFetchByCompanyThen(this::callStore);
            return;
        }
        ApiService api = ApiClient.get();
        showRequest("GET", "/store/" + code, null);
        api.getStoreByCode(code).enqueue(new Callback<ApiResponse<Store>>() {
            @Override public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                handleApiResult("Store", response);
                if (response.isSuccessful() && response.body() != null && response.body().getPayload() != null) {
                    Store s = response.body().getPayload();
                    assignStoreToSingleton(s);
                }
            }
            @Override public void onFailure(Call<ApiResponse<Store>> call, Throwable t) { setError("Store", t); }
        });
    }

    private void callDevices() {
        applyConfigToSingleton();
        String code = etStoreCode.getText().toString().trim();
        if (code.isEmpty()) {
            String fromSingleton = KioskInfo.getInstance().getStoreCode();
            if (fromSingleton != null && !fromSingleton.isEmpty()) {
                code = fromSingleton;
                etStoreCode.setText(code);
            }
        }
        if (code.isEmpty()) {
            promptFetchByCompanyThen(this::callDevices);
            return;
        }
        ApiService api = ApiClient.get();
        showRequest("GET", "/store/" + code + "/devices", null);
        api.getDevicesByStoreCode(code).enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                handleApiResult("Devices", response);
            }
            @Override public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) { setError("Devices", t); }
        });
    }

    private void callStoreByCompany() {
        applyConfigToSingleton();
        String companyNo = etCompanyNumber.getText().toString().trim();
        if (companyNo.isEmpty()) {
            tvAssignStatus.setText("Store(company): FAILED - 사업자번호가 비어있음");
            return;
        }
        ApiService api = ApiClient.get();
        showRequest("GET", "/store/company/" + companyNo, null);
        api.getStoreByCompanyNumber(companyNo).enqueue(new Callback<ApiResponse<Store>>() {
            @Override public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                handleApiResult("Store(company)", response);
                if (response.isSuccessful() && response.body() != null && response.body().getPayload() != null) {
                    Store s = response.body().getPayload();
                    assignStoreToSingleton(s);
                    // StoreCode를 입력창에도 반영
                    if (s.code != null) etStoreCode.setText(s.code);
                }
            }
            @Override public void onFailure(Call<ApiResponse<Store>> call, Throwable t) { setError("Store(company)", t); }
        });
    }

    private void runChainFromCompany() {
        applyConfigToSingleton();
        String companyNo = etCompanyNumber.getText().toString().trim();
        tvAssignStatus.setText("Chain: /store/company → /store/{code}/devices → /device/{DeviceCode}/products");
        showRequest("GET", "/store/company/" + companyNo, null);
        new com.jwlryk.gkiosk.remote.BootstrapChain().runByCompanyNumber(companyNo, new com.jwlryk.gkiosk.remote.BootstrapChain.Listener() {
            @Override public void onProgress(@NonNull String step) {
                tvAssignStatus.setText("Chain: 진행 중 - " + step);
            }
            @Override public void onSuccess() {
                tvAssignStatus.setText("Chain: OK - 모든 단계 완료");
                if (KioskInfo.getInstance().getStoreCode() != null) etStoreCode.setText(KioskInfo.getInstance().getStoreCode());
                if (KioskInfo.getInstance().getKioskCode() != null) etDeviceCode.setText(KioskInfo.getInstance().getKioskCode());
                com.jwlryk.gkiosk.data.KioskPrefs.saveAll(ApiDebugActivity.this, KioskInfo.getInstance());
                renderSingletonState();
            }
            @Override public void onError(@NonNull String step, @NonNull String message, @Nullable Throwable t) {
                tvAssignStatus.setText("Chain: FAILED at " + step + " - " + message);
            }
        });
    }

    private void callProducts() {
        applyConfigToSingleton();
        String deviceCode = etDeviceCode.getText().toString().trim();
        if (deviceCode.isEmpty()) {
            String fromSingleton = KioskInfo.getInstance().getKioskCode();
            if (fromSingleton != null && !fromSingleton.isEmpty()) {
                deviceCode = fromSingleton;
                etDeviceCode.setText(deviceCode);
            }
        }
        if (deviceCode.isEmpty()) {
            promptFetchByCompanyThen(this::callProducts);
            return;
        }
        ApiService api = ApiClient.get();
        showRequest("GET", "/device/" + deviceCode + "/products", null);
        api.getProductsByDeviceCode(deviceCode).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                handleApiResult("Products", response);
                if (response.isSuccessful() && response.body() != null && response.body().getPayload() != null) {
                    assignProductsToSingleton(response.body().getPayload());
                }
            }
            @Override public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) { setError("Products", t); }
        });
    }

    private void handleApiResult(String tag, Response<?> response) {
        String out;
        if (response.isSuccessful()) {
            try {
                Object body = response.body();
                out = (body != null) ? gson.toJson(body) : ("HTTP " + response.code() + " (empty body)");
                tvAssignStatus.setText(tag + ": OK");
            } catch (Exception e) {
                out = "parse error: " + e.getMessage();
                tvAssignStatus.setText(tag + ": PARSE ERROR");
            }
        } else {
            try {
                String err = response.errorBody() != null ? response.errorBody().string() : "";
                out = "HTTP " + response.code() + "\n" + err;
            } catch (Exception e) {
                out = "HTTP " + response.code() + " (error body read failed: " + e.getMessage() + ")";
            }
            tvAssignStatus.setText(tag + ": FAILED HTTP " + response.code());
        }
        tvApiJson.setText(out);
        renderSingletonState();
    }

    private void setError(String tag, Throwable t) {
        tvApiJson.setText("ERROR: " + (t == null ? "unknown" : t.getMessage()));
        tvAssignStatus.setText(tag + ": FAILED - " + (t == null ? "unknown" : t.getMessage()));
    }

    private void assignStoreToSingleton(Store s) {
        try {
            KioskInfo.getInstance()
                    .setStoreCode(s.code)
                    .setStoreName(s.name)
                    .setStoreCompanyNumber(s.companyNumber)
                    .setDeviceCardCompanyType(s.cardVanType)
                    .setDeviceCardTID(s.cardVanCATID);
            if (s.code != null) etStoreCode.setText(s.code);
            if (s.companyNumber != null && etCompanyNumber != null) etCompanyNumber.setText(s.companyNumber);
            com.jwlryk.gkiosk.data.KioskPrefs.saveAll(this, KioskInfo.getInstance());
            tvAssignStatus.setText("Store assigned to KioskInfo");
        } catch (Exception e) {
            tvAssignStatus.setText("Store assign FAILED: " + e.getMessage());
        }
        renderSingletonState();
    }

    private void promptFetchByCompanyThen(@NonNull Runnable afterSuccess) {
        String companyNo = etCompanyNumber != null ? etCompanyNumber.getText().toString().trim() : "";
        if (companyNo.isEmpty()) {
            tvAssignStatus.setText("체인 실행 불가: 사업자번호가 비어있음");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("설정 필요")
                .setMessage("사업자번호(" + companyNo + ")로 매장/디바이스/상품 정보를 불러오시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("불러오기", (d, w) -> {
                    tvAssignStatus.setText("Chain: /store/company → /store/{code}/devices → /device/{DeviceCode}/products");
                    showRequest("GET", "/store/company/" + companyNo, null);
                    new com.jwlryk.gkiosk.remote.BootstrapChain().runByCompanyNumber(companyNo, new com.jwlryk.gkiosk.remote.BootstrapChain.Listener() {
                        @Override public void onProgress(@NonNull String step) { tvAssignStatus.setText("Chain: 진행 중 - " + step); }
                        @Override public void onSuccess() {
                            if (KioskInfo.getInstance().getStoreCode() != null) etStoreCode.setText(KioskInfo.getInstance().getStoreCode());
                            if (KioskInfo.getInstance().getKioskCode() != null) etDeviceCode.setText(KioskInfo.getInstance().getKioskCode());
                            com.jwlryk.gkiosk.data.KioskPrefs.saveAll(ApiDebugActivity.this, KioskInfo.getInstance());
                            renderSingletonState();
                            afterSuccess.run();
                        }
                        @Override public void onError(@NonNull String step, @NonNull String message, @Nullable Throwable t) {
                            tvAssignStatus.setText("Chain: FAILED at " + step + " - " + message);
                        }
                    });
                })
                .show();
    }

    private void assignProductsToSingleton(List<Product> list) {
        try {
            List<ProductItem> local = new ArrayList<>();
            if (list != null) {
                for (Product rp : list) {
                    ProductItem lp = new ProductItem();
                    lp.setId(rp.id);
                    lp.setProductCode(rp.productCode);
                    lp.setTitle(rp.name);
                    lp.setBrand(rp.brand);
                    lp.setCategory(rp.category);
                    lp.setPrice(rp.price);
                    lp.setVendingSlotNumber(rp.positionIndex);
                    lp.setSortOrder(rp.displayIndex);
                    lp.setActive(rp.status == 1);
                    local.add(lp);
                }
            }
            ProductItemList.getInstance().setAll(local);
            tvAssignStatus.setText("Products assigned to ProductItemList (" + local.size() + ")");
        } catch (Exception e) {
            tvAssignStatus.setText("Products assign FAILED: " + e.getMessage());
        }
        renderSingletonState();
    }

    private void showRequest(String method, String path, @Nullable String bodyJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method: ").append(method).append('\n')
          .append("BaseUrl: ").append(n(KioskInfo.getInstance().getApiBaseUrl())).append('\n')
          .append("Path: ").append(path).append('\n')
          .append("StoreCode: ").append(n(KioskInfo.getInstance().getStoreCode())).append('\n')
          .append("DeviceCode: ").append(n(KioskInfo.getInstance().getKioskCode())).append('\n');
        if (bodyJson != null && !bodyJson.isEmpty()) {
            sb.append("Body:\n").append(bodyJson).append('\n');
        }
        if (tvRequestJson != null) tvRequestJson.setText(sb.toString());
        if (tvApiJson != null) tvApiJson.setText("");
    }

    private void applyConfigToSingleton() {
        String baseUrl = etBaseUrl.getText().toString().trim();
        String storeCode = etStoreCode.getText().toString().trim();
        String deviceCode = etDeviceCode.getText().toString().trim();
        KioskInfo.getInstance()
                .setApiBaseUrl(baseUrl.isEmpty() ? null : baseUrl)
                .setStoreCode(storeCode)
                .setKioskCode(deviceCode);
        com.jwlryk.gkiosk.data.KioskPrefs.saveBasics(this, baseUrl.isEmpty() ? null : baseUrl, storeCode, deviceCode);
        renderSingletonState();
    }

    private static String n(String s) { return s == null ? "" : s; }
}
