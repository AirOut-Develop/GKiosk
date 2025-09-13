package com.jwlryk.gkiosk.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jwlryk.gkiosk.data.KioskInfo;
import com.jwlryk.gkiosk.data.ProductItemList;
import com.jwlryk.gkiosk.model.ProductItem;
import com.jwlryk.gkiosk.remote.api.ApiClient;
import com.jwlryk.gkiosk.remote.api.ApiResponse;
import com.jwlryk.gkiosk.remote.api.Device;
import com.jwlryk.gkiosk.remote.api.Product;
import com.jwlryk.gkiosk.remote.api.Store;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chains API calls: companyNumber → Store → Devices → Products
 * and assigns results into singletons (KioskInfo, ProductItemList).
 */
public final class BootstrapChain {

    public interface Listener {
        void onProgress(@NonNull String step);
        void onSuccess();
        void onError(@NonNull String step, @NonNull String message, @Nullable Throwable t);
    }

    public void runByCompanyNumber(@NonNull String companyNumber, @NonNull Listener listener) {
        if (companyNumber.isEmpty()) {
            listener.onError("validate", "사업자번호가 비어있음", null);
            return;
        }
        listener.onProgress("/store/company/{companyNumber}");
        ApiClient.get().getStoreByCompanyNumber(companyNumber).enqueue(new Callback<ApiResponse<Store>>() {
            @Override public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> respStore) {
                if (!respStore.isSuccessful() || respStore.body() == null || respStore.body().getPayload() == null) {
                    listener.onError("store(company)", httpMsg(respStore), null);
                    return;
                }
                Store s = respStore.body().getPayload();
                assignStore(s);
                String storeCode = s.code;
                if (storeCode == null || storeCode.isEmpty()) {
                    listener.onError("store(company)", "Store code 없음", null);
                    return;
                }
                fetchDevicesThenProducts(storeCode, listener);
            }
            @Override public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                listener.onError("store(company)", errMsg(t), t);
            }
        });
    }

    private void fetchDevicesThenProducts(@NonNull String storeCode, @NonNull Listener listener) {
        listener.onProgress("/store/" + storeCode + "/devices");
        ApiClient.get().getDevicesByStoreCode(storeCode).enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> respDev) {
                if (!respDev.isSuccessful() || respDev.body() == null || respDev.body().getPayload() == null) {
                    listener.onError("devices", httpMsg(respDev), null);
                    return;
                }
                String deviceCode = pickFirstDeviceCode(respDev.body().getPayload());
                if (deviceCode == null) {
                    listener.onError("devices", "사용 가능한 device code 없음", null);
                    return;
                }
                KioskInfo.getInstance().setKioskCode(deviceCode);
                fetchProducts(deviceCode, listener);
            }
            @Override public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                listener.onError("devices", errMsg(t), t);
            }
        });
    }

    private void fetchProducts(@NonNull String deviceCode, @NonNull Listener listener) {
        listener.onProgress("/device/" + deviceCode + "/products");
        ApiClient.get().getProductsByDeviceCode(deviceCode).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> respProd) {
                if (!respProd.isSuccessful() || respProd.body() == null || respProd.body().getPayload() == null) {
                    listener.onError("products", httpMsg(respProd), null);
                    return;
                }
                assignProducts(respProd.body().getPayload());
                listener.onSuccess();
            }
            @Override public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                listener.onError("products", errMsg(t), t);
            }
        });
    }

    private static void assignStore(@NonNull Store s) {
        KioskInfo.getInstance()
                .setStoreCode(s.code)
                .setStoreName(s.name)
                .setStoreCompanyNumber(s.companyNumber)
                .setDeviceCardCompanyType(s.cardVanType)
                .setDeviceCardTID(s.cardVanCATID);
    }

    private static void assignProducts(@NonNull List<Product> list) {
        List<ProductItem> local = new ArrayList<>();
        for (Product rp : list) {
            if (rp == null) continue;
            ProductItem lp = new ProductItem();
            lp.setId(rp.id);
            lp.setProductCode(rp.productCode);
            lp.setTitle(rp.name);
            lp.setBrand(rp.brand);
            lp.setCategory(rp.category);
            // Map images and price like KioskRemoteOps for consistency
            lp.setImage(rp.imageMain);
            lp.setDetailImage(rp.imageDetail);
            lp.setPrice(rp.price);
            lp.setVendingSlotNumber(rp.positionIndex);
            lp.setSortOrder(rp.displayIndex);
            lp.setActive(rp.status == 1);
            // Normalize categories and tags (GKVEN codes, comma-separated)
            com.jwlryk.gkiosk.remote.KioskRemoteOps.applyCategoryAndTags(rp, lp);
            local.add(lp);
        }
        ProductItemList.getInstance().setAll(local);
    }

    @Nullable
    private static String pickFirstDeviceCode(@Nullable List<Device> devs) {
        if (devs == null) return null;
        for (Device d : devs) {
            if (d != null && d.code != null && !d.code.isEmpty()) return d.code;
        }
        return null;
    }

    private static String httpMsg(Response<?> r) {
        return "HTTP " + (r == null ? "?" : r.code());
    }

    private static String errMsg(Throwable t) {
        return t == null ? "unknown" : t.getMessage();
    }
}
