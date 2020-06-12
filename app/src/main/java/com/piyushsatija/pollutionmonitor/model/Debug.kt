package com.piyushsatija.pollutionmonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Debug {
    @SerializedName("sync")
    @Expose
    var sync: String? = null

}