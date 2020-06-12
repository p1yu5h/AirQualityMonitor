package com.piyushsatija.pollutionmonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {
    @SerializedName("aqi")
    @Expose
    var aqi: Int? = null

    @SerializedName("idx")
    @Expose
    var idx: Int? = null

    @SerializedName("attributions")
    @Expose
    var attributions: List<Attribution>? = null

    @SerializedName("city")
    @Expose
    var city: City? = null

    @SerializedName("dominentpol")
    @Expose
    var dominentpol: String? = null

    @SerializedName("iaqi")
    @Expose
    var iaqi: Iaqi? = null

    @SerializedName("time")
    @Expose
    var time: Time? = null

    @SerializedName("debug")
    @Expose
    var debug: Debug? = null

}