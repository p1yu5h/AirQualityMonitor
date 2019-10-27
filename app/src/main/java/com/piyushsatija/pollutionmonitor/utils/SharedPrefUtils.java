package com.piyushsatija.pollutionmonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefUtils {
    private static final String SHARED_PREF_NAME = "location";
    private static SharedPrefUtils INSTANCE = null;
    private SharedPreferences preferences;

    public static SharedPrefUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SharedPrefUtils(context);
            return INSTANCE;
        }
        return INSTANCE;
    }

    private SharedPrefUtils(Context context) {
        preferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    }

    public void saveLatestAQI(String location) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("aqi", location);
        editor.apply();
    }

    public String getLatestAQI() {
        return preferences.getString("aqi", "");
    }

    public void isDarkMode(Boolean b) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("darkMode", b);
        editor.apply();
    }

    public Boolean isDarkMode() {
        return preferences.getBoolean("darkMode", false);
    }

    public void rateCardDone(Boolean b) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("rateCard", b);
        editor.apply();
    }

    public Boolean rateCardDone() {
        return preferences.getBoolean("rateCard", false);
    }

    public void setAppInstallTime(Long time) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("appInstallTime", time);
        editor.apply();
    }

    public Long getAppInstallTime() {
        return preferences.getLong("appInstallTime", 0);
    }

    public void clearAllPrefs() {
        preferences.edit().clear().apply();
    }
}
