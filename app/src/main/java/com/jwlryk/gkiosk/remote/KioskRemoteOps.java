package com.jwlryk.gkiosk.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jwlryk.gkiosk.data.KioskInfo;
import com.jwlryk.gkiosk.data.KioskPrefs;
import com.jwlryk.gkiosk.data.ProductItemList;
import com.jwlryk.gkiosk.model.ProductItem;
import com.jwlryk.gkiosk.remote.api.ApiClient;
import com.jwlryk.gkiosk.remote.api.ApiResponse;
import com.jwlryk.gkiosk.remote.api.Device;
import com.jwlryk.gkiosk.remote.api.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Centralized, reusable remote operations for kiosk bootstrap and product refresh.
 * Extracted from API Debug/Init paths so any screen can invoke consistently.
 */
public final class KioskRemoteOps {

    private KioskRemoteOps() {}

    public interface Listener {
        default void onProgress(@NonNull String step) {}
        void onSuccess();
        void onError(@NonNull String message, @Nullable Throwable t);
    }

    /** Run company → store → devices → products chain and assign to singletons. */
    public static void chainByCompanyNumber(@NonNull String companyNumber, @NonNull Listener listener) {
        new BootstrapChain().runByCompanyNumber(companyNumber, new BootstrapChain.Listener() {
            @Override public void onProgress(@NonNull String step) { listener.onProgress(step); }
            @Override public void onSuccess() {
                // Persist latest state
                listener.onProgress("persist");
                listener.onSuccess();
            }
            @Override public void onError(@NonNull String step, @NonNull String message, @Nullable Throwable t) {
                listener.onError(step + ": " + message, t);
            }
        });
    }

    /** Fetch products by device code and assign to ProductItemList. */
    public static void refreshProductsByDevice(@NonNull String deviceCode, @NonNull Listener listener) {
        listener.onProgress("/device/" + deviceCode + "/products");
        ApiClient.get().getProductsByDeviceCode(deviceCode).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getPayload() == null) {
                    listener.onError("HTTP " + (response == null ? "?" : response.code()), null);
                    return;
                }
                assignProducts(response.body().getPayload());
                listener.onSuccess();
            }
            @Override public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                listener.onError(t == null ? "unknown" : t.getMessage(), t);
            }
        });
    }

    /** Fetch products by device code filtered by category code and assign. */
    public static void refreshProductsByDeviceAndCategory(@NonNull String deviceCode, @NonNull String categoryCode, @NonNull Listener listener) {
        listener.onProgress("/device/" + deviceCode + "/products?category=" + categoryCode);
        ApiClient.get().getProductsByDeviceCodeAndCategory(deviceCode, categoryCode).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getPayload() == null) {
                    listener.onError("HTTP " + (response == null ? "?" : response.code()), null);
                    return;
                }
                assignProducts(response.body().getPayload());
                listener.onSuccess();
            }
            @Override public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                listener.onError(t == null ? "unknown" : t.getMessage(), t);
            }
        });
    }

    /** Resolve best path and ensure ProductItemList is populated. */
    public static void resolveAndRefreshProducts(@NonNull KioskInfo ki, @NonNull Listener listener) {
        // If baseUrl is empty, ApiClient will fall back to its internal default.
        // 1) If kioskCode known, fetch products directly
        String device = ki.getKioskCode();
        if (device != null && !device.isEmpty()) {
            refreshProductsByDevice(device, listener);
            return;
        }
        // 2) If company known, run chain
        String company = ki.getStoreCompanyNumber();
        if (company != null && !company.isEmpty()) {
            chainByCompanyNumber(company, listener);
            return;
        }
        // 3) If store known, resolve a device then fetch products
        String store = ki.getStoreCode();
        if (store != null && !store.isEmpty()) {
            listener.onProgress("/store/" + store + "/devices");
            ApiClient.get().getDevicesByStoreCode(store).enqueue(new Callback<ApiResponse<List<Device>>>() {
                @Override public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> respDev) {
                    if (!respDev.isSuccessful() || respDev.body() == null || respDev.body().getPayload() == null || respDev.body().getPayload().isEmpty()) {
                        listener.onError("devices: HTTP " + (respDev == null ? "?" : respDev.code()), null);
                        return;
                    }
                    String dev = respDev.body().getPayload().get(0).code;
                    KioskInfo.getInstance().setKioskCode(dev);
                    refreshProductsByDevice(dev, listener);
                }
                @Override public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                    listener.onError(t == null ? "unknown" : t.getMessage(), t);
                }
            });
            return;
        }
        listener.onError("no keys to resolve products", null);
    }

    public static void assignProducts(@NonNull List<Product> list) {
        List<ProductItem> local = new ArrayList<>();
        for (Product rp : list) {
            if (rp == null) continue;
            ProductItem lp = new ProductItem();
            lp.setId(rp.id);
            lp.setProductCode(rp.productCode);
            lp.setTitle(rp.name);
            lp.setBrand(rp.brand);
            lp.setCategory(rp.category);
            // Map images for UI binding
            lp.setImage(rp.imageMain);
            lp.setDetailImage(rp.imageDetail);
            lp.setPrice(rp.price);
            lp.setVendingSlotNumber(rp.positionIndex);
            lp.setSortOrder(rp.displayIndex);
            lp.setActive(rp.status == 1);
            // Normalize categories and tags from GKVEN codes and comma-separated values
            applyCategoryAndTags(rp, lp);
            local.add(lp);
        }
        ProductItemList.getInstance().setAll(local);
        KioskPrefs.saveAll(null, KioskInfo.getInstance()); // safe even if ctx null in our overload (we will guard)
    }

    private static void addTag(List<String> tags, String v) {
        if (v == null) return;
        String t = v.trim();
        if (!t.isEmpty() && !tags.contains(t)) tags.add(t);
    }

    /**
     * Build category/tags using ONLY remote.api.Product.category.
     * Expected formats:
     *  - "GKVEN-CTGRY-XXX,라벨" (key,name)
     *  - "GKVEN-CTGRY-XXX" (key only)
     *  - Any other string → used as-is for category; label becomes same.
     */
    public static void applyCategoryAndTags(@NonNull Product rp, @NonNull ProductItem lp) {
        // Store category EXACTLY as provided by API. Do not split or transform.
        lp.setCategory(rp.category == null ? "" : rp.category.trim());
        // Do not derive tags from category string.
        lp.setTags(new java.util.ArrayList<>());
    }
}
