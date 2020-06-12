package com.piyushsatija.pollutionmonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.piyushsatija.pollutionmonitor.model.properties.*

class Iaqi {
    @SerializedName("co")
    @Expose
    var co: Co? = null

    @SerializedName("no2")
    @Expose
    var no2: No2? = null

    @SerializedName("o3")
    @Expose
    var o3: O3? = null

    @SerializedName("p")
    @Expose
    var pressure: Pressure? = null

    @SerializedName("pm10")
    @Expose
    var pm10: Pm10? = null

    @SerializedName("pm25")
    @Expose
    var pm2_5: Pm2_5? = null

    @SerializedName("so2")
    @Expose
    var so2: So2? = null

    @SerializedName("t")
    @Expose
    var temperature: Temperature? = null

    @SerializedName("h")
    @Expose
    var humidity: Humidity? = null

    @SerializedName("w")
    @Expose
    var wind: Wind? = null

}