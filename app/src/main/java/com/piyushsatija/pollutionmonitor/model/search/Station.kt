package com.piyushsatija.pollutionmonitor.model.search

import com.google.gson.annotations.SerializedName

data class Station(
        @SerializedName("name") val name: String,
        @SerializedName("geo") val geo: List<Float>,
        @SerializedName("url") val url: String,
        @SerializedName("country") val country: String
)