package com.piyushsatija.pollutionmonitor.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefUtils private constructor(context: Context) {
    private val preferences: SharedPreferences
    fun saveLatestAQI(location: String?) {
        val editor = preferences.edit()
        editor.putString("aqi", location)
        editor.apply()
    }

    val latestAQI: String?
        get() = preferences.getString("aqi", "")

    fun isDarkMode(b: Boolean?) {
        val editor = preferences.edit()
        editor.putBoolean("darkMode", b!!)
        editor.apply()
    }

    val isDarkMode: Boolean
        get() = preferences.getBoolean("darkMode", false)

    fun rateCardDone(b: Boolean?) {
        val editor = preferences.edit()
        editor.putBoolean("rateCard", b!!)
        editor.apply()
    }

    fun rateCardDone(): Boolean {
        return preferences.getBoolean("rateCard", false)
    }

    var appInstallTime: Long?
        get() = preferences.getLong("appInstallTime", 0)
        set(time) {
            val editor = preferences.edit()
            editor.putLong("appInstallTime", time!!)
            editor.apply()
        }

    fun clearAllPrefs() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "location"
        private var INSTANCE: SharedPrefUtils? = null
        fun getInstance(context: Context): SharedPrefUtils? {
            if (INSTANCE == null) {
                INSTANCE = SharedPrefUtils(context)
                return INSTANCE
            }
            return INSTANCE
        }
    }

    init {
        preferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }
}