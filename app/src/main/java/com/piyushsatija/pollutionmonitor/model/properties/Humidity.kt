package com.piyushsatija.pollutionmonitor.model.properties

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Humidity {
    @SerializedName("v")
    @Expose
    var v: Double? = null

}