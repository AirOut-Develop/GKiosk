package com.jwlryk.gkiosk;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

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

    // Locale management
    private static final String PREFS = "gkiosk_prefs";
    private static final String KEY_LANG = "language"; // values: "system", "ko", "en"

    public static void setLanguage(Context context, String langTag) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_LANG, langTag == null ? "system" : langTag).apply();

        LocaleListCompat locales = (langTag == null || langTag.isEmpty() || "system".equals(langTag))
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(langTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    public static String getLanguage(Context context) {
        if (context == null) return "system";
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_LANG, "system");
    }

    public static void applyStoredLocale(Context context) {
        if (context == null) return;
        String tag = getLanguage(context);
        LocaleListCompat locales = (tag == null || tag.isEmpty() || "system".equals(tag))
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag);
        AppCompatDelegate.setApplicationLocales(locales);
    }
}
