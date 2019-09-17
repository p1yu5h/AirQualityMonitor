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
        return preferences.getString("aqi","");
    }

    public void clearAllPrefs() {
        preferences.edit().clear().apply();
    }
}
