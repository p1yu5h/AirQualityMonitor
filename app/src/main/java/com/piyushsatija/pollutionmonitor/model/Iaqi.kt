package com.piyushsatija.pollutionmonitor.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.piyushsatija.pollutionmonitor.model.properties.*

public class Iaqi {
    @SerializedName("co")
    @Expose
    public var co: Co? = null

    @SerializedName("no2")
    @Expose
    public var no2: No2? = null

    @SerializedName("o3")
    @Expose
    public var o3: O3? = null

    @SerializedName("p")
    @Expose
    public var pressure: Pressure? = null

    @SerializedName("pm10")
    @Expose
    public var pm10: Pm10? = null

    @SerializedName("pm25")
    @Expose
    public var pm2_5: Pm2_5? = null

    @SerializedName("so2")
    @Expose
    public var so2: So2? = null

    @SerializedName("t")
    @Expose
    public var temperature: Temperature? = null

    @SerializedName("h")
    @Expose
    public var humidity: Humidity? = null

    @SerializedName("w")
    @Expose
    public var wind: Wind? = null

}