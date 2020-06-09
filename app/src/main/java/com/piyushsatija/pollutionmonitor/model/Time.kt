package com.piyushsatija.pollutionmonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Time {
    @SerializedName("s")
    @Expose
    var s: String? = null

    @SerializedName("tz")
    @Expose
    var tz: String? = null

    @SerializedName("v")
    @Expose
    var v: Int? = null
}