package com.piyushsatija.pollutionmonitor.utils

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
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

    fun getAQIColor(aqi: Int, context: Context): Int {
        return when (aqi) {
            in 0..50 -> ContextCompat.getColor(context, R.color.scaleGood)
            in 51..100 -> ContextCompat.getColor(context, R.color.scaleModerate)
            in 101..150 -> ContextCompat.getColor(context, R.color.scaleUnhealthySensitive)
            in 151..200 -> ContextCompat.getColor(context, R.color.scaleUnhealthy)
            in 201..300 -> ContextCompat.getColor(context, R.color.scaleVeryUnhealthy)
            else -> ContextCompat.getColor(context, R.color.scaleHazardous)
        }
    }

    fun getAQIDialogInfo(aqi: Int, context: Context): Bundle {
        val bundle = Bundle()
        when (aqi) {
            in 0..50 -> {
                bundle.putString("range", context.getString(R.string.good_range))
                bundle.putString("desc", context.getString(R.string.goodCaution))
                bundle.putInt("color", R.color.scaleGood)
            }
            in 51..100 -> {
                bundle.putString("range", context.getString(R.string.moderate_range))
                bundle.putString("desc", context.getString(R.string.moderateCaution))
                bundle.putInt("color", R.color.scaleModerate)
            }
            in 101..150 -> {
                bundle.putString("range", context.getString(R.string.unhealthy_for_sensitive_range))
                bundle.putString("desc", context.getString(R.string.unhealthySensitiveCaution))
                bundle.putInt("color", R.color.scaleUnhealthySensitive)
            }
            in 151..200 -> {
                bundle.putString("range", context.getString(R.string.unhealthy_range))
                bundle.putString("desc", context.getString(R.string.unhealthyCaution))
                bundle.putInt("color", R.color.scaleUnhealthy)
            }
            in 201..300 -> {
                bundle.putString("range", context.getString(R.string.very_unhealthy_range))
                bundle.putString("desc", context.getString(R.string.veryUnhealthyCaution))
                bundle.putInt("color", R.color.scaleVeryUnhealthy)
            }
            else -> {
                bundle.putString("range", context.getString(R.string.hazardous_range))
                bundle.putString("desc", context.getString(R.string.hazardousCaution))
                bundle.putInt("color", R.color.scaleHazardous)
            }
        }
        return bundle
    }
}