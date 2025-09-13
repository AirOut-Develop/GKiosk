package com.jwlryk.gkiosk.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Simple SharedPreferences bridge for persisting KioskInfo essentials. */
public final class KioskPrefs {
    private static final String PREFS = "gkiosk_prefs";

    private static final String KEY_API_BASE_URL = "api_base_url";
    private static final String KEY_WS_ENDPOINT = "ws_endpoint";
    private static final String KEY_STORE_CODE = "store_code";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_COMPANY_NO = "company_no";
    private static final String KEY_KIOSK_CODE = "kiosk_code";

    private KioskPrefs() {}

    public static void saveAll(Context ctx, KioskInfo ki) {
        if (ki == null) return;
        if (ctx == null) return; // require context for now
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_API_BASE_URL, ki.getApiBaseUrl())
                .putString(KEY_STORE_CODE, ki.getStoreCode())
                .putString(KEY_STORE_NAME, ki.getStoreName())
                .putString(KEY_COMPANY_NO, ki.getStoreCompanyNumber())
                .putString(KEY_KIOSK_CODE, ki.getKioskCode())
                .putString(KEY_WS_ENDPOINT, ki.getWsEndpoint())
                .apply();
    }

    public static void saveBasics(Context ctx, String baseUrl, String wsEndpoint, String storeCode, String kioskCode) {
        if (ctx == null) return;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_API_BASE_URL, baseUrl)
                .putString(KEY_WS_ENDPOINT, wsEndpoint)
                .putString(KEY_STORE_CODE, storeCode)
                .putString(KEY_KIOSK_CODE, kioskCode)
                .apply();
    }

    public static void loadIntoSingleton(Context ctx) {
        if (ctx == null) return;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        KioskInfo.getInstance()
                .setApiBaseUrl(sp.getString(KEY_API_BASE_URL, null))
                .setWsEndpoint(sp.getString(KEY_WS_ENDPOINT, null))
                .setStoreCode(sp.getString(KEY_STORE_CODE, null))
                .setStoreName(sp.getString(KEY_STORE_NAME, null))
                .setStoreCompanyNumber(sp.getString(KEY_COMPANY_NO, null))
                .setKioskCode(sp.getString(KEY_KIOSK_CODE, null));
    }
}
