package com.jwlryk.ogkiosk.API;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jwlryk.ogkiosk.API.ApiResponse;
import com.jwlryk.ogkiosk.API.ApiService;
import com.jwlryk.ogkiosk.API.Store;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHelper {

    public static final String BASE_URL = "https://hub.airout.kr:8800";
    private ApiService apiService;

    // Retrofit 초기화
    public ApiHelper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build()) // 필요한 경우 OkHttp 클라이언트 설정
                .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱에 사용
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // --------------------- Store 관련 API 호출 ---------------------

    // StoreCode로 Store 데이터 조회
    public void fetchStoreByCode(String storeCode, final ApiCallback<Store> callback) {
        Call<ApiResponse<Store>> call = apiService.getStoreByCode(storeCode);
        executeApiCall(call, "Store", callback);
    }

    // CompanyNumber로 Store 데이터 조회
    public void fetchStoreByCompanyNumber(String companyNumber, final ApiCallback<Store> callback) {
        Call<ApiResponse<Store>> call = apiService.getStoreByCompanyNumber(companyNumber);
        executeApiCall(call, "Store", callback);
    }

    // StoreCode로 Device 데이터 조회
    public void fetchDevicesByStoreCode(String storeCode, ApiCallback<List<Device>> callback) {
        Call<ApiResponse<List<Device>>> call = apiService.getDevicesByStoreCode(storeCode);
        executeApiCall(call, "Device", callback);
    }

    // --------------------- Device 관련 API 호출 ---------------------

    // DeviceCode로 제품 목록 조회
    public void fetchProductsByDeviceCode(String deviceCode, ApiCallback<List<Product>> callback) {
        // API 엔드포인트 호출
        Call<ApiResponse<List<Product>>> call = apiService.getProductsByDeviceCode(deviceCode);
        executeApiCall(call, "Product", callback);
    }


    // --------------------- Product 관련 API 호출 ---------------------

    // ProductCode로 Product 데이터 조회 (예시)
//    public void fetchProductByCode(String productCode) {
//        Call<ApiResponse<Product>> call = apiService.getProductByCode(productCode);
//        executeApiCall(call, "Product");
//    }



    // --------------------- Sales 관련 API 호출 ---------------------

    // 판매 기록을 전송하는 메서드
    public void createSale(Sale sale, final ApiCallback<Void> callback) {
        Call<ApiResponse<Void>> call = apiService.createSale(sale);
        executeApiCall(call, "Sale", callback);
    }




    // 공통 API 호출 메서드 (Store, Device, Product 등 모든 도메인에서 사용 가능)
    private <T> void executeApiCall(Call<ApiResponse<T>> call, String entityType, final ApiCallback<T> callback) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getPayload() != null) {
                    // 응답에서 payload를 가져옴
                    T data = response.body().getPayload();
                    callback.onSuccess(data);

                    // JSON 데이터가 필요하다면 여기서 변환 가능
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String jsonResponse = gson.toJson(data);
                    Log.d(entityType + " Data (JSON)", jsonResponse);

                } else {
                    Log.e("Error", "Error: " + response.code());
                    callback.onFailure("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                Log.e("Error", "Failed to fetch " + entityType + " data: " + t);
                callback.onFailure("Failed to fetch data: " + t);
            }
        });
    }


    // 콜백 인터페이스
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }
}
