package com.jwlryk.gkiosk.remote.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/store/{code}")
    Call<ApiResponse<Store>> getStoreByCode(@Path("code") String storeCode);

    @GET("/store/company/{companyNumber}")
    Call<ApiResponse<Store>> getStoreByCompanyNumber(@Path("companyNumber") String companyNumber);

    @GET("/store/{storeCode}/devices")
    Call<ApiResponse<java.util.List<Device>>> getDevicesByStoreCode(@Path("storeCode") String storeCode);

    @GET("/device/{DeviceCode}/products")
    Call<ApiResponse<java.util.List<Product>>> getProductsByDeviceCode(@Path("DeviceCode") String deviceCode);

    @POST("/sales")
    Call<ApiResponse<Void>> createSale(@Body Sale sale);
}

