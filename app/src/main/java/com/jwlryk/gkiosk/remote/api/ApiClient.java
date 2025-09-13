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
    private static volatile String lastBaseUrl;

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
                    lastBaseUrl = baseUrl;
                }
            }
        }
        // Recreate if baseUrl changed
        String current = KioskInfo.getInstance().getApiBaseUrl();
        if (current == null || current.isEmpty()) current = "https://hub.airout.kr:8800/";
        if (!current.endsWith("/")) current = current + "/";
        if (lastBaseUrl == null || !lastBaseUrl.equals(current)) {
            reset();
            return get();
        }
        return service;
    }

    public static synchronized void reset() {
        service = null;
        lastBaseUrl = null;
    }
}
