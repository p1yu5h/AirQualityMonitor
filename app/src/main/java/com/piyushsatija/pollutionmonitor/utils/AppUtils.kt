package com.piyushsatija.pollutionmonitor.utils

object AppUtils {
    fun convertToFahrenheit(tempInCelsius: Double): Double {
        return (tempInCelsius * 9) / 5 + 32
    }

    fun convertToKmph(windSpeedInMps: Double): Double {
        return windSpeedInMps * 3.6
    }
}