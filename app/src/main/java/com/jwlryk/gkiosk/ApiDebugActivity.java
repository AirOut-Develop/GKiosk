package com.jwlryk.gkiosk;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        tvApiJson = findViewById(R.id.tv_api_json);
        tvAssignStatus = findViewById(R.id.tv_assign_status);
        etBaseUrl = findViewById(R.id.et_base_url);
        etStoreCode = findViewById(R.id.et_store_code);
        etDeviceCode = findViewById(R.id.et_device_code);
        tvApiJson.setMovementMethod(new ScrollingMovementMethod());

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
        ApiService api = ApiClient.get();
        tvApiJson.setText("Loading /store/" + code + " ...");
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
        ApiService api = ApiClient.get();
        tvApiJson.setText("Loading /store/" + code + "/devices ...");
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
        tvApiJson.setText("Loading /store/company/" + companyNo + " ...");
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
        if (companyNo.isEmpty()) {
            tvAssignStatus.setText("Chain: FAILED - 사업자번호가 비어있음");
            return;
        }
        ApiService api = ApiClient.get();
        tvAssignStatus.setText("Chain: /store/company → /store/{code}/devices → /device/{DeviceCode}/products");
        tvApiJson.setText("Loading /store/company/" + companyNo + " ...");
        api.getStoreByCompanyNumber(companyNo).enqueue(new Callback<ApiResponse<Store>>() {
            @Override public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> respStore) {
                handleApiResult("Store(company)", respStore);
                if (!respStore.isSuccessful() || respStore.body() == null || respStore.body().getPayload() == null) {
                    tvAssignStatus.setText("Chain: FAILED at store(company)");
                    return;
                }
                Store s = respStore.body().getPayload();
                assignStoreToSingleton(s);
                if (s.code != null) etStoreCode.setText(s.code);

                // step 2: devices
                tvApiJson.setText("Loading /store/" + s.code + "/devices ...");
                ApiClient.get().getDevicesByStoreCode(s.code).enqueue(new Callback<ApiResponse<List<Device>>>() {
                    @Override public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> respDev) {
                        handleApiResult("Devices", respDev);
                        if (!respDev.isSuccessful() || respDev.body() == null || respDev.body().getPayload() == null) {
                            tvAssignStatus.setText("Chain: FAILED at devices");
                            return;
                        }
                        List<Device> devs = respDev.body().getPayload();
                        // pick first device code
                        String deviceCode = null;
                        if (devs != null) {
                            for (Device d : devs) { if (d != null && d.code != null && !d.code.isEmpty()) { deviceCode = d.code; break; } }
                        }
                        if (deviceCode == null) {
                            tvAssignStatus.setText("Chain: FAILED - device code 없음");
                            return;
                        }
                        etDeviceCode.setText(deviceCode);
                        KioskInfo.getInstance().setKioskCode(deviceCode);

                        // step 3: products
                        tvApiJson.setText("Loading /device/" + deviceCode + "/products ...");
                        ApiClient.get().getProductsByDeviceCode(deviceCode).enqueue(new Callback<ApiResponse<List<Product>>>() {
                            @Override public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> respProd) {
                                handleApiResult("Products", respProd);
                                if (!respProd.isSuccessful() || respProd.body() == null || respProd.body().getPayload() == null) {
                                    tvAssignStatus.setText("Chain: FAILED at products");
                                    return;
                                }
                                assignProductsToSingleton(respProd.body().getPayload());
                                tvAssignStatus.setText("Chain: OK - 모든 단계 완료");
                            }
                            @Override public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) { setError("Products(chain)", t); }
                        });
                    }
                    @Override public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) { setError("Devices(chain)", t); }
                });
            }
            @Override public void onFailure(Call<ApiResponse<Store>> call, Throwable t) { setError("Store(company)", t); }
        });
    }

    private void callProducts() {
        applyConfigToSingleton();
        String deviceCode = etDeviceCode.getText().toString().trim();
        ApiService api = ApiClient.get();
        tvApiJson.setText("Loading /device/" + deviceCode + "/products ...");
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
        String json;
        try {
            Object body = response.body();
            if (body != null) json = gson.toJson(body);
            else json = "HTTP " + response.code() + " (empty body)";
        } catch (Exception e) {
            json = "parse error: " + e.getMessage();
        }
        tvApiJson.setText(json);
        tvAssignStatus.setText(tag + ": OK");
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
            tvAssignStatus.setText("Store assigned to KioskInfo");
        } catch (Exception e) {
            tvAssignStatus.setText("Store assign FAILED: " + e.getMessage());
        }
        renderSingletonState();
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

    private void applyConfigToSingleton() {
        String baseUrl = etBaseUrl.getText().toString().trim();
        String storeCode = etStoreCode.getText().toString().trim();
        String deviceCode = etDeviceCode.getText().toString().trim();
        KioskInfo.getInstance()
                .setApiBaseUrl(baseUrl.isEmpty() ? null : baseUrl)
                .setStoreCode(storeCode)
                .setKioskCode(deviceCode);
        renderSingletonState();
    }

    private static String n(String s) { return s == null ? "" : s; }
}
