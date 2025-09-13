package com.jwlryk.gkiosk;

import android.app.Application;

public class GKioskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply default theme (Dark prioritized)
        Global.applyNightMode();
        // Apply stored locale if any (or system default)
        Global.applyStoredLocale(this);
    }
}
