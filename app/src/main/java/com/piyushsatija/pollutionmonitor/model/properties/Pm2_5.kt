package com.piyushsatija.pollutionmonitor.model.properties

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Pm2_5 {
    @SerializedName("v")
    @Expose
    var v: Double? = null

}