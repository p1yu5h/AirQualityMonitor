package com.piyushsatija.pollutionmonitor.model.search

import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("uid") val uid: Double,
        @SerializedName("aqi") val aqi: String,
        @SerializedName("time") val time: Time,
        @SerializedName("station") val station: Station
)