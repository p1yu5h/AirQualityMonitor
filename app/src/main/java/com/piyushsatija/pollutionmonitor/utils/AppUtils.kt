package com.piyushsatija.pollutionmonitor.utils

import android.content.Context
import android.os.Bundle
import com.piyushsatija.pollutionmonitor.R

object AppUtils {
    fun convertToFahrenheit(tempInCelsius: Double): Double {
        return (tempInCelsius * 9) / 5 + 32
    }

    fun convertToKmph(windSpeedInMps: Double): Double {
        return windSpeedInMps * 3.6
    }

    fun getAQIInfo(aqi: Int, context: Context): Bundle {
        val bundle = Bundle()
        when (aqi) {
            in 0..50 -> {
                bundle.putString("title", context.getString(R.string.good))
                bundle.putString("desc", context.getString(R.string.good_health_implications))
            }
            in 51..100 -> {
                bundle.putString("title", context.getString(R.string.moderate))
                bundle.putString("desc", context.getString(R.string.moderate_health_implications))
            }
            in 101..150 -> {
                bundle.putString("title", context.getString(R.string.unhealthy_for_sensitive))
                bundle.putString("desc", context.getString(R.string.unhealthy_for_sensitive_health_implications))
            }
            in 151..200 -> {
                bundle.putString("title", context.getString(R.string.unhealthy))
                bundle.putString("desc", context.getString(R.string.unhealthy_health_implications))
            }
            in 201..300 -> {
                bundle.putString("title", context.getString(R.string.very_unhealthy))
                bundle.putString("desc", context.getString(R.string.very_unhealthy_health_implications))
            }
            else -> {
                bundle.putString("title", context.getString(R.string.hazardous))
                bundle.putString("desc", context.getString(R.string.hazardous_health_implications))
            }
        }
        return bundle
    }
}