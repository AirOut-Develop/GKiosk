package com.jwlryk.gkiosk;

import androidx.appcompat.app.AppCompatDelegate;

public class Global {

    public enum ThemeMode { LIGHT, DARK }

    private static ThemeMode themeMode = ThemeMode.LIGHT; // default: Light first

    public static ThemeMode getThemeMode() {
        return themeMode;
    }

    public static void setThemeMode(ThemeMode mode) {
        themeMode = mode != null ? mode : ThemeMode.DARK;
    }

    public static void applyNightMode() {
        if (themeMode == ThemeMode.DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
