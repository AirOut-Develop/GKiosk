package com.jwlryk.ogkiosk.API;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    /// Store 데이터를 받아오는 GET 메서드
    @GET("/store/{code}")
    Call<ApiResponse<Store>> getStoreByCode(@Path("code") String storeCode);

    @GET("/store/company/{companyNumber}")
    Call<ApiResponse<Store>> getStoreByCompanyNumber(@Path("companyNumber") String companyNumber);

    // StoreCode로 Device 데이터 조회
    @GET("/store/{storeCode}/devices")
    Call<ApiResponse<List<Device>>> getDevicesByStoreCode(@Path("storeCode") String storeCode);

    // Device 데이터를 받아오는 GET 메서드
    @GET("/device/{DeviceCode}/products")
    Call<ApiResponse<List<Product>>> getProductsByDeviceCode(@Path("DeviceCode") String deviceCode);

    // 판매 기록을 서버로 전송하는 POST 메서드
    @POST("/sales")
    Call<ApiResponse<Void>> createSale(@Body Sale sale);

//    // Product 데이터를 받아오는 GET 메서드 (예시)
//    @GET("/product/{code}")
//    Call<ApiResponse<Product>> getProductByCode(@Path("code") String productCode);


}
