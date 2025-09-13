package com.jwlryk.gkiosk.remote.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jwlryk.gkiosk.data.KioskInfo;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile ApiService service;

    private ApiClient() {}

    public static ApiService get() {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    String baseUrl = KioskInfo.getInstance().getApiBaseUrl();
                    if (baseUrl == null || baseUrl.isEmpty()) {
                        baseUrl = "https://hub.airout.kr:8800"; // reference default
                    }
                    if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/"; // Retrofit requires trailing slash for baseUrl

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build();
                    Gson gson = new GsonBuilder().create();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                    service = retrofit.create(ApiService.class);
                }
            }
        }
        return service;
    }
}
