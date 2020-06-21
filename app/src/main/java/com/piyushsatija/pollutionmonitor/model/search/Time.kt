package com.piyushsatija.pollutionmonitor.model.search

import com.google.gson.annotations.SerializedName

data class Time(
        @SerializedName("tz") val tz: String,
        @SerializedName("stime") val stime: String,
        @SerializedName("vtime") val vtime: Double
)